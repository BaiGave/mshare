/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import java.util.List;
import java.util.Map;

public interface IHttpResponse {
    public int statusCode();

    public Map<String, List<String>> headers();

    public String body();
}

