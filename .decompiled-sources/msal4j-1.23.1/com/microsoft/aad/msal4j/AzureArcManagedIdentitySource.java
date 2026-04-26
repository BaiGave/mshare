/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AbstractManagedIdentitySource;
import com.microsoft.aad.msal4j.HttpMethod;
import com.microsoft.aad.msal4j.HttpRequest;
import com.microsoft.aad.msal4j.IEnvironmentVariables;
import com.microsoft.aad.msal4j.IHttpResponse;
import com.microsoft.aad.msal4j.ManagedIdentityApplication;
import com.microsoft.aad.msal4j.ManagedIdentityIdType;
import com.microsoft.aad.msal4j.ManagedIdentityParameters;
import com.microsoft.aad.msal4j.ManagedIdentityResponse;
import com.microsoft.aad.msal4j.ManagedIdentitySourceType;
import com.microsoft.aad.msal4j.MsalRequest;
import com.microsoft.aad.msal4j.MsalServiceException;
import com.microsoft.aad.msal4j.ServiceBundle;
import com.microsoft.aad.msal4j.StringHelper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AzureArcManagedIdentitySource
extends AbstractManagedIdentitySource {
    private static final Logger LOG = LoggerFactory.getLogger(AzureArcManagedIdentitySource.class);
    private static final String ARC_API_VERSION = "2019-11-01";
    private static final String AZURE_ARC = "Azure Arc";
    private static final String WINDOWS_PATH = System.getenv("ProgramData") + "/AzureConnectedMachineAgent/Tokens/";
    private static final String LINUX_PATH = "/var/opt/azcmagent/tokens/";
    private static final String FILE_EXTENSION = ".key";
    private static final int MAX_FILE_SIZE_BYTES = 4096;
    private static final String WWW_AUTHENTICATE_HEADER = "WWW-Authenticate";
    private final URI MSI_ENDPOINT;

    static AbstractManagedIdentitySource create(MsalRequest msalRequest, ServiceBundle serviceBundle) {
        String imdsEndpoint;
        IEnvironmentVariables environmentVariables = AzureArcManagedIdentitySource.getEnvironmentVariables();
        String identityEndpoint = environmentVariables.getEnvironmentVariable("IDENTITY_ENDPOINT");
        URI validatedUri = AzureArcManagedIdentitySource.validateAndGetUri(identityEndpoint, imdsEndpoint = environmentVariables.getEnvironmentVariable("IMDS_ENDPOINT"));
        return validatedUri == null ? null : new AzureArcManagedIdentitySource(validatedUri, msalRequest, serviceBundle);
    }

    private static URI validateAndGetUri(String identityEndpoint, String imdsEndpoint) {
        URI endpointUri;
        if (StringHelper.isNullOrBlank(identityEndpoint) || StringHelper.isNullOrBlank(imdsEndpoint)) {
            LOG.info("[Managed Identity] Azure Arc managed identity is unavailable.");
            return null;
        }
        try {
            endpointUri = new URI(identityEndpoint);
        }
        catch (URISyntaxException e) {
            throw new MsalServiceException(String.format("[Managed Identity] The environment variable %s contains an invalid Uri %s in %s managed identity source.", "IDENTITY_ENDPOINT", identityEndpoint, AZURE_ARC), "invalid_managed_identity_endpoint", ManagedIdentitySourceType.AZURE_ARC);
        }
        LOG.info(String.format("[Managed Identity] Creating Azure Arc managed identity. Endpoint URI: %s", endpointUri));
        return endpointUri;
    }

    private AzureArcManagedIdentitySource(URI endpoint, MsalRequest msalRequest, ServiceBundle serviceBundle) {
        super(msalRequest, serviceBundle, ManagedIdentitySourceType.AZURE_ARC);
        this.MSI_ENDPOINT = endpoint;
        ManagedIdentityIdType idType = ((ManagedIdentityApplication)msalRequest.application()).getManagedIdentityId().getIdType();
        if (idType != ManagedIdentityIdType.SYSTEM_ASSIGNED) {
            throw new MsalServiceException(String.format("[Managed Identity] User assigned identity is not supported by the %s Managed Identity. To authenticate with the system assigned identity use ManagedIdentityApplication.builder(ManagedIdentityId.systemAssigned()).build().", AZURE_ARC), "user_assigned_managed_identity_not_supported", ManagedIdentitySourceType.AZURE_ARC);
        }
    }

    @Override
    public void createManagedIdentityRequest(String resource) {
        this.managedIdentityRequest.baseEndpoint = this.MSI_ENDPOINT;
        this.managedIdentityRequest.method = HttpMethod.GET;
        this.managedIdentityRequest.headers = new HashMap<String, String>();
        this.managedIdentityRequest.headers.put("Metadata", "true");
        this.managedIdentityRequest.queryParameters = new HashMap<String, String>();
        this.managedIdentityRequest.queryParameters.put("api-version", ARC_API_VERSION);
        this.managedIdentityRequest.queryParameters.put("resource", resource);
    }

    @Override
    public ManagedIdentityResponse handleResponse(ManagedIdentityParameters parameters, IHttpResponse response) {
        LOG.info("[Managed Identity] Response received. Status code: {}", (Object)response.statusCode());
        if (response.statusCode() == 401) {
            String challenge = this.readChallengeFrom(response).orElseGet(() -> {
                LOG.error("[Managed Identity] {} is expected but not found.", (Object)WWW_AUTHENTICATE_HEADER);
                throw new MsalServiceException("[Managed Identity] Did not receive expected WWW-Authenticate header in the response from Azure Arc Managed Identity Endpoint.", "managed_identity_request_failed", ManagedIdentitySourceType.AZURE_ARC);
            });
            String[] splitChallenge = challenge.split("=");
            if (splitChallenge.length != 2) {
                LOG.error("[Managed Identity] The {} header for Azure arc managed identity is not an expected format.", (Object)WWW_AUTHENTICATE_HEADER);
                throw new MsalServiceException("[Managed Identity] The WWW-Authenticate header in the response from Azure Arc Managed Identity Endpoint did not match the expected format.", "managed_identity_request_failed", ManagedIdentitySourceType.AZURE_ARC);
            }
            Path path = Paths.get(splitChallenge[1], new String[0]).normalize();
            this.validateFile(path);
            if (!path.toFile().exists()) {
                LOG.error("[Managed Identity] The WWW-Authenticate header specifies a file that does not exist");
                throw new MsalServiceException("[Managed Identity] The file on the file path in the WWW-Authenticate header is not secure or could not be found.", "managed_identity_file_read_error", ManagedIdentitySourceType.AZURE_ARC);
            }
            String authHeaderValue = null;
            try {
                authHeaderValue = "Basic " + new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            }
            catch (IOException e) {
                throw new MsalServiceException(e.getMessage(), "managed_identity_file_read_error", ManagedIdentitySourceType.AZURE_ARC);
            }
            this.createManagedIdentityRequest(parameters.resource);
            LOG.info("[Managed Identity] Adding authorization header to the request.");
            this.managedIdentityRequest.headers.put("Authorization", authHeaderValue);
            try {
                response = this.serviceBundle.getHttpHelper().executeHttpRequest(new HttpRequest(HttpMethod.GET, this.managedIdentityRequest.computeURI().toString(), this.managedIdentityRequest.headers), this.managedIdentityRequest.requestContext(), this.serviceBundle);
            }
            catch (URISyntaxException e) {
                throw new MsalServiceException("[Managed Identity] The environment variable %s contains an invalid Uri %s in %s managed identity source.", "invalid_managed_identity_endpoint", this.managedIdentitySourceType);
            }
            return super.handleResponse(parameters, response);
        }
        return super.handleResponse(parameters, response);
    }

    private Optional<String> readChallengeFrom(IHttpResponse response) {
        return response.headers().entrySet().stream().filter(entry -> WWW_AUTHENTICATE_HEADER.equalsIgnoreCase((String)entry.getKey())).map(Map.Entry::getValue).flatMap(Collection::stream).findFirst();
    }

    private void validateFile(Path path) {
        String osName = System.getProperty("os.name").toLowerCase();
        if (!osName.contains("windows") && !osName.contains("linux")) {
            LOG.error(String.format("[Managed Identity] Unsupported platform: %s", osName));
            throw new MsalServiceException("[Managed Identity] This managed identity source is not available on this platform.", "managed_identity_file_read_error", ManagedIdentitySourceType.AZURE_ARC);
        }
        if (this.isValidWindowsPath(path) || this.isValidLinuxPath(path)) {
            if (path.toFile().length() > 4096L) {
                LOG.error(String.format("[Managed Identity] File is larger than %s bytes.", 4096));
                throw new MsalServiceException("[Managed Identity] The file on the file path in the WWW-Authenticate header is not secure or could not be found.", "managed_identity_file_read_error", ManagedIdentitySourceType.AZURE_ARC);
            }
        } else {
            LOG.error("[Managed Identity] Invalid filepath.");
            throw new MsalServiceException("[Managed Identity] The file on the file path in the WWW-Authenticate header is not secure or could not be found.", "managed_identity_file_read_error", ManagedIdentitySourceType.AZURE_ARC);
        }
        LOG.info("[Managed Identity] Path passed validation.");
    }

    private boolean isValidWindowsPath(Path path) {
        return path.startsWith(WINDOWS_PATH) && path.toString().toLowerCase().endsWith(FILE_EXTENSION);
    }

    private boolean isValidLinuxPath(Path path) {
        return path.startsWith(LINUX_PATH) && path.toString().toLowerCase().endsWith(FILE_EXTENSION);
    }
}

