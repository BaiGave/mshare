/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent.internal;

public abstract class InternalFutureFailureAccess {
    protected InternalFutureFailureAccess() {
    }

    protected abstract Throwable tryInternalFastPathGetFailure();
}

