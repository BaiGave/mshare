/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.ITokenCacheAccessContext;

public interface ITokenCacheAccessAspect {
    public void beforeCacheAccess(ITokenCacheAccessContext var1);

    public void afterCacheAccess(ITokenCacheAccessContext var1);
}

