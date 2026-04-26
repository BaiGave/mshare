/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.ITenantProfile;
import java.util.Map;
import java.util.Objects;

class Account
implements IAccount {
    String homeAccountId;
    String environment;
    String username;
    Map<String, ITenantProfile> tenantProfiles;

    Account(String homeAccountId, String environment, String username, Map<String, ITenantProfile> tenantProfiles) {
        this.homeAccountId = homeAccountId;
        this.environment = environment;
        this.username = username;
        this.tenantProfiles = tenantProfiles;
    }

    @Override
    public Map<String, ITenantProfile> getTenantProfiles() {
        return this.tenantProfiles;
    }

    @Override
    public String homeAccountId() {
        return this.homeAccountId;
    }

    @Override
    public String environment() {
        return this.environment;
    }

    @Override
    public String username() {
        return this.username;
    }

    void username(String username) {
        this.username = username;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Account)) {
            return false;
        }
        Account other = (Account)o;
        return Objects.equals(this.homeAccountId(), other.homeAccountId());
    }

    public int hashCode() {
        int result = 1;
        result = result * 59 + (this.homeAccountId == null ? 43 : this.homeAccountId.hashCode());
        return result;
    }
}

