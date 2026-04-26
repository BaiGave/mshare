/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent.internal;

import com.google.common.util.concurrent.internal.InternalFutureFailureAccess;

public final class InternalFutures {
    public static Throwable tryInternalFastPathGetFailure(InternalFutureFailureAccess future) {
        return future.tryInternalFastPathGetFailure();
    }

    private InternalFutures() {
    }
}

