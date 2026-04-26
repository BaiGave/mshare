/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AbstractMsalAuthorizationGrant;
import com.microsoft.aad.msal4j.ClaimsRequest;
import java.util.Map;
import java.util.Set;

class IntegratedWindowsAuthorizationGrant
extends AbstractMsalAuthorizationGrant {
    private final String userName;

    IntegratedWindowsAuthorizationGrant(Set<String> scopes, String userName, ClaimsRequest claims) {
        this.userName = userName;
        this.scopes = scopes;
        this.claims = claims;
    }

    @Override
    Map<String, String> toParameters() {
        return null;
    }

    String getUserName() {
        return this.userName;
    }
}

