/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AbstractApplicationBase;
import com.microsoft.aad.msal4j.Authority;
import com.microsoft.aad.msal4j.CacheRefreshReason;
import com.microsoft.aad.msal4j.IUserAssertion;
import com.microsoft.aad.msal4j.MsalRequest;
import com.microsoft.aad.msal4j.RequestContext;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.StringHelper;
import java.net.MalformedURLException;
import java.net.URL;

class SilentRequest
extends MsalRequest {
    private SilentParameters parameters;
    private IUserAssertion assertion;
    private Authority requestAuthority;

    SilentRequest(SilentParameters parameters, AbstractApplicationBase application, RequestContext requestContext, IUserAssertion assertion) throws MalformedURLException {
        super(application, null, requestContext);
        this.parameters = parameters;
        this.assertion = assertion;
        Authority authority = this.requestAuthority = StringHelper.isBlank(parameters.authorityUrl()) ? application.authenticationAuthority : Authority.createAuthority(new URL(Authority.enforceTrailingSlash(parameters.authorityUrl())));
        if (parameters.forceRefresh()) {
            application.serviceBundle().getServerSideTelemetry().getCurrentRequest().cacheInfo(CacheRefreshReason.FORCE_REFRESH);
        }
    }

    SilentParameters parameters() {
        return this.parameters;
    }

    IUserAssertion assertion() {
        return this.assertion;
    }

    Authority requestAuthority() {
        return this.requestAuthority;
    }
}

