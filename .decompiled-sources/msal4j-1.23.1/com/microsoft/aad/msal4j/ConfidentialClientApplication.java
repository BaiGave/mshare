/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AbstractClientApplicationBase;
import com.microsoft.aad.msal4j.AppTokenProviderParameters;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ClientCredentialRequest;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.IConfidentialClientApplication;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import com.microsoft.aad.msal4j.OnBehalfOfRequest;
import com.microsoft.aad.msal4j.ParameterValidationUtils;
import com.microsoft.aad.msal4j.PublicApi;
import com.microsoft.aad.msal4j.RequestContext;
import com.microsoft.aad.msal4j.TokenProviderResult;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.slf4j.LoggerFactory;

public class ConfidentialClientApplication
extends AbstractClientApplicationBase
implements IConfidentialClientApplication {
    IClientCredential clientCredential;
    private boolean sendX5c;
    public Function<AppTokenProviderParameters, CompletableFuture<TokenProviderResult>> appTokenProvider;

    @Override
    public CompletableFuture<IAuthenticationResult> acquireToken(ClientCredentialParameters parameters) {
        ParameterValidationUtils.validateNotNull("parameters", parameters);
        RequestContext context = new RequestContext(this, PublicApi.ACQUIRE_TOKEN_FOR_CLIENT, parameters);
        ClientCredentialRequest clientCredentialRequest = new ClientCredentialRequest(parameters, this, context, this.appTokenProvider);
        return this.executeRequest(clientCredentialRequest);
    }

    @Override
    public CompletableFuture<IAuthenticationResult> acquireToken(OnBehalfOfParameters parameters) {
        ParameterValidationUtils.validateNotNull("parameters", parameters);
        RequestContext context = new RequestContext(this, PublicApi.ACQUIRE_TOKEN_ON_BEHALF_OF, parameters);
        OnBehalfOfRequest oboRequest = new OnBehalfOfRequest(parameters, this, context);
        return this.executeRequest(oboRequest);
    }

    private ConfidentialClientApplication(Builder builder) {
        super(builder);
        this.sendX5c = builder.sendX5c;
        this.appTokenProvider = builder.appTokenProvider;
        this.log = LoggerFactory.getLogger(ConfidentialClientApplication.class);
        this.clientCredential = builder.clientCredential;
        this.tenant = this.authenticationAuthority.tenant;
    }

    public static Builder builder(String clientId, IClientCredential clientCredential) {
        return new Builder(clientId, clientCredential);
    }

    @Override
    public boolean sendX5c() {
        return this.sendX5c;
    }

    public static class Builder
    extends AbstractClientApplicationBase.Builder<Builder> {
        private IClientCredential clientCredential;
        private boolean sendX5c = true;
        private Function<AppTokenProviderParameters, CompletableFuture<TokenProviderResult>> appTokenProvider;

        private Builder(String clientId, IClientCredential clientCredential) {
            super(clientId);
            ParameterValidationUtils.validateNotNull("clientCredential", clientCredential);
            this.clientCredential = clientCredential;
        }

        public Builder sendX5c(boolean val) {
            this.sendX5c = val;
            return this.self();
        }

        public Builder appTokenProvider(Function<AppTokenProviderParameters, CompletableFuture<TokenProviderResult>> appTokenProvider) {
            if (appTokenProvider != null) {
                this.appTokenProvider = appTokenProvider;
                return this.self();
            }
            throw new NullPointerException("appTokenProvider is null");
        }

        @Override
        public ConfidentialClientApplication build() {
            return new ConfidentialClientApplication(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}

