/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AppTokenProviderParameters;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.MsalRequest;
import com.microsoft.aad.msal4j.OAuthAuthorizationGrant;
import com.microsoft.aad.msal4j.RequestContext;
import com.microsoft.aad.msal4j.TokenProviderResult;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

class ClientCredentialRequest
extends MsalRequest {
    ClientCredentialParameters parameters;
    Function<AppTokenProviderParameters, CompletableFuture<TokenProviderResult>> appTokenProvider;

    ClientCredentialRequest(ClientCredentialParameters parameters, ConfidentialClientApplication application, RequestContext requestContext, Function<AppTokenProviderParameters, CompletableFuture<TokenProviderResult>> appTokenProvider) {
        super(application, ClientCredentialRequest.createMsalGrant(parameters), requestContext);
        this.parameters = parameters;
        this.appTokenProvider = appTokenProvider;
    }

    private static OAuthAuthorizationGrant createMsalGrant(ClientCredentialParameters parameters) {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("grant_type", "client_credentials");
        return new OAuthAuthorizationGrant(params, parameters.scopes(), parameters.claims());
    }
}

