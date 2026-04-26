/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.ClaimsRequest;
import java.util.Map;
import java.util.Set;

interface IAcquireTokenParameters {
    public Set<String> scopes();

    public ClaimsRequest claims();

    public Map<String, String> extraHttpHeaders();

    public String tenant();

    public Map<String, String> extraQueryParameters();
}

