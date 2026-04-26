/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.ITenantProfile;
import java.util.Map;

class TenantProfile
implements ITenantProfile {
    Map<String, ?> idTokenClaims;
    String environment;

    public TenantProfile(Map<String, ?> idTokenClaims, String environment) {
        this.idTokenClaims = idTokenClaims;
        this.environment = environment;
    }

    @Override
    public Map<String, ?> getClaims() {
        return this.idTokenClaims;
    }

    public Map<String, ?> idTokenClaims() {
        return this.idTokenClaims;
    }

    @Override
    public String environment() {
        return this.environment;
    }

    public TenantProfile idTokenClaims(Map<String, ?> idTokenClaims) {
        this.idTokenClaims = idTokenClaims;
        return this;
    }

    public TenantProfile environment(String environment) {
        this.environment = environment;
        return this;
    }
}

