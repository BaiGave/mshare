/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.EnvironmentVariables;
import com.microsoft.aad.msal4j.HttpRequest;
import com.microsoft.aad.msal4j.IEnvironmentVariables;
import com.microsoft.aad.msal4j.IHttpResponse;
import com.microsoft.aad.msal4j.JsonHelper;
import com.microsoft.aad.msal4j.ManagedIdentityApplication;
import com.microsoft.aad.msal4j.ManagedIdentityErrorResponse;
import com.microsoft.aad.msal4j.ManagedIdentityIdType;
import com.microsoft.aad.msal4j.ManagedIdentityParameters;
import com.microsoft.aad.msal4j.ManagedIdentityRequest;
import com.microsoft.aad.msal4j.ManagedIdentityResponse;
import com.microsoft.aad.msal4j.ManagedIdentitySourceType;
import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.MsalJsonParsingException;
import com.microsoft.aad.msal4j.MsalRequest;
import com.microsoft.aad.msal4j.MsalServiceException;
import com.microsoft.aad.msal4j.ServiceBundle;
import java.net.SocketException;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractManagedIdentitySource {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractManagedIdentitySource.class);
    private static final String MANAGED_IDENTITY_NO_RESPONSE_RECEIVED = "[Managed Identity] Authentication unavailable. No response received from the managed identity endpoint.";
    protected final ManagedIdentityRequest managedIdentityRequest;
    protected final ServiceBundle serviceBundle;
    ManagedIdentitySourceType managedIdentitySourceType;
    ManagedIdentityIdType idType;
    String userAssignedId;
    private boolean isUserAssignedManagedIdentity;
    private String managedIdentityUserAssignedClientId;
    private String managedIdentityUserAssignedResourceId;

    public AbstractManagedIdentitySource(MsalRequest msalRequest, ServiceBundle serviceBundle, ManagedIdentitySourceType sourceType) {
        this.managedIdentityRequest = (ManagedIdentityRequest)msalRequest;
        this.managedIdentitySourceType = sourceType;
        this.serviceBundle = serviceBundle;
        this.idType = ((ManagedIdentityApplication)msalRequest.application()).getManagedIdentityId().getIdType();
        this.userAssignedId = ((ManagedIdentityApplication)msalRequest.application()).getManagedIdentityId().getUserAssignedId();
    }

    public ManagedIdentityResponse getManagedIdentityResponse(ManagedIdentityParameters parameters) {
        IHttpResponse response;
        this.createManagedIdentityRequest(parameters.resource);
        this.managedIdentityRequest.addTokenRevocationParametersToQuery(parameters);
        try {
            HttpRequest httpRequest = new HttpRequest(this.managedIdentityRequest.method, this.managedIdentityRequest.computeURI().toString(), this.managedIdentityRequest.headers);
            response = this.serviceBundle.getHttpHelper().executeHttpRequest(httpRequest, this.managedIdentityRequest.requestContext(), this.serviceBundle);
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

    public ManagedIdentityResponse handleResponse(ManagedIdentityParameters parameters, IHttpResponse response) {
        try {
            if (response.statusCode() == 200) {
                LOG.info("[Managed Identity] Successful response received.");
                return this.getSuccessfulResponse(response);
            }
            String message = this.getMessageFromErrorResponse(response);
            LOG.error(String.format("[Managed Identity] request failed, HttpStatusCode: %s, Error message: %s", response.statusCode(), message));
            throw new MsalServiceException(message, "managed_identity_request_failed", this.managedIdentitySourceType);
        }
        catch (Exception e) {
            if (e instanceof MsalServiceException) {
                throw e;
            }
            String message = String.format("[Managed Identity] Unexpected exception occurred when parsing the response, HttpStatusCode: %s, Error message: %s", response.statusCode(), e.getMessage());
            throw new MsalServiceException(message, "managed_identity_request_failed", this.managedIdentitySourceType);
        }
    }

    public abstract void createManagedIdentityRequest(String var1);

    protected ManagedIdentityResponse getSuccessfulResponse(IHttpResponse response) {
        ManagedIdentityResponse managedIdentityResponse;
        try {
            managedIdentityResponse = JsonHelper.convertJsonStringToJsonSerializableObject(response.body(), ManagedIdentityResponse::fromJson);
        }
        catch (MsalJsonParsingException e) {
            throw new MsalJsonParsingException(String.format("[Managed Identity] MSI returned %s, but the response could not be parsed: %s", response.statusCode(), e.getMessage()), "managed_identity_response_parse_failure", this.managedIdentitySourceType);
        }
        if (managedIdentityResponse == null || managedIdentityResponse.getAccessToken() == null || managedIdentityResponse.getAccessToken().isEmpty() || managedIdentityResponse.getExpiresOn() == null || managedIdentityResponse.getExpiresOn().isEmpty()) {
            throw new MsalServiceException("[Managed Identity] Response is either null or insufficient for authentication.", "managed_identity_request_failed", this.managedIdentitySourceType);
        }
        return managedIdentityResponse;
    }

    protected String getMessageFromErrorResponse(IHttpResponse response) {
        ManagedIdentityErrorResponse managedIdentityErrorResponse;
        try {
            managedIdentityErrorResponse = JsonHelper.convertJsonStringToJsonSerializableObject(response.body(), ManagedIdentityErrorResponse::fromJson);
        }
        catch (MsalJsonParsingException e) {
            throw new MsalJsonParsingException(String.format("[Managed Identity] MSI returned %s, but the response could not be parsed: %s", response.statusCode(), e.getMessage()), "managed_identity_response_parse_failure", this.managedIdentitySourceType);
        }
        if (managedIdentityErrorResponse == null) {
            return MANAGED_IDENTITY_NO_RESPONSE_RECEIVED;
        }
        if (managedIdentityErrorResponse.getMessage() != null && !managedIdentityErrorResponse.getMessage().isEmpty()) {
            return String.format("[Managed Identity] Error Message: %s Managed Identity Correlation ID: %s Use this Correlation ID for further investigation.", managedIdentityErrorResponse.getMessage(), managedIdentityErrorResponse.getCorrelationId());
        }
        return String.format("[Managed Identity] Error Code: %s Error Message: %s", managedIdentityErrorResponse.getError(), managedIdentityErrorResponse.getErrorDescription());
    }

    protected static IEnvironmentVariables getEnvironmentVariables() {
        return ManagedIdentityApplication.environmentVariables == null ? new EnvironmentVariables() : ManagedIdentityApplication.environmentVariables;
    }

    public boolean isUserAssignedManagedIdentity() {
        return this.isUserAssignedManagedIdentity;
    }

    public String getManagedIdentityUserAssignedClientId() {
        return this.managedIdentityUserAssignedClientId;
    }

    public String getManagedIdentityUserAssignedResourceId() {
        return this.managedIdentityUserAssignedResourceId;
    }

    public void setUserAssignedManagedIdentity(boolean isUserAssignedManagedIdentity) {
        this.isUserAssignedManagedIdentity = isUserAssignedManagedIdentity;
    }

    public void setManagedIdentityUserAssignedClientId(String managedIdentityUserAssignedClientId) {
        this.managedIdentityUserAssignedClientId = managedIdentityUserAssignedClientId;
    }

    public void setManagedIdentityUserAssignedResourceId(String managedIdentityUserAssignedResourceId) {
        this.managedIdentityUserAssignedResourceId = managedIdentityUserAssignedResourceId;
    }
}

