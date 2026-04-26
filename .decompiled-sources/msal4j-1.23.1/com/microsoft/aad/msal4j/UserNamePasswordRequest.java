/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.MsalRequest;
import com.microsoft.aad.msal4j.OAuthAuthorizationGrant;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.RequestContext;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;
import java.util.LinkedHashMap;

class UserNamePasswordRequest
extends MsalRequest {
    UserNamePasswordRequest(UserNamePasswordParameters parameters, PublicClientApplication application, RequestContext requestContext) {
        super(application, UserNamePasswordRequest.createAuthenticationGrant(parameters), requestContext);
    }

    private static OAuthAuthorizationGrant createAuthenticationGrant(UserNamePasswordParameters parameters) {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("grant_type", "password");
        params.put("username", parameters.username());
        params.put("password", new String(parameters.password()));
        return new OAuthAuthorizationGrant(params, parameters.scopes(), parameters.claims());
    }
}

