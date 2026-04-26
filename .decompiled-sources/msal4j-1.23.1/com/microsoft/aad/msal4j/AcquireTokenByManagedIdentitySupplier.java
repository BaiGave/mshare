/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AcquireTokenSilentSupplier;
import com.microsoft.aad.msal4j.AuthenticationResult;
import com.microsoft.aad.msal4j.AuthenticationResultMetadata;
import com.microsoft.aad.msal4j.AuthenticationResultSupplier;
import com.microsoft.aad.msal4j.CacheRefreshReason;
import com.microsoft.aad.msal4j.ManagedIdentityApplication;
import com.microsoft.aad.msal4j.ManagedIdentityClient;
import com.microsoft.aad.msal4j.ManagedIdentityParameters;
import com.microsoft.aad.msal4j.ManagedIdentityResponse;
import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.MsalRequest;
import com.microsoft.aad.msal4j.PublicApi;
import com.microsoft.aad.msal4j.RequestContext;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.SilentRequest;
import com.microsoft.aad.msal4j.SilentRequestHelper;
import com.microsoft.aad.msal4j.StringHelper;
import com.microsoft.aad.msal4j.TokenRequestExecutor;
import com.microsoft.aad.msal4j.TokenSource;
import java.time.Instant;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AcquireTokenByManagedIdentitySupplier
extends AuthenticationResultSupplier {
    private static final Logger LOG = LoggerFactory.getLogger(AcquireTokenByManagedIdentitySupplier.class);
    private static final int TWO_HOURS = 7200;
    private ManagedIdentityParameters managedIdentityParameters;

    AcquireTokenByManagedIdentitySupplier(ManagedIdentityApplication managedIdentityApplication, MsalRequest msalRequest) {
        super(managedIdentityApplication, msalRequest);
        this.managedIdentityParameters = (ManagedIdentityParameters)msalRequest.requestContext().apiParameters();
    }

    @Override
    AuthenticationResult execute() throws Exception {
        if (StringHelper.isNullOrBlank(this.managedIdentityParameters.resource)) {
            throw new MsalClientException("resource_required_managed_identity", "At least one scope needs to be requested for this authentication flow. ");
        }
        TokenRequestExecutor tokenRequestExecutor = new TokenRequestExecutor(this.clientApplication.authenticationAuthority, this.msalRequest, this.clientApplication.serviceBundle());
        CacheRefreshReason cacheRefreshReason = CacheRefreshReason.NOT_APPLICABLE;
        if (this.managedIdentityParameters.forceRefresh) {
            LOG.debug("ForceRefresh set to true. Skipping cache lookup and attempting to acquire new token");
            return this.fetchNewAccessTokenAndSaveToCache(tokenRequestExecutor, CacheRefreshReason.FORCE_REFRESH);
        }
        LOG.debug("ForceRefresh set to false. Attempting cache lookup");
        try {
            HashSet<String> scopes = new HashSet<String>();
            scopes.add(this.managedIdentityParameters.resource);
            SilentParameters parameters = SilentParameters.builder(scopes).tenant(this.managedIdentityParameters.tenant()).claims(this.managedIdentityParameters.claims()).build();
            RequestContext context = new RequestContext(this.clientApplication, PublicApi.ACQUIRE_TOKEN_SILENTLY, parameters);
            SilentRequest silentRequest = new SilentRequest(parameters, this.clientApplication, context, null);
            AcquireTokenSilentSupplier supplier = new AcquireTokenSilentSupplier(this.clientApplication, silentRequest);
            AuthenticationResult result = supplier.execute();
            cacheRefreshReason = SilentRequestHelper.getCacheRefreshReasonIfApplicable(parameters, result, LOG);
            if (cacheRefreshReason == CacheRefreshReason.NOT_APPLICABLE) {
                LOG.debug("Returning token from cache");
                result.metadata().tokenSource(TokenSource.CACHE);
                return result;
            }
            if (cacheRefreshReason == CacheRefreshReason.CLAIMS) {
                LOG.debug("Claims are passed, creating token hash and refreshing the token");
                this.managedIdentityParameters.revokedTokenHash = StringHelper.createSha256HashHexString(result.accessToken());
                return this.fetchNewAccessTokenAndSaveToCache(tokenRequestExecutor, CacheRefreshReason.CLAIMS);
            }
            LOG.debug(String.format("Refreshing access token. Cache refresh reason: %s", new Object[]{cacheRefreshReason}));
            return this.fetchNewAccessTokenAndSaveToCache(tokenRequestExecutor, cacheRefreshReason);
        }
        catch (MsalClientException ex) {
            if (ex.errorCode().equals("cache_miss")) {
                LOG.debug(String.format("Cache lookup failed: %s", ex.getMessage()));
                return this.fetchNewAccessTokenAndSaveToCache(tokenRequestExecutor, cacheRefreshReason);
            }
            LOG.error(String.format("Error occurred while cache lookup: %s", ex.getMessage()));
            throw ex;
        }
    }

    private AuthenticationResult fetchNewAccessTokenAndSaveToCache(TokenRequestExecutor tokenRequestExecutor, CacheRefreshReason cacheRefreshReason) throws Exception {
        ManagedIdentityClient managedIdentityClient = new ManagedIdentityClient(this.msalRequest, tokenRequestExecutor.getServiceBundle());
        LOG.debug(String.format("[Managed Identity] Managed Identity source and ID type identified and set successfully, request will use Managed Identity for %s", managedIdentityClient.managedIdentitySource.managedIdentitySourceType.name()));
        ManagedIdentityResponse managedIdentityResponse = managedIdentityClient.getManagedIdentityResponse(this.managedIdentityParameters);
        AuthenticationResult authenticationResult = this.createFromManagedIdentityResponse(managedIdentityResponse);
        this.clientApplication.tokenCache.saveTokens(tokenRequestExecutor, authenticationResult, this.clientApplication.authenticationAuthority.host);
        authenticationResult.metadata().tokenSource(TokenSource.IDENTITY_PROVIDER);
        authenticationResult.metadata().cacheRefreshReason(cacheRefreshReason);
        return authenticationResult;
    }

    private AuthenticationResult createFromManagedIdentityResponse(ManagedIdentityResponse managedIdentityResponse) {
        long expiresOn = AcquireTokenByManagedIdentitySupplier.getExpiresOnFromManagedIdentityTimestamp(managedIdentityResponse.expiresOn);
        long refreshOn = this.calculateRefreshOn(expiresOn);
        AuthenticationResultMetadata metadata = AuthenticationResultMetadata.builder().tokenSource(TokenSource.IDENTITY_PROVIDER).refreshOn(refreshOn).build();
        return AuthenticationResult.builder().accessToken(managedIdentityResponse.getAccessToken()).scopes(this.managedIdentityParameters.resource()).expiresOn(expiresOn).extExpiresOn(0L).refreshOn(refreshOn).metadata(metadata).build();
    }

    static long getExpiresOnFromManagedIdentityTimestamp(String dateTimeStamp) {
        if (dateTimeStamp == null || dateTimeStamp.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(dateTimeStamp);
        }
        catch (NumberFormatException numberFormatException) {
            try {
                return Instant.parse(dateTimeStamp).getEpochSecond();
            }
            catch (Exception exception) {
                throw new MsalClientException(String.format("Failed to parse timestamp '%s'. Expected Unix epoch seconds or ISO 8601 format.", dateTimeStamp), "invalid_timestamp_format");
            }
        }
    }

    private long calculateRefreshOn(long expiresOn) {
        long timestampSeconds = System.currentTimeMillis() / 1000L;
        long expiresIn = expiresOn - timestampSeconds;
        return expiresIn > 7200L ? expiresIn / 2L + timestampSeconds : 0L;
    }
}

