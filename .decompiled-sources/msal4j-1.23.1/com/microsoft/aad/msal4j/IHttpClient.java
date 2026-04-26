/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.HttpRequest;
import com.microsoft.aad.msal4j.IHttpResponse;

public interface IHttpClient {
    public IHttpResponse send(HttpRequest var1) throws Exception;
}

