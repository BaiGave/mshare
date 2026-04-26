/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.Constants;
import com.microsoft.aad.msal4j.HttpMethod;
import com.microsoft.aad.msal4j.ManagedIdentityApplication;
import com.microsoft.aad.msal4j.ManagedIdentityClient;
import com.microsoft.aad.msal4j.ManagedIdentityIdType;
import com.microsoft.aad.msal4j.ManagedIdentityParameters;
import com.microsoft.aad.msal4j.ManagedIdentitySourceType;
import com.microsoft.aad.msal4j.MsalRequest;
import com.microsoft.aad.msal4j.RequestContext;
import com.microsoft.aad.msal4j.StringHelper;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ManagedIdentityRequest
extends MsalRequest {
    private static final Logger LOG = LoggerFactory.getLogger(ManagedIdentityRequest.class);
    URI baseEndpoint;
    HttpMethod method;
    Map<String, String> headers;
    Map<String, String> bodyParameters;
    Map<String, String> queryParameters;

    public ManagedIdentityRequest(ManagedIdentityApplication managedIdentityApplication, RequestContext requestContext) {
        super(managedIdentityApplication, requestContext);
    }

    public String getBodyAsString() {
        if (this.bodyParameters == null || this.bodyParameters.isEmpty()) {
            return "";
        }
        return StringHelper.serializeQueryParameters(this.bodyParameters);
    }

    public URL computeURI() throws URISyntaxException {
        String endpoint = this.appendQueryParametersToBaseEndpoint();
        try {
            return new URL(endpoint);
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private String appendQueryParametersToBaseEndpoint() {
        if (this.queryParameters == null || this.queryParameters.isEmpty()) {
            return this.baseEndpoint.toString();
        }
        String queryString = StringHelper.serializeQueryParameters(this.queryParameters);
        return this.baseEndpoint.toString() + "?" + queryString;
    }

    void addUserAssignedIdToQuery(ManagedIdentityIdType idType, String userAssignedId) {
        switch (idType) {
            case CLIENT_ID: {
                LOG.info("[Managed Identity] Adding user assigned client id to the request.");
                this.queryParameters.put("client_id", userAssignedId);
                break;
            }
            case RESOURCE_ID: {
                LOG.info("[Managed Identity] Adding user assigned resource id to the request.");
                if (ManagedIdentityClient.getManagedIdentitySource() == ManagedIdentitySourceType.IMDS) {
                    this.queryParameters.put("msi_res_id", userAssignedId);
                    break;
                }
                this.queryParameters.put("mi_res_id", userAssignedId);
                break;
            }
            case OBJECT_ID: {
                LOG.info("[Managed Identity] Adding user assigned object id to the request.");
                this.queryParameters.put("object_id", userAssignedId);
            }
        }
    }

    void addTokenRevocationParametersToQuery(ManagedIdentityParameters parameters) {
        ManagedIdentitySourceType sourceType = ManagedIdentityClient.getManagedIdentitySource();
        boolean supportsTokenRevocation = Constants.TOKEN_REVOCATION_SUPPORTED_ENVIRONMENTS.contains((Object)sourceType);
        if (supportsTokenRevocation) {
            ManagedIdentityApplication managedIdentityApplication = (ManagedIdentityApplication)this.application();
            if (managedIdentityApplication.getClientCapabilities() != null && !managedIdentityApplication.getClientCapabilities().isEmpty()) {
                String clientCapabilities = String.join((CharSequence)",", managedIdentityApplication.getClientCapabilities());
                this.queryParameters.put("xms_cc", clientCapabilities.toString());
            }
            if (!StringHelper.isNullOrBlank(parameters.claims) && !StringHelper.isNullOrBlank(parameters.revokedTokenHash())) {
                LOG.info("[Managed Identity] Adding token revocation parameter to request");
                if (this.queryParameters == null) {
                    this.queryParameters = new HashMap<String, String>();
                }
                this.queryParameters.put("token_sha256_to_refresh", parameters.revokedTokenHash());
            }
        }
    }
}

