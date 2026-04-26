/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.MsalRequest;
import com.microsoft.aad.msal4j.OAuthAuthorizationGrant;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import com.microsoft.aad.msal4j.RequestContext;
import java.util.LinkedHashMap;

class OnBehalfOfRequest
extends MsalRequest {
    OnBehalfOfParameters parameters;

    OnBehalfOfRequest(OnBehalfOfParameters parameters, ConfidentialClientApplication application, RequestContext requestContext) {
        super(application, OnBehalfOfRequest.createAuthenticationGrant(parameters), requestContext);
        this.parameters = parameters;
    }

    private static OAuthAuthorizationGrant createAuthenticationGrant(OnBehalfOfParameters parameters) {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        params.put("assertion", parameters.userAssertion().getAssertion());
        params.put("requested_token_use", "on_behalf_of");
        if (parameters.claims() != null) {
            params.put("claims", parameters.claims().formatAsJSONString());
        }
        return new OAuthAuthorizationGrant(params, parameters.scopes());
    }

    OnBehalfOfParameters parameters() {
        return this.parameters;
    }
}

