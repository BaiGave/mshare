/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.ITokenCache;

public interface ITokenCacheAccessContext {
    public ITokenCache tokenCache();

    public String clientId();

    public IAccount account();

    public boolean hasCacheChanged();
}

