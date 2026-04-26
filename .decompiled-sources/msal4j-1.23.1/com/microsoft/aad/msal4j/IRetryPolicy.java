/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.IHttpResponse;

interface IRetryPolicy {
    public boolean isRetryable(IHttpResponse var1);

    public int getMaxRetryCount(IHttpResponse var1);

    public int getRetryDelayMs(IHttpResponse var1);
}

