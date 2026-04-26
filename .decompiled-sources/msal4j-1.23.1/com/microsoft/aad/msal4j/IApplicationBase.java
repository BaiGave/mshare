/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.IHttpClient;
import java.net.Proxy;
import javax.net.ssl.SSLSocketFactory;

interface IApplicationBase {
    public static final String DEFAULT_AUTHORITY = "https://login.microsoftonline.com/common/";

    public boolean logPii();

    public String correlationId();

    public IHttpClient httpClient();

    public Proxy proxy();

    public SSLSocketFactory sslSocketFactory();
}

