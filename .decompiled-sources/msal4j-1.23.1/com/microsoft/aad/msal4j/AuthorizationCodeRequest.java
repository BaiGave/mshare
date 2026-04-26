/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AbstractClientApplicationBase;
import com.microsoft.aad.msal4j.AbstractMsalAuthorizationGrant;
import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.MsalRequest;
import com.microsoft.aad.msal4j.OAuthAuthorizationGrant;
import com.microsoft.aad.msal4j.RequestContext;
import java.util.LinkedHashMap;

class AuthorizationCodeRequest
extends MsalRequest {
    AuthorizationCodeRequest(AuthorizationCodeParameters parameters, AbstractClientApplicationBase application, RequestContext requestContext) {
        super(application, AuthorizationCodeRequest.createMsalGrant(parameters), requestContext);
    }

    private static AbstractMsalAuthorizationGrant createMsalGrant(AuthorizationCodeParameters parameters) {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("grant_type", "authorization_code");
        params.put("code", parameters.authorizationCode());
        if (parameters.redirectUri() != null) {
            params.put("redirect_uri", parameters.redirectUri().toString());
        }
        if (parameters.codeVerifier() != null) {
            params.put("code_verifier", parameters.codeVerifier());
        }
        return new OAuthAuthorizationGrant(params, parameters.scopes(), parameters.claims());
    }
}

