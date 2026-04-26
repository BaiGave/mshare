/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.Authority;
import com.microsoft.aad.msal4j.AuthorityType;
import java.net.URL;

class AADAuthority
extends Authority {
    private static final String TENANTLESS_TENANT_NAME = "common";
    private static final String AUTHORIZATION_ENDPOINT = "oauth2/v2.0/authorize";
    private static final String TOKEN_ENDPOINT = "oauth2/v2.0/token";
    static final String DEVICE_CODE_ENDPOINT = "oauth2/v2.0/devicecode";
    private static final String AAD_AUTHORITY_FORMAT = "https://%s/%s/";
    private static final String AAD_AUTHORIZATION_ENDPOINT_FORMAT = "https://%s/%s/oauth2/v2.0/authorize";
    private static final String AAD_TOKEN_ENDPOINT_FORMAT = "https://%s/%s/oauth2/v2.0/token";
    private static final String DEVICE_CODE_ENDPOINT_FORMAT = "https://%s/%s/oauth2/v2.0/devicecode";

    AADAuthority(URL authorityUrl) {
        super(authorityUrl, AuthorityType.AAD);
        this.setAuthorityProperties();
        this.authority = String.format(AAD_AUTHORITY_FORMAT, this.host, this.tenant);
    }

    private void setAuthorityProperties() {
        this.authorizationEndpoint = String.format(AAD_AUTHORIZATION_ENDPOINT_FORMAT, this.host, this.tenant);
        this.tokenEndpoint = String.format(AAD_TOKEN_ENDPOINT_FORMAT, this.host, this.tenant);
        this.deviceCodeEndpoint = String.format(DEVICE_CODE_ENDPOINT_FORMAT, this.host, this.tenant);
        this.isTenantless = TENANTLESS_TENANT_NAME.equalsIgnoreCase(this.tenant);
        this.selfSignedJwtAudience = this.tokenEndpoint;
    }
}

