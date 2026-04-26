/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AuthenticationResultMetadata;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.ITenantProfile;
import java.io.Serializable;
import java.util.Date;

public interface IAuthenticationResult
extends Serializable {
    public String accessToken();

    public String idToken();

    public IAccount account();

    public ITenantProfile tenantProfile();

    public String environment();

    public String scopes();

    public Date expiresOnDate();

    default public AuthenticationResultMetadata metadata() {
        return AuthenticationResultMetadata.builder().build();
    }
}

