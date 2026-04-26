/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.ITenantProfile;
import java.io.Serializable;
import java.util.Map;

public interface IAccount
extends Serializable {
    public String homeAccountId();

    public String environment();

    public String username();

    public Map<String, ITenantProfile> getTenantProfiles();
}

