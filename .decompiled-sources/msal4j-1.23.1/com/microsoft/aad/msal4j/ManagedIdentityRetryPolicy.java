/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.IHttpResponse;
import com.microsoft.aad.msal4j.IRetryPolicy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class ManagedIdentityRetryPolicy
implements IRetryPolicy {
    private static final int RETRY_NUM = 3;
    private static final int RETRY_DELAY_MS = 1000;
    private static int currentRetryDelayMs = 1000;
    private static final Set<Integer> RETRYABLE_STATUS_CODES = Collections.unmodifiableSet(new HashSet<Integer>(Arrays.asList(404, 408, 429, 500, 503, 504)));

    ManagedIdentityRetryPolicy() {
    }

    @Override
    public boolean isRetryable(IHttpResponse httpResponse) {
        return RETRYABLE_STATUS_CODES.contains(httpResponse.statusCode());
    }

    @Override
    public int getMaxRetryCount(IHttpResponse httpResponse) {
        return 3;
    }

    @Override
    public int getRetryDelayMs(IHttpResponse httpResponse) {
        return currentRetryDelayMs;
    }

    static void setRetryDelayMs(int retryDelayMs) {
        currentRetryDelayMs = retryDelayMs;
    }

    static void resetToDefaults() {
        currentRetryDelayMs = 1000;
    }
}

