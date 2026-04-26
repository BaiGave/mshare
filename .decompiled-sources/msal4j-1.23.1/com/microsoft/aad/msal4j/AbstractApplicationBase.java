/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AADAuthority;
import com.microsoft.aad.msal4j.AadInstanceDiscoveryProvider;
import com.microsoft.aad.msal4j.AcquireTokenByAuthorizationGrantSupplier;
import com.microsoft.aad.msal4j.AcquireTokenByClientCredentialSupplier;
import com.microsoft.aad.msal4j.AcquireTokenByDeviceCodeFlowSupplier;
import com.microsoft.aad.msal4j.AcquireTokenByInteractiveFlowSupplier;
import com.microsoft.aad.msal4j.AcquireTokenByManagedIdentitySupplier;
import com.microsoft.aad.msal4j.AcquireTokenByOnBehalfOfSupplier;
import com.microsoft.aad.msal4j.AcquireTokenSilentSupplier;
import com.microsoft.aad.msal4j.AuthenticationResult;
import com.microsoft.aad.msal4j.AuthenticationResultSupplier;
import com.microsoft.aad.msal4j.Authority;
import com.microsoft.aad.msal4j.AuthorityType;
import com.microsoft.aad.msal4j.ClientCredentialRequest;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.DefaultHttpClient;
import com.microsoft.aad.msal4j.DeviceCodeFlowRequest;
import com.microsoft.aad.msal4j.HttpHeaders;
import com.microsoft.aad.msal4j.IApplicationBase;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IHttpClient;
import com.microsoft.aad.msal4j.InstanceDiscoveryMetadataEntry;
import com.microsoft.aad.msal4j.InteractiveRequest;
import com.microsoft.aad.msal4j.LogHelper;
import com.microsoft.aad.msal4j.ManagedIdentityApplication;
import com.microsoft.aad.msal4j.ManagedIdentityRequest;
import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.MsalRequest;
import com.microsoft.aad.msal4j.OnBehalfOfRequest;
import com.microsoft.aad.msal4j.ParameterValidationUtils;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.ServiceBundle;
import com.microsoft.aad.msal4j.SilentRequest;
import com.microsoft.aad.msal4j.TokenCache;
import com.microsoft.aad.msal4j.TokenRequestExecutor;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import javax.net.ssl.SSLSocketFactory;
import org.slf4j.Logger;

