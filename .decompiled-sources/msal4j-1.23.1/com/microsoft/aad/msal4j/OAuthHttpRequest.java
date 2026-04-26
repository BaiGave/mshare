/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.HTTPContentType;
import com.microsoft.aad.msal4j.HttpMethod;
import com.microsoft.aad.msal4j.HttpRequest;
import com.microsoft.aad.msal4j.HttpResponse;
import com.microsoft.aad.msal4j.HttpUtils;
import com.microsoft.aad.msal4j.IHttpResponse;
import com.microsoft.aad.msal4j.RequestContext;
import com.microsoft.aad.msal4j.ServiceBundle;
import com.microsoft.aad.msal4j.StringHelper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class OAuthHttpRequest {
    final HttpMethod method;
    final URL url;
    String query;
    private final Map<String, String> extraHeaderParams;
    private final ServiceBundle serviceBundle;
    private final RequestContext requestContext;

    OAuthHttpRequest(HttpMethod method, URL url, Map<String, String> extraHeaderParams, RequestContext requestContext, ServiceBundle serviceBundle) {
        this.method = method;
        this.url = url;
        this.extraHeaderParams = extraHeaderParams;
        this.requestContext = requestContext;
        this.serviceBundle = serviceBundle;
    }

    public HttpResponse send() throws IOException {
        Map<String, String> httpHeaders = this.configureHttpHeaders();
        HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, this.url.toString(), httpHeaders, this.query);
        IHttpResponse httpResponse = this.serviceBundle.getHttpHelper().executeHttpRequest(httpRequest, this.requestContext, this.serviceBundle);
        return this.createOauthHttpResponseFromHttpResponse(httpResponse);
    }

    private Map<String, String> configureHttpHeaders() {
        HashMap<String, String> httpHeaders = new HashMap<String, String>(this.extraHeaderParams);
        httpHeaders.put("Content-Type", HTTPContentType.ApplicationURLEncoded.contentType);
        Map<String, String> telemetryHeaders = this.serviceBundle.getServerSideTelemetry().getServerTelemetryHeaderMap();
        httpHeaders.putAll(telemetryHeaders);
        return httpHeaders;
    }

    private HttpResponse createOauthHttpResponseFromHttpResponse(IHttpResponse httpResponse) throws IOException {
        String contentType;
        HttpResponse response = new HttpResponse();
        response.statusCode(httpResponse.statusCode());
        String location = HttpUtils.headerValue(httpResponse.headers(), "Location");
        if (!StringHelper.isBlank(location)) {
            try {
                response.addHeader("Location", new URI(location).toString());
            }
            catch (URISyntaxException e) {
                throw new IOException("Invalid location URI " + location, e);
            }
        }
        if (!StringHelper.isBlank(contentType = HttpUtils.headerValue(httpResponse.headers(), "Content-Type"))) {
            response.addHeader("Content-Type", contentType);
        }
        Map<String, List<String>> headers = httpResponse.headers();
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            List<String> headerValue;
            if (StringHelper.isBlank(header.getKey()) || (headerValue = response.getHeader(header.getKey())) != null) continue;
            response.addHeader(header.getKey(), header.getValue().toArray(new String[0]));
        }
        if (!StringHelper.isBlank(httpResponse.body())) {
            response.body(httpResponse.body());
        }
        return response;
    }

    void setQuery(String query) {
        this.query = query;
    }

    Map<String, String> getExtraHeaderParams() {
        return this.extraHeaderParams;
    }
}

