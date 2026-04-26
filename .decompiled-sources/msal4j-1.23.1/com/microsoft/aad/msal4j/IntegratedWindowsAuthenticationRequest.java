/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AbstractMsalAuthorizationGrant;
import com.microsoft.aad.msal4j.IntegratedWindowsAuthenticationParameters;
import com.microsoft.aad.msal4j.IntegratedWindowsAuthorizationGrant;
import com.microsoft.aad.msal4j.MsalRequest;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.RequestContext;

class IntegratedWindowsAuthenticationRequest
extends MsalRequest {
    IntegratedWindowsAuthenticationRequest(IntegratedWindowsAuthenticationParameters parameters, PublicClientApplication application, RequestContext requestContext) {
        super(application, IntegratedWindowsAuthenticationRequest.createAuthenticationGrant(parameters), requestContext);
    }

    private static AbstractMsalAuthorizationGrant createAuthenticationGrant(IntegratedWindowsAuthenticationParameters parameters) {
        return new IntegratedWindowsAuthorizationGrant(parameters.scopes(), parameters.username(), parameters.claims());
    }
}

