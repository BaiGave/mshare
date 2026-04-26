/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AbstractClientApplicationBase;
import com.microsoft.aad.msal4j.AuthorityType;
import com.microsoft.aad.msal4j.DeviceCodeFlowParameters;
import com.microsoft.aad.msal4j.DeviceCodeFlowRequest;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IBroker;
import com.microsoft.aad.msal4j.IPublicClientApplication;
import com.microsoft.aad.msal4j.IntegratedWindowsAuthenticationParameters;
import com.microsoft.aad.msal4j.IntegratedWindowsAuthenticationRequest;
import com.microsoft.aad.msal4j.InteractiveRequest;
import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.ParameterValidationUtils;
import com.microsoft.aad.msal4j.PublicApi;
import com.microsoft.aad.msal4j.RequestContext;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.UserIdentifier;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;
import com.microsoft.aad.msal4j.UserNamePasswordRequest;
import java.net.MalformedURLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.LoggerFactory;

public class PublicClientApplication
extends AbstractClientApplicationBase
implements IPublicClientApplication {
    private IBroker broker;
    private boolean brokerEnabled;

    @Override
    public CompletableFuture<IAuthenticationResult> acquireToken(UserNamePasswordParameters parameters) {
        CompletableFuture<IAuthenticationResult> future;
        ParameterValidationUtils.validateNotNull("parameters", parameters);
        RequestContext context = new RequestContext(this, PublicApi.ACQUIRE_TOKEN_BY_USERNAME_PASSWORD, parameters, UserIdentifier.fromUpn(parameters.username()));
        if (this.validateBrokerUsage(parameters)) {
            future = this.broker.acquireToken(this, parameters);
        } else {
            UserNamePasswordRequest userNamePasswordRequest = new UserNamePasswordRequest(parameters, this, context);
            future = this.executeRequest(userNamePasswordRequest);
        }
        return future;
    }

    @Override
    public CompletableFuture<IAuthenticationResult> acquireToken(IntegratedWindowsAuthenticationParameters parameters) {
        ParameterValidationUtils.validateNotNull("parameters", parameters);
        RequestContext context = new RequestContext(this, PublicApi.ACQUIRE_TOKEN_BY_INTEGRATED_WINDOWS_AUTH, parameters, UserIdentifier.fromUpn(parameters.username()));
        IntegratedWindowsAuthenticationRequest integratedWindowsAuthenticationRequest = new IntegratedWindowsAuthenticationRequest(parameters, this, context);
        return this.executeRequest(integratedWindowsAuthenticationRequest);
    }

    @Override
    public CompletableFuture<IAuthenticationResult> acquireToken(DeviceCodeFlowParameters parameters) {
        if (AuthorityType.B2C.equals((Object)this.authenticationAuthority.authorityType())) {
            throw new IllegalArgumentException("Invalid authority type. Device Flow is not supported by B2C authority.");
        }
        ParameterValidationUtils.validateNotNull("parameters", parameters);
        RequestContext context = new RequestContext(this, PublicApi.ACQUIRE_TOKEN_BY_DEVICE_CODE_FLOW, parameters);
        AtomicReference<CompletableFuture<IAuthenticationResult>> futureReference = new AtomicReference<CompletableFuture<IAuthenticationResult>>();
        DeviceCodeFlowRequest deviceCodeRequest = new DeviceCodeFlowRequest(parameters, futureReference, this, context);
        CompletableFuture<IAuthenticationResult> future = this.executeRequest(deviceCodeRequest);
        futureReference.set(future);
        return future;
    }

    @Override
    public CompletableFuture<IAuthenticationResult> acquireToken(InteractiveRequestParameters parameters) {
        ParameterValidationUtils.validateNotNull("parameters", parameters);
        AtomicReference<CompletableFuture<IAuthenticationResult>> futureReference = new AtomicReference<CompletableFuture<IAuthenticationResult>>();
        RequestContext context = new RequestContext(this, PublicApi.ACQUIRE_TOKEN_INTERACTIVE, parameters, UserIdentifier.fromUpn(parameters.loginHint()));
        InteractiveRequest interactiveRequest = new InteractiveRequest(parameters, futureReference, this, context);
        CompletableFuture<IAuthenticationResult> future = this.validateBrokerUsage(parameters) ? this.broker.acquireToken(this, parameters) : this.executeRequest(interactiveRequest);
        futureReference.set(future);
        return future;
    }

    @Override
    public CompletableFuture<IAuthenticationResult> acquireTokenSilently(SilentParameters parameters) throws MalformedURLException {
        CompletableFuture<IAuthenticationResult> future = this.validateBrokerUsage(parameters) ? this.broker.acquireToken(this, parameters) : super.acquireTokenSilently(parameters);
        return future;
    }

    @Override
    public CompletableFuture<Void> removeAccount(IAccount account) {
        if (this.brokerEnabled) {
            this.broker.removeAccount(this, account);
        }
        return super.removeAccount(account);
    }

    private PublicClientApplication(Builder builder) {
        super(builder);
        ParameterValidationUtils.validateNotBlank("clientId", this.clientId());
        this.log = LoggerFactory.getLogger(PublicClientApplication.class);
        this.broker = builder.broker;
        this.brokerEnabled = builder.brokerEnabled;
        this.tenant = this.authenticationAuthority.tenant;
    }

    public static Builder builder(String clientId) {
        return new Builder(clientId);
    }

    private boolean validateBrokerUsage(InteractiveRequestParameters parameters) {
        if (!this.brokerEnabled && parameters.proofOfPossession() != null) {
            throw new MsalClientException("InteractiveRequestParameters.proofOfPossession should not be used when broker is not available, see https://aka.ms/msal4j-pop for more information", "brokers_package_error");
        }
        return this.brokerEnabled;
    }

    private boolean validateBrokerUsage(UserNamePasswordParameters parameters) {
        if (!this.brokerEnabled && parameters.proofOfPossession() != null) {
            throw new MsalClientException("UserNamePasswordParameters.proofOfPossession should not be used when broker is not available, see https://aka.ms/msal4j-pop for more information", "brokers_package_error");
        }
        return this.brokerEnabled;
    }

    private boolean validateBrokerUsage(SilentParameters parameters) {
        if (!this.brokerEnabled && parameters.proofOfPossession() != null) {
            throw new MsalClientException("UserNamePasswordParameters.proofOfPossession should not be used when broker is not available, see https://aka.ms/msal4j-pop for more information", "brokers_package_error");
        }
        return this.brokerEnabled;
    }

    public static class Builder
    extends AbstractClientApplicationBase.Builder<Builder> {
        private IBroker broker = null;
        private boolean brokerEnabled = false;

        private Builder(String clientId) {
            super(clientId);
        }

        public Builder broker(IBroker val) {
            this.broker = val;
            this.brokerEnabled = this.broker.isBrokerAvailable();
            return this.self();
        }

        @Override
        public PublicClientApplication build() {
            return new PublicClientApplication(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}

