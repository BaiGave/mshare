/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.HttpRequest;
import com.microsoft.aad.msal4j.IHttpResponse;
import com.microsoft.aad.msal4j.RequestContext;
import com.microsoft.aad.msal4j.ServiceBundle;

interface IHttpHelper {
    public IHttpResponse executeHttpRequest(HttpRequest var1, RequestContext var2, ServiceBundle var3);
}

