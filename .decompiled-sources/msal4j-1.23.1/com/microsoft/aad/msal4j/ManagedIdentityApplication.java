/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AbstractApplicationBase;
import com.microsoft.aad.msal4j.HttpHelper;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IEnvironmentVariables;
import com.microsoft.aad.msal4j.IManagedIdentityApplication;
import com.microsoft.aad.msal4j.IRetryPolicy;
import com.microsoft.aad.msal4j.ManagedIdentityClient;
import com.microsoft.aad.msal4j.ManagedIdentityId;
import com.microsoft.aad.msal4j.ManagedIdentityIdType;
import com.microsoft.aad.msal4j.ManagedIdentityParameters;
import com.microsoft.aad.msal4j.ManagedIdentityRequest;
import com.microsoft.aad.msal4j.ManagedIdentityRetryPolicy;
import com.microsoft.aad.msal4j.ManagedIdentitySourceType;
import com.microsoft.aad.msal4j.PublicApi;
import com.microsoft.aad.msal4j.RequestContext;
import com.microsoft.aad.msal4j.ServiceBundle;
import com.microsoft.aad.msal4j.TelemetryManager;
import com.microsoft.aad.msal4j.TokenCache;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.LoggerFactory;

public class ManagedIdentityApplication
extends AbstractApplicationBase
implements IManagedIdentityApplication {
    private final ManagedIdentityId managedIdentityId;
    private List<String> clientCapabilities;
    static TokenCache sharedTokenCache = new TokenCache();
    @Deprecated
    ManagedIdentitySourceType managedIdentitySource = ManagedIdentityClient.getManagedIdentitySource();
    static IEnvironmentVariables environmentVariables;

    static void setEnvironmentVariables(IEnvironmentVariables environmentVariables) {
        ManagedIdentityApplication.environmentVariables = environmentVariables;
    }

    private ManagedIdentityApplication(Builder builder) {
        super(builder);
        this.tokenCache = sharedTokenCache;
        this.serviceBundle = new ServiceBundle(builder.executorService, new TelemetryManager(this.telemetryConsumer, builder.onlySendFailureTelemetry), new HttpHelper(this, (IRetryPolicy)new ManagedIdentityRetryPolicy()));
        this.log = LoggerFactory.getLogger(ManagedIdentityApplication.class);
        this.managedIdentityId = builder.managedIdentityId;
        this.tenant = "managed_identity";
        this.clientCapabilities = builder.clientCapabilities;
    }

    public static TokenCache getSharedTokenCache() {
        return sharedTokenCache;
    }

    static IEnvironmentVariables getEnvironmentVariables() {
        return environmentVariables;
    }

    public ManagedIdentityId getManagedIdentityId() {
        return this.managedIdentityId;
    }

    public List<String> getClientCapabilities() {
        return this.clientCapabilities;
    }

    @Override
    public CompletableFuture<IAuthenticationResult> acquireTokenForManagedIdentity(ManagedIdentityParameters managedIdentityParameters) throws Exception {
        RequestContext requestContext = new RequestContext(this, this.managedIdentityId.getIdType() == ManagedIdentityIdType.SYSTEM_ASSIGNED ? PublicApi.ACQUIRE_TOKEN_BY_SYSTEM_ASSIGNED_MANAGED_IDENTITY : PublicApi.ACQUIRE_TOKEN_BY_USER_ASSIGNED_MANAGED_IDENTITY, managedIdentityParameters);
        ManagedIdentityRequest managedIdentityRequest = new ManagedIdentityRequest(this, requestContext);
        return this.executeRequest(managedIdentityRequest);
    }

    public static Builder builder(ManagedIdentityId managedIdentityId) {
        return new Builder(managedIdentityId);
    }

    public static ManagedIdentitySourceType getManagedIdentitySource() {
        return ManagedIdentityClient.getManagedIdentitySource();
    }

    public static class Builder
    extends AbstractApplicationBase.Builder<Builder> {
        private String resource;
        private ManagedIdentityId managedIdentityId;
        private List<String> clientCapabilities;

        private Builder(ManagedIdentityId managedIdentityId) {
            super(managedIdentityId.getIdType() == ManagedIdentityIdType.SYSTEM_ASSIGNED ? "system_assigned_managed_identity" : managedIdentityId.getUserAssignedId());
            this.managedIdentityId = managedIdentityId;
        }

        public Builder resource(String resource) {
            this.resource = resource;
            return this.self();
        }

        public Builder clientCapabilities(List<String> clientCapabilities) {
            this.clientCapabilities = clientCapabilities;
            return this.self();
        }

        @Override
        public ManagedIdentityApplication build() {
            return new ManagedIdentityApplication(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}

