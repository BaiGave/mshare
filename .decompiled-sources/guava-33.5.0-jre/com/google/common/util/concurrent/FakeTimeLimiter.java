/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.ParametricNullness;
import com.google.common.util.concurrent.Platform;
import com.google.common.util.concurrent.TimeLimiter;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@J2ktIncompatible
@GwtIncompatible
public final class FakeTimeLimiter
implements TimeLimiter {
    @Override
    @CanIgnoreReturnValue
    public <T> T newProxy(T target, Class<T> interfaceType, long timeoutDuration, TimeUnit timeoutUnit) {
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(interfaceType);
        Preconditions.checkNotNull(timeoutUnit);
        return target;
    }

    @Override
    @CanIgnoreReturnValue
    @ParametricNullness
    public <T> T callWithTimeout(Callable<T> callable, long timeoutDuration, TimeUnit timeoutUnit) throws ExecutionException {
        Preconditions.checkNotNull(callable);
        Preconditions.checkNotNull(timeoutUnit);
        try {
            return callable.call();
        }
        catch (RuntimeException e) {
            throw new UncheckedExecutionException(e);
        }
        catch (Exception e) {
            Platform.restoreInterruptIfIsInterruptedException(e);
            throw new ExecutionException(e);
        }
        catch (Error e) {
            throw new ExecutionError(e);
        }
    }

    @Override
    @CanIgnoreReturnValue
    @ParametricNullness
    public <T> T callUninterruptiblyWithTimeout(Callable<T> callable, long timeoutDuration, TimeUnit timeoutUnit) throws ExecutionException {
        return this.callWithTimeout(callable, timeoutDuration, timeoutUnit);
    }

    @Override
    public void runWithTimeout(Runnable runnable, long timeoutDuration, TimeUnit timeoutUnit) {
        Preconditions.checkNotNull(runnable);
        Preconditions.checkNotNull(timeoutUnit);
        try {
            runnable.run();
        }
        catch (Exception e) {
            throw new UncheckedExecutionException(e);
        }
        catch (Error e) {
            throw new ExecutionError(e);
        }
    }

    @Override
    public void runUninterruptiblyWithTimeout(Runnable runnable, long timeoutDuration, TimeUnit timeoutUnit) {
        this.runWithTimeout(runnable, timeoutDuration, timeoutUnit);
    }
}

