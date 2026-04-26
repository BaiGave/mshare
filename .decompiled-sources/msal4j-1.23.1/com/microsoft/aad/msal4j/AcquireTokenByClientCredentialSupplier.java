/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AbstractClientApplicationBase;
import com.microsoft.aad.msal4j.AcquireTokenByAppProviderSupplier;
import com.microsoft.aad.msal4j.AcquireTokenByAuthorizationGrantSupplier;
import com.microsoft.aad.msal4j.AcquireTokenSilentSupplier;
import com.microsoft.aad.msal4j.AppTokenProviderParameters;
import com.microsoft.aad.msal4j.AuthenticationResult;
import com.microsoft.aad.msal4j.AuthenticationResultSupplier;
import com.microsoft.aad.msal4j.ClientCredentialRequest;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.PublicApi;
import com.microsoft.aad.msal4j.RequestContext;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.SilentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AcquireTokenByClientCredentialSupplier
extends AuthenticationResultSupplier {
    private static final Logger LOG = LoggerFactory.getLogger(AcquireTokenByClientCredentialSupplier.class);
    private ClientCredentialRequest clientCredentialRequest;

    AcquireTokenByClientCredentialSupplier(ConfidentialClientApplication clientApplication, ClientCredentialRequest clientCredentialRequest) {
        super(clientApplication, clientCredentialRequest);
        this.clientCredentialRequest = clientCredentialRequest;
    }

    @Override
    AuthenticationResult execute() throws Exception {
        if (this.clientCredentialRequest.parameters.skipCache() != null && !this.clientCredentialRequest.parameters.skipCache().booleanValue()) {
            LOG.debug("SkipCache set to false. Attempting cache lookup");
            try {
                SilentParameters parameters = SilentParameters.builder(this.clientCredentialRequest.parameters.scopes()).claims(this.clientCredentialRequest.parameters.claims()).tenant(this.clientCredentialRequest.parameters.tenant()).build();
                RequestContext context = new RequestContext(this.clientApplication, PublicApi.ACQUIRE_TOKEN_SILENTLY, parameters);
                SilentRequest silentRequest = new SilentRequest(parameters, this.clientApplication, context, null);
                AcquireTokenSilentSupplier supplier = new AcquireTokenSilentSupplier(this.clientApplication, silentRequest);
                return supplier.execute();
            }
            catch (MsalClientException ex) {
                LOG.debug(String.format("Cache lookup failed: %s", ex.getMessage()));
                return this.acquireTokenByClientCredential();
            }
        }
        LOG.debug("SkipCache set to true. Skipping cache lookup and attempting client credentials request");
        return this.acquireTokenByClientCredential();
    }

    private AuthenticationResult acquireTokenByClientCredential() throws Exception {
        if (this.clientCredentialRequest.appTokenProvider != null) {
            String claims = "";
            if (null != this.clientCredentialRequest.parameters.claims()) {
                claims = this.clientCredentialRequest.parameters.claims().toString();
            }
            AppTokenProviderParameters appTokenProviderParameters = new AppTokenProviderParameters(this.clientCredentialRequest.parameters.scopes(), this.clientCredentialRequest.requestContext().correlationId(), claims, this.clientCredentialRequest.parameters.tenant());
            AcquireTokenByAppProviderSupplier supplier = new AcquireTokenByAppProviderSupplier((AbstractClientApplicationBase)this.clientApplication, this.clientCredentialRequest, appTokenProviderParameters);
            return supplier.execute();
        }
        AcquireTokenByAuthorizationGrantSupplier supplier = new AcquireTokenByAuthorizationGrantSupplier(this.clientApplication, this.clientCredentialRequest, null);
        return supplier.execute();
    }
}

