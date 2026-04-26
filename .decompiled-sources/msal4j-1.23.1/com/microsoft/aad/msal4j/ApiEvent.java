/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.Event;
import com.microsoft.aad.msal4j.StringHelper;
import java.net.URI;
import java.util.Locale;

class ApiEvent
extends Event {
    private static final String API_ID_KEY = "msal.api_id";
    private static final String AUTHORITY_KEY = "msal.authority";
    private static final String AUTHORITY_TYPE_KEY = "msal.authority_type";
    private static final String TENANT_ID_KEY = "msal.tenant_id";
    private static final String USER_ID_KEY = "msal.user_id";
    private static final String WAS_SUCCESSFUL_KEY = "msal.was_succesful";
    private static final String CORRELATION_ID_KEY = "msal.correlation_id";
    private static final String REQUEST_ID_KEY = "msal.request_id";
    private static final String IS_CONFIDENTIAL_CLIENT_KEY = "msal.is_confidential_client";
    private static final String API_ERROR_CODE_KEY = "msal.api_error_code";
    private Boolean logPii;

    public ApiEvent(Boolean logPii) {
        super("msal.api_event");
        this.logPii = logPii;
    }

    public void setApiId(int apiId) {
        this.put(API_ID_KEY, Integer.toString(apiId).toLowerCase(Locale.ROOT));
    }

    public void setAuthority(URI authority) {
        this.put(AUTHORITY_KEY, ApiEvent.scrubTenant(authority));
    }

    public void setAuthorityType(String authorityType) {
        this.put(AUTHORITY_TYPE_KEY, authorityType.toLowerCase(Locale.ROOT));
    }

    public void setTenantId(String tenantId) {
        if (!StringHelper.isBlank(tenantId) && this.logPii.booleanValue()) {
            this.put(TENANT_ID_KEY, StringHelper.createBase64EncodedSha256Hash(tenantId));
        } else {
            this.put(TENANT_ID_KEY, null);
        }
    }

    public void setAccountId(String accountId) {
        if (!StringHelper.isBlank(accountId) && this.logPii.booleanValue()) {
            this.put(USER_ID_KEY, StringHelper.createBase64EncodedSha256Hash(accountId));
        } else {
            this.put(USER_ID_KEY, null);
        }
    }

    public void setWasSuccessful(boolean wasSuccessful) {
        this.put(WAS_SUCCESSFUL_KEY, String.valueOf(wasSuccessful).toLowerCase(Locale.ROOT));
    }

    public boolean getWasSuccessful() {
        return Boolean.valueOf((String)this.get(WAS_SUCCESSFUL_KEY));
    }

    public void setCorrelationId(String correlationId) {
        this.put(CORRELATION_ID_KEY, correlationId);
    }

    public void setRequestId(String requestId) {
        this.put(REQUEST_ID_KEY, requestId);
    }

    public void setIsConfidentialClient(boolean isConfidentialClient) {
        this.put(IS_CONFIDENTIAL_CLIENT_KEY, String.valueOf(isConfidentialClient).toLowerCase(Locale.ROOT));
    }

    public void setApiErrorCode(String apiErrorCode) {
        this.put(API_ERROR_CODE_KEY, apiErrorCode);
    }
}

