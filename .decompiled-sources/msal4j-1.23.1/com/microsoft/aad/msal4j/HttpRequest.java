/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.HttpMethod;
import com.microsoft.aad.msal4j.MsalClientException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

public class HttpRequest {
    private HttpMethod httpMethod;
    private URL url;
    private Map<String, String> headers;
    private String body;

    HttpRequest(HttpMethod httpMethod, String url) {
        this.httpMethod = httpMethod;
        this.url = this.createUrlFromString(url);
    }

    HttpRequest(HttpMethod httpMethod, String url, Map<String, String> headers) {
        this.httpMethod = httpMethod;
        this.url = this.createUrlFromString(url);
        this.headers = headers;
    }

    HttpRequest(HttpMethod httpMethod, String url, String body) {
        this.httpMethod = httpMethod;
        this.url = this.createUrlFromString(url);
        this.body = body;
    }

    HttpRequest(HttpMethod httpMethod, String url, Map<String, String> headers, String body) {
        this.httpMethod = httpMethod;
        this.url = this.createUrlFromString(url);
        this.headers = headers;
        this.body = body;
    }

    public String headerValue(String headerName) {
        if (headerName == null || this.headers == null) {
            return null;
        }
        return this.headers.get(headerName);
    }

    private URL createUrlFromString(String stringUrl) {
        URL url;
        try {
            url = new URL(stringUrl);
        }
        catch (MalformedURLException e) {
            throw new MsalClientException(e);
        }
        return url;
    }

    public HttpMethod httpMethod() {
        return this.httpMethod;
    }

    public URL url() {
        return this.url;
    }

    public Map<String, String> headers() {
        return this.headers;
    }

    public String body() {
        return this.body;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HttpRequest)) {
            return false;
        }
        HttpRequest other = (HttpRequest)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (!Objects.equals((Object)this.httpMethod(), (Object)other.httpMethod())) {
            return false;
        }
        if (!Objects.equals(this.url(), other.url())) {
            return false;
        }
        if (!Objects.equals(this.headers(), other.headers())) {
            return false;
        }
        return Objects.equals(this.body(), other.body());
    }

    protected boolean canEqual(Object other) {
        return other instanceof HttpRequest;
    }

    public int hashCode() {
        int result = 1;
        result = result * 59 + (this.httpMethod == null ? 43 : this.httpMethod.hashCode());
        result = result * 59 + (this.url == null ? 43 : this.url.hashCode());
        result = result * 59 + (this.headers == null ? 43 : this.headers.hashCode());
        result = result * 59 + (this.body == null ? 43 : this.body.hashCode());
        return result;
    }
}