public abstract class AbstractApplicationBase
implements IApplicationBase {
    protected Logger log;
    protected Authority authenticationAuthority;
    private String correlationId;
    private boolean logPii;
    private Proxy proxy;
    private SSLSocketFactory sslSocketFactory;
    private IHttpClient httpClient;
    private Integer connectTimeoutForDefaultHttpClient;
    private Integer readTimeoutForDefaultHttpClient;
    private boolean retryDisabled;
    String tenant;
    private boolean validateAuthority;
    private String clientId;
    private String authority;
    ServiceBundle serviceBundle;
    Consumer<List<HashMap<String, String>>> telemetryConsumer;
    protected TokenCache tokenCache;

    CompletableFuture<IAuthenticationResult> executeRequest(MsalRequest msalRequest) {
        AuthenticationResultSupplier supplier = this.getAuthenticationResultSupplier(msalRequest);
        ExecutorService executorService = this.serviceBundle.getExecutorService();
        return executorService != null ? CompletableFuture.supplyAsync(supplier, executorService) : CompletableFuture.supplyAsync(supplier);
    }

    AuthenticationResult acquireTokenCommon(MsalRequest msalRequest, Authority requestAuthority) throws Exception {
        HttpHeaders headers = msalRequest.headers();
        if (this.logPii) {
            this.log.debug(LogHelper.createMessage(String.format("Using Client Http Headers: %s", headers), headers.getHeaderCorrelationIdValue()));
        }
        TokenRequestExecutor requestExecutor = new TokenRequestExecutor(requestAuthority, msalRequest, this.serviceBundle);
        AuthenticationResult result = requestExecutor.executeTokenRequest();
        if (this.authenticationAuthority.authorityType.equals((Object)AuthorityType.AAD)) {
            InstanceDiscoveryMetadataEntry instanceDiscoveryMetadata = AadInstanceDiscoveryProvider.getMetadataEntry(requestAuthority.canonicalAuthorityUrl(), this.validateAuthority, msalRequest, this.serviceBundle);
            this.tokenCache.saveTokens(requestExecutor, result, instanceDiscoveryMetadata.preferredCache);
        } else {
            this.tokenCache.saveTokens(requestExecutor, result, this.authenticationAuthority.host);
        }
        return result;
    }

    private AuthenticationResultSupplier getAuthenticationResultSupplier(MsalRequest msalRequest) {
        AuthenticationResultSupplier supplier = msalRequest instanceof DeviceCodeFlowRequest ? new AcquireTokenByDeviceCodeFlowSupplier((PublicClientApplication)this, (DeviceCodeFlowRequest)msalRequest) : (msalRequest instanceof SilentRequest ? new AcquireTokenSilentSupplier(this, (SilentRequest)msalRequest) : (msalRequest instanceof InteractiveRequest ? new AcquireTokenByInteractiveFlowSupplier((PublicClientApplication)this, (InteractiveRequest)msalRequest) : (msalRequest instanceof ClientCredentialRequest ? new AcquireTokenByClientCredentialSupplier((ConfidentialClientApplication)this, (ClientCredentialRequest)msalRequest) : (msalRequest instanceof OnBehalfOfRequest ? new AcquireTokenByOnBehalfOfSupplier((ConfidentialClientApplication)this, (OnBehalfOfRequest)msalRequest) : (msalRequest instanceof ManagedIdentityRequest ? new AcquireTokenByManagedIdentitySupplier((ManagedIdentityApplication)this, (MsalRequest)((ManagedIdentityRequest)msalRequest)) : new AcquireTokenByAuthorizationGrantSupplier(this, msalRequest, null))))));
        return supplier;
    }

    @Override
    public String correlationId() {
        return this.correlationId;
    }

    @Override
    public boolean logPii() {
        return this.logPii;
    }

    @Override
    public Proxy proxy() {
        return this.proxy;
    }

    @Override
    public SSLSocketFactory sslSocketFactory() {
        return this.sslSocketFactory;
    }

    @Override
    public IHttpClient httpClient() {
        return this.httpClient;
    }

    public Integer connectTimeoutForDefaultHttpClient() {
        return this.connectTimeoutForDefaultHttpClient;
    }

    public Integer readTimeoutForDefaultHttpClient() {
        return this.readTimeoutForDefaultHttpClient;
    }

    boolean isRetryDisabled() {
        return this.retryDisabled;
    }

    String tenant() {
        return this.tenant;
    }

    boolean validateAuthority() {
        return this.validateAuthority;
    }

    String clientId() {
        return this.clientId;
    }

    String authority() {
        return this.authority;
    }

    ServiceBundle serviceBundle() {
        return this.serviceBundle;
    }

    Consumer<List<HashMap<String, String>>> telemetryConsumer() {
        return this.telemetryConsumer;
    }

    TokenCache tokenCache() {
        return this.tokenCache;
    }

    AbstractApplicationBase(Builder<?> builder) {
        this.correlationId = ((Builder)builder).correlationId;
        this.logPii = ((Builder)builder).logPii;
        this.telemetryConsumer = ((Builder)builder).telemetryConsumer;
        this.proxy = builder.proxy;
        this.sslSocketFactory = builder.sslSocketFactory;
        this.connectTimeoutForDefaultHttpClient = builder.connectTimeoutForDefaultHttpClient;
        this.readTimeoutForDefaultHttpClient = builder.readTimeoutForDefaultHttpClient;
        this.authenticationAuthority = ((Builder)builder).authenticationAuthority;
        this.clientId = ((Builder)builder).clientId;
        this.retryDisabled = builder.disableInternalRetries;
        this.httpClient = builder.httpClient == null ? new DefaultHttpClient(builder.proxy, builder.sslSocketFactory, builder.connectTimeoutForDefaultHttpClient, builder.readTimeoutForDefaultHttpClient) : builder.httpClient;
    }

    public static abstract class Builder<T extends Builder<T>> {
        private String correlationId;
        private boolean logPii = false;
        ExecutorService executorService;
        Proxy proxy;
        SSLSocketFactory sslSocketFactory;
        IHttpClient httpClient;
        private Consumer<List<HashMap<String, String>>> telemetryConsumer;
        Boolean onlySendFailureTelemetry = false;
        Integer connectTimeoutForDefaultHttpClient;
        Integer readTimeoutForDefaultHttpClient;
        boolean disableInternalRetries;
        private String clientId;
        private Authority authenticationAuthority = Builder.createDefaultAADAuthority();

        public Builder() {
        }

        public Builder(String clientId) {
            ParameterValidationUtils.validateNotBlank("clientId", clientId);
            this.clientId = clientId;
        }

        abstract T self();

        public T correlationId(String val) {
            ParameterValidationUtils.validateNotBlank("correlationId", val);
            this.correlationId = val;
            return this.self();
        }

        public T logPii(boolean val) {
            this.logPii = val;
            return this.self();
        }

        public T executorService(ExecutorService val) {
            ParameterValidationUtils.validateNotNull("executorService", val);
            this.executorService = val;
            return this.self();
        }

        public T proxy(Proxy val) {
            ParameterValidationUtils.validateNotNull("proxy", val);
            this.proxy = val;
            return this.self();
        }

        public T httpClient(IHttpClient val) {
            ParameterValidationUtils.validateNotNull("httpClient", val);
            this.httpClient = val;
            return this.self();
        }

        public T sslSocketFactory(SSLSocketFactory val) {
            ParameterValidationUtils.validateNotNull("sslSocketFactory", val);
            this.sslSocketFactory = val;
            return this.self();
        }

        public T connectTimeoutForDefaultHttpClient(Integer val) {
            ParameterValidationUtils.validateNotNull("connectTimeoutForDefaultHttpClient", val);
            this.connectTimeoutForDefaultHttpClient = val;
            return this.self();
        }

        public T readTimeoutForDefaultHttpClient(Integer val) {
            ParameterValidationUtils.validateNotNull("readTimeoutForDefaultHttpClient", val);
            this.readTimeoutForDefaultHttpClient = val;
            return this.self();
        }

        public T disableInternalRetries() {
            this.disableInternalRetries = true;
            return this.self();
        }

        T telemetryConsumer(Consumer<List<HashMap<String, String>>> val) {
            ParameterValidationUtils.validateNotNull("telemetryConsumer", val);
            this.telemetryConsumer = val;
            return this.self();
        }

        T onlySendFailureTelemetry(Boolean val) {
            this.onlySendFailureTelemetry = val;
            return this.self();
        }

        private static Authority createDefaultAADAuthority() {
            AADAuthority authority;
            try {
                authority = new AADAuthority(new URL("https://login.microsoftonline.com/common/"));
            }
            catch (Exception e) {
                throw new MsalClientException(e);
            }
            return authority;
        }

        abstract AbstractApplicationBase build();
    }
}

