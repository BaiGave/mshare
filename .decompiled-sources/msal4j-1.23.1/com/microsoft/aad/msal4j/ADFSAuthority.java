/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.Authority;
import com.microsoft.aad.msal4j.AuthorityType;
import java.net.URL;

class ADFSAuthority
extends Authority {
    static final String AUTHORIZATION_ENDPOINT = "oauth2/authorize";
    static final String TOKEN_ENDPOINT = "oauth2/token";
    static final String DEVICE_CODE_ENDPOINT = "oauth2/devicecode";
    private static final String ADFS_AUTHORITY_FORMAT = "https://%s/%s/";
    private static final String DEVICE_CODE_ENDPOINT_FORMAT = "https://%s/%s/oauth2/devicecode";

    ADFSAuthority(URL authorityUrl) {
        super(authorityUrl, AuthorityType.ADFS);
        this.authority = String.format(ADFS_AUTHORITY_FORMAT, this.host, this.tenant);
        this.authorizationEndpoint = this.authority + AUTHORIZATION_ENDPOINT;
        this.selfSignedJwtAudience = this.tokenEndpoint = this.authority + TOKEN_ENDPOINT;
        this.deviceCodeEndpoint = String.format(DEVICE_CODE_ENDPOINT_FORMAT, this.host, this.tenant);
    }
}

