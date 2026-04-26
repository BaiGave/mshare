/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import java.io.Serializable;
import java.util.Map;

public interface ITenantProfile
extends Serializable {
    public Map<String, ?> getClaims();

    public String environment();
}

