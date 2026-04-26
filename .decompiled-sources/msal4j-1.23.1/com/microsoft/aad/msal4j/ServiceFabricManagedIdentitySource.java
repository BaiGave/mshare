/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AbstractManagedIdentitySource;
import com.microsoft.aad.msal4j.DefaultHttpClientManagedIdentity;
import com.microsoft.aad.msal4j.HttpHelper;
import com.microsoft.aad.msal4j.HttpMethod;
import com.microsoft.aad.msal4j.HttpRequest;
import com.microsoft.aad.msal4j.IEnvironmentVariables;
import com.microsoft.aad.msal4j.IHttpClient;
import com.microsoft.aad.msal4j.IHttpResponse;
import com.microsoft.aad.msal4j.IRetryPolicy;
import com.microsoft.aad.msal4j.ManagedIdentityParameters;
import com.microsoft.aad.msal4j.ManagedIdentityResponse;
import com.microsoft.aad.msal4j.ManagedIdentityRetryPolicy;
import com.microsoft.aad.msal4j.ManagedIdentitySourceType;
import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.MsalRequest;
import com.microsoft.aad.msal4j.MsalServiceException;
import com.microsoft.aad.msal4j.ServiceBundle;
import com.microsoft.aad.msal4j.StringHelper;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ServiceFabricManagedIdentitySource
extends AbstractManagedIdentitySource {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceFabricManagedIdentitySource.class);
    private static final String SERVICE_FABRIC_MSI_API_VERSION = "2019-07-01-preview";
    private final URI msiEndpoint;
    private final String identityHeader;
    private static IHttpClient httpClient = new DefaultHttpClientManagedIdentity(null, null, null, null);
    private static HttpHelper httpHelper = new HttpHelper(httpClient, (IRetryPolicy)new ManagedIdentityRetryPolicy());

    @Override
    public void createManagedIdentityRequest(String resource) {
        this.managedIdentityRequest.baseEndpoint = this.msiEndpoint;
        this.managedIdentityRequest.method = HttpMethod.GET;
        this.managedIdentityRequest.headers = new HashMap<String, String>();
        this.managedIdentityRequest.headers.put("secret", this.identityHeader);
        this.managedIdentityRequest.queryParameters = new HashMap<String, String>();
        this.managedIdentityRequest.queryParameters.put("resource", resource);
        this.managedIdentityRequest.queryParameters.put("api-version", SERVICE_FABRIC_MSI_API_VERSION);
        if (this.idType != null && !StringHelper.isNullOrBlank(this.userAssignedId)) {
            LOG.info("[Managed Identity] Adding user assigned ID to the request for Service Fabric Managed Identity.");
            this.managedIdentityRequest.addUserAssignedIdToQuery(this.idType, this.userAssignedId);
        }
    }

    private ServiceFabricManagedIdentitySource(MsalRequest msalRequest, ServiceBundle serviceBundle, URI msiEndpoint, String identityHeader) {
        super(msalRequest, serviceBundle, ManagedIdentitySourceType.SERVICE_FABRIC);
        this.msiEndpoint = msiEndpoint;
        this.identityHeader = identityHeader;
    }

    @Override
    public ManagedIdentityResponse getManagedIdentityResponse(ManagedIdentityParameters parameters) {
        IHttpResponse response;
        this.createManagedIdentityRequest(parameters.resource);
        this.managedIdentityRequest.addTokenRevocationParametersToQuery(parameters);
        try {
            HttpRequest httpRequest = this.managedIdentityRequest.method.equals((Object)HttpMethod.GET) ? new HttpRequest(HttpMethod.GET, this.managedIdentityRequest.computeURI().toString(), this.managedIdentityRequest.headers) : new HttpRequest(HttpMethod.POST, this.managedIdentityRequest.computeURI().toString(), this.managedIdentityRequest.headers, this.managedIdentityRequest.getBodyAsString());
            response = httpHelper.executeHttpRequest(httpRequest, this.managedIdentityRequest.requestContext(), this.serviceBundle.getTelemetryManager(), httpClient);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        catch (MsalClientException e) {
            if (e.getCause() instanceof SocketException) {
                throw new MsalServiceException(e.getMessage(), "managed_identity_unreachable_network", this.managedIdentitySourceType);
            }
            throw e;
        }
        return this.handleResponse(parameters, response);
    }

    static AbstractManagedIdentitySource create(MsalRequest msalRequest, ServiceBundle serviceBundle) {
        IEnvironmentVariables environmentVariables = ServiceFabricManagedIdentitySource.getEnvironmentVariables();
        String identityEndpoint = environmentVariables.getEnvironmentVariable("IDENTITY_ENDPOINT");
        String identityHeader = environmentVariables.getEnvironmentVariable("IDENTITY_HEADER");
        String identityServerThumbprint = environmentVariables.getEnvironmentVariable("IDENTITY_SERVER_THUMBPRINT");
        if (StringHelper.isNullOrBlank(identityEndpoint) || StringHelper.isNullOrBlank(identityHeader) || StringHelper.isNullOrBlank(identityServerThumbprint)) {
            LOG.info("[Managed Identity] Service fabric managed identity is unavailable.");
            return null;
        }
        return new ServiceFabricManagedIdentitySource(msalRequest, serviceBundle, ServiceFabricManagedIdentitySource.validateAndGetUri(identityEndpoint), identityHeader);
    }

    private static URI validateAndGetUri(String msiEndpoint) {
        try {
            URI endpointUri = new URI(msiEndpoint);
            LOG.info(String.format("[Managed Identity] Environment variables validation passed for Service Fabric Managed Identity. Endpoint URI: %s", endpointUri));
            return endpointUri;
        }
        catch (URISyntaxException ex) {
            throw new MsalServiceException(String.format("[Managed Identity] The environment variable %s contains an invalid Uri %s in %s managed identity source.", "MSI_ENDPOINT", msiEndpoint, "Service Fabric"), "invalid_managed_identity_endpoint", ManagedIdentitySourceType.SERVICE_FABRIC);
        }
    }

    static void setHttpClient(IHttpClient client) {
        httpClient = client;
        httpHelper = new HttpHelper(httpClient, (IRetryPolicy)new ManagedIdentityRetryPolicy());
    }

    static void resetHttpClient() {
        httpClient = new DefaultHttpClientManagedIdentity(null, null, null, null);
        httpHelper = new HttpHelper(httpClient, (IRetryPolicy)new ManagedIdentityRetryPolicy());
    }
}

