/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.HttpMethod;
import com.microsoft.aad.msal4j.HttpRequest;
import com.microsoft.aad.msal4j.IHttpResponse;
import com.microsoft.aad.msal4j.JsonHelper;
import com.microsoft.aad.msal4j.MsalServiceExceptionFactory;
import com.microsoft.aad.msal4j.RequestContext;
import com.microsoft.aad.msal4j.ServiceBundle;
import com.microsoft.aad.msal4j.UserDiscoveryResponse;
import java.util.HashMap;
import java.util.Map;

class UserDiscoveryRequest {
    private static final Map<String, String> HEADERS = new HashMap<String, String>();

    private UserDiscoveryRequest() {
    }

    static UserDiscoveryResponse execute(String uri, Map<String, String> clientDataHeaders, RequestContext requestContext, ServiceBundle serviceBundle) {
        HashMap<String, String> headers = new HashMap<String, String>(HEADERS);
        headers.putAll(clientDataHeaders);
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, uri, headers);
        IHttpResponse response = serviceBundle.getHttpHelper().executeHttpRequest(httpRequest, requestContext, serviceBundle);
        if (response.statusCode() != 200) {
            throw MsalServiceExceptionFactory.fromHttpResponse(response);
        }
        return JsonHelper.convertJsonStringToJsonSerializableObject(response.body(), UserDiscoveryResponse::fromJson);
    }

    static {
        HEADERS.put("Accept", "application/json, text/javascript, */*");
    }
}

