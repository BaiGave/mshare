/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AbstractClientApplicationBase;
import com.microsoft.aad.msal4j.HttpHelper;
import com.microsoft.aad.msal4j.HttpMethod;
import com.microsoft.aad.msal4j.HttpRequest;
import com.microsoft.aad.msal4j.IHttpResponse;
import com.microsoft.aad.msal4j.JsonHelper;
import com.microsoft.aad.msal4j.MsalServiceExceptionFactory;
import com.microsoft.aad.msal4j.OidcAuthority;
import com.microsoft.aad.msal4j.OidcDiscoveryResponse;

class OidcDiscoveryProvider {
    OidcDiscoveryProvider() {
    }

    static OidcDiscoveryResponse performOidcDiscovery(OidcAuthority authority, AbstractClientApplicationBase clientApplication) {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, authority.canonicalAuthorityUrl.toString());
        IHttpResponse httpResponse = ((HttpHelper)clientApplication.serviceBundle.getHttpHelper()).executeHttpRequest(httpRequest);
        OidcDiscoveryResponse response = JsonHelper.convertJsonStringToJsonSerializableObject(httpResponse.body(), OidcDiscoveryResponse::fromJson);
        if (httpResponse.statusCode() != 200) {
            throw MsalServiceExceptionFactory.fromHttpResponse(httpResponse);
        }
        return response;
    }
}

