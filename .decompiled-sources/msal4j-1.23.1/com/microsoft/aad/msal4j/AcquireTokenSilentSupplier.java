/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AbstractApplicationBase;
import com.microsoft.aad.msal4j.AcquireTokenByAuthorizationGrantSupplier;
import com.microsoft.aad.msal4j.AuthenticationResult;
import com.microsoft.aad.msal4j.AuthenticationResultSupplier;
import com.microsoft.aad.msal4j.Authority;
import com.microsoft.aad.msal4j.CacheRefreshReason;
import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.MsalServiceException;
import com.microsoft.aad.msal4j.RefreshTokenParameters;
import com.microsoft.aad.msal4j.RefreshTokenRequest;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.SilentRequest;
import com.microsoft.aad.msal4j.StringHelper;
import com.microsoft.aad.msal4j.TokenSource;
import java.net.URL;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AcquireTokenSilentSupplier
extends AuthenticationResultSupplier {
    private static final Logger log = LoggerFactory.getLogger(AcquireTokenSilentSupplier.class);
    private SilentRequest silentRequest;
    protected static final int ACCESS_TOKEN_EXPIRE_BUFFER_IN_SEC = 300;

    AcquireTokenSilentSupplier(AbstractApplicationBase clientApplication, SilentRequest silentRequest) {
        super(clientApplication, silentRequest);
        this.silentRequest = silentRequest;
    }

    @Override
    AuthenticationResult execute() throws Exception {
        AuthenticationResult res;
        Authority requestAuthority = this.getAuthorityWithPrefNetworkHost(this.silentRequest.requestAuthority().authority());
        if (this.silentRequest.parameters().account() == null) {
            res = this.clientApplication.tokenCache.getCachedAuthenticationResult(requestAuthority, this.silentRequest.parameters().scopes(), this.clientApplication.clientId(), this.silentRequest.assertion());
        } else {
            boolean shouldRefresh;
            res = this.clientApplication.tokenCache.getCachedAuthenticationResult(this.silentRequest.parameters().account(), requestAuthority, this.silentRequest.parameters().scopes(), this.clientApplication.clientId());
            if (res == null) {
                throw new MsalClientException("Token not found in the cache", "cache_miss");
            }
            res.metadata().tokenSource(TokenSource.CACHE);
            if (!StringHelper.isBlank(res.accessToken())) {
                this.clientApplication.serviceBundle().getServerSideTelemetry().incrementSilentSuccessfulCount();
            }
            if (shouldRefresh = this.shouldRefresh(this.silentRequest.parameters(), res)) {
                if (!StringHelper.isBlank(res.refreshToken())) {
                    if (this.silentRequest.parameters().authorityUrl() == null && !res.account().environment().equals(requestAuthority.host)) {
                        requestAuthority = Authority.createAuthority(new URL(requestAuthority.authority().replace(requestAuthority.host(), res.account().environment())));
                    }
                    res = this.makeRefreshRequest(res, requestAuthority, this.clientApplication.serviceBundle().getServerSideTelemetry().getCurrentRequest().cacheInfo());
                } else {
                    res = null;
                }
            }
        }
        if (res == null || StringHelper.isBlank(res.accessToken())) {
            throw new MsalClientException("Token not found in the cache", "cache_miss");
        }
        log.debug("Returning token from cache");
        return res;
    }

    private AuthenticationResult makeRefreshRequest(AuthenticationResult cachedResult, Authority requestAuthority, CacheRefreshReason refreshReason) throws Exception {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(RefreshTokenParameters.builder(this.silentRequest.parameters().scopes(), cachedResult.refreshToken()).build(), this.silentRequest.application(), this.silentRequest.requestContext(), this.silentRequest);
        this.setCacheTelemetry(refreshReason);
        AcquireTokenByAuthorizationGrantSupplier acquireTokenByAuthorisationGrantSupplier = new AcquireTokenByAuthorizationGrantSupplier(this.clientApplication, refreshTokenRequest, requestAuthority);
        try {
            AuthenticationResult refreshedResult = acquireTokenByAuthorisationGrantSupplier.execute();
            refreshedResult.metadata().tokenSource(TokenSource.IDENTITY_PROVIDER);
            refreshedResult.metadata().cacheRefreshReason(refreshReason);
            log.info("Access token refreshed successfully.");
            return refreshedResult;
        }
        catch (MsalServiceException ex) {
            if (refreshReason == CacheRefreshReason.PROACTIVE_REFRESH) {
                return cachedResult;
            }
            throw ex;
        }
    }

    private boolean shouldRefresh(SilentParameters parameters, AuthenticationResult cachedResult) {
        if (parameters.forceRefresh()) {
            this.setCacheTelemetry(CacheRefreshReason.FORCE_REFRESH);
            log.debug(String.format("Refreshing access token. Cache refresh reason: %s", new Object[]{CacheRefreshReason.FORCE_REFRESH}));
            return true;
        }
        if (parameters.claims() != null) {
            this.setCacheTelemetry(CacheRefreshReason.CLAIMS);
            log.debug(String.format("Refreshing access token. Cache refresh reason: %s", new Object[]{CacheRefreshReason.CLAIMS}));
            return true;
        }
        long currTimeStampSec = new Date().getTime() / 1000L;
        if (!StringHelper.isBlank(cachedResult.accessToken()) && cachedResult.expiresOn() < currTimeStampSec + 300L) {
            this.setCacheTelemetry(CacheRefreshReason.EXPIRED);
            log.debug(String.format("Refreshing access token. Cache refresh reason: %s", new Object[]{CacheRefreshReason.EXPIRED}));
            return true;
        }
        if (!StringHelper.isBlank(cachedResult.accessToken()) && cachedResult.refreshOn() != null && cachedResult.refreshOn() > 0L && cachedResult.refreshOn() < currTimeStampSec && cachedResult.expiresOn() >= currTimeStampSec + 300L) {
            this.setCacheTelemetry(CacheRefreshReason.PROACTIVE_REFRESH);
            log.debug(String.format("Refreshing access token. Cache refresh reason: %s", new Object[]{CacheRefreshReason.PROACTIVE_REFRESH}));
            return true;
        }
        if (StringHelper.isBlank(cachedResult.accessToken()) && !StringHelper.isBlank(cachedResult.refreshToken())) {
            this.setCacheTelemetry(CacheRefreshReason.NO_CACHED_ACCESS_TOKEN);
            log.debug(String.format("Refreshing access token. Cache refresh reason: %s", new Object[]{CacheRefreshReason.NO_CACHED_ACCESS_TOKEN}));
            return true;
        }
        return false;
    }

    private void setCacheTelemetry(CacheRefreshReason cacheInfoValue) {
        this.clientApplication.serviceBundle().getServerSideTelemetry().getCurrentRequest().cacheInfo(cacheInfoValue);
    }
}

