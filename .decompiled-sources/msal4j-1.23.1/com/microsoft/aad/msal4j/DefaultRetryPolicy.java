/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.HttpHelper;
import com.microsoft.aad.msal4j.HttpStatus;
import com.microsoft.aad.msal4j.IHttpResponse;
import com.microsoft.aad.msal4j.IRetryPolicy;

class DefaultRetryPolicy
implements IRetryPolicy {
    private static final int RETRY_NUM = 1;
    private static final int RETRY_DELAY_MS = 1000;

    DefaultRetryPolicy() {
    }

    @Override
    public boolean isRetryable(IHttpResponse httpResponse) {
        return HttpStatus.isServerError(httpResponse.statusCode()) && HttpHelper.getRetryAfterHeader(httpResponse) == null;
    }

    @Override
    public int getMaxRetryCount(IHttpResponse httpResponse) {
        return 1;
    }

    @Override
    public int getRetryDelayMs(IHttpResponse httpResponse) {
        return 1000;
    }
}

