/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AbstractManagedIdentitySource;
import com.microsoft.aad.msal4j.HttpMethod;
import com.microsoft.aad.msal4j.IEnvironmentVariables;
import com.microsoft.aad.msal4j.ManagedIdentitySourceType;
import com.microsoft.aad.msal4j.MsalRequest;
import com.microsoft.aad.msal4j.MsalServiceException;
import com.microsoft.aad.msal4j.ServiceBundle;
import com.microsoft.aad.msal4j.StringHelper;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AppServiceManagedIdentitySource
extends AbstractManagedIdentitySource {
    private static final Logger LOG = LoggerFactory.getLogger(AppServiceManagedIdentitySource.class);
    private static final String APP_SERVICE_MSI_API_VERSION = "2019-08-01";
    private static final String SECRET_HEADER_NAME = "X-IDENTITY-HEADER";
    private final URI msiEndpoint;
    private final String identityHeader;

    @Override
    public void createManagedIdentityRequest(String resource) {
        this.managedIdentityRequest.baseEndpoint = this.msiEndpoint;
        this.managedIdentityRequest.method = HttpMethod.GET;
        this.managedIdentityRequest.headers = new HashMap<String, String>();
        this.managedIdentityRequest.headers.put(SECRET_HEADER_NAME, this.identityHeader);
        this.managedIdentityRequest.queryParameters = new HashMap<String, String>();
        this.managedIdentityRequest.queryParameters.put("api-version", APP_SERVICE_MSI_API_VERSION);
        this.managedIdentityRequest.queryParameters.put("resource", resource);
        if (this.idType != null && !StringHelper.isNullOrBlank(this.userAssignedId)) {
            LOG.info("[Managed Identity] Adding user assigned ID to the request for App Service Managed Identity.");
            this.managedIdentityRequest.addUserAssignedIdToQuery(this.idType, this.userAssignedId);
        }
    }

    private AppServiceManagedIdentitySource(MsalRequest msalRequest, ServiceBundle serviceBundle, URI msiEndpoint, String secret) {
        super(msalRequest, serviceBundle, ManagedIdentitySourceType.APP_SERVICE);
        this.msiEndpoint = msiEndpoint;
        this.identityHeader = secret;
    }

    static AbstractManagedIdentitySource create(MsalRequest msalRequest, ServiceBundle serviceBundle) {
        IEnvironmentVariables environmentVariables = AppServiceManagedIdentitySource.getEnvironmentVariables();
        String msiSecret = environmentVariables.getEnvironmentVariable("IDENTITY_HEADER");
        String msiEndpoint = environmentVariables.getEnvironmentVariable("IDENTITY_ENDPOINT");
        URI validatedEndpoint = AppServiceManagedIdentitySource.validateAndGetUri(msiEndpoint, msiSecret);
        return validatedEndpoint == null ? null : new AppServiceManagedIdentitySource(msalRequest, serviceBundle, validatedEndpoint, msiSecret);
    }

    private static URI validateAndGetUri(String msiEndpoint, String secret) {
        URI endpointUri;
        if (StringHelper.isNullOrBlank(msiEndpoint) || StringHelper.isNullOrBlank(secret)) {
            LOG.info("[Managed Identity] App service managed identity is unavailable.");
            return null;
        }
        try {
            endpointUri = new URI(msiEndpoint);
        }
        catch (URISyntaxException ex) {
            throw new MsalServiceException(String.format("[Managed Identity] The environment variable %s contains an invalid Uri %s in %s managed identity source.", "IDENTITY_ENDPOINT", msiEndpoint, "App Service"), "invalid_managed_identity_endpoint", ManagedIdentitySourceType.APP_SERVICE);
        }
        LOG.info("[Managed Identity] Environment variables validation passed for app service managed identity. Endpoint URI: {endpointUri}. Creating App Service managed identity.");
        return endpointUri;
    }
}

