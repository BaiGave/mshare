/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AuthenticationResult;
import com.microsoft.aad.msal4j.CacheRefreshReason;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.StringHelper;
import java.util.Date;
import org.slf4j.Logger;

class SilentRequestHelper {
    private static final int ACCESS_TOKEN_EXPIRE_BUFFER_IN_SEC = 300;

    private SilentRequestHelper() {
    }

    static CacheRefreshReason getCacheRefreshReasonIfApplicable(SilentParameters parameters, AuthenticationResult cachedResult, Logger log) {
        if (parameters.claims() != null) {
            log.debug(String.format("Refreshing access token. Cache refresh reason: %s", new Object[]{CacheRefreshReason.CLAIMS}));
            return CacheRefreshReason.CLAIMS;
        }
        long currTimeStampSec = new Date().getTime() / 1000L;
        if (!StringHelper.isBlank(cachedResult.accessToken()) && cachedResult.expiresOn() < currTimeStampSec + 300L) {
            log.debug(String.format("Refreshing access token. Cache refresh reason: %s", new Object[]{CacheRefreshReason.EXPIRED}));
            return CacheRefreshReason.EXPIRED;
        }
        if (!StringHelper.isBlank(cachedResult.accessToken()) && cachedResult.refreshOn() != null && cachedResult.refreshOn() > 0L && cachedResult.refreshOn() < currTimeStampSec && cachedResult.expiresOn() >= currTimeStampSec + 300L) {
            log.debug(String.format("Refreshing access token. Cache refresh reason: %s", new Object[]{CacheRefreshReason.PROACTIVE_REFRESH}));
            return CacheRefreshReason.PROACTIVE_REFRESH;
        }
        if (StringHelper.isBlank(cachedResult.accessToken()) && !StringHelper.isBlank(cachedResult.refreshToken())) {
            log.debug(String.format("Refreshing access token. Cache refresh reason: %s", new Object[]{CacheRefreshReason.NO_CACHED_ACCESS_TOKEN}));
            return CacheRefreshReason.NO_CACHED_ACCESS_TOKEN;
        }
        return CacheRefreshReason.NOT_APPLICABLE;
    }
}

