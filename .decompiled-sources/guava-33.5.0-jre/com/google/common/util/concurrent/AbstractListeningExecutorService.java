/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ParametricNullness;
import com.google.common.util.concurrent.TrustedListenableFutureTask;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;

@CheckReturnValue
@GwtIncompatible
public abstract class AbstractListeningExecutorService
extends AbstractExecutorService
implements ListeningExecutorService {
    @Override
    @CanIgnoreReturnValue
    protected final <T> RunnableFuture<T> newTaskFor(Runnable runnable, @ParametricNullness T value) {
        return TrustedListenableFutureTask.create(runnable, value);
    }

    @Override
    @CanIgnoreReturnValue
    protected final <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return TrustedListenableFutureTask.create(callable);
    }

    @Override
    @CanIgnoreReturnValue
    public ListenableFuture<?> submit(Runnable task) {
        return (ListenableFuture)super.submit(task);
    }

    @Override
    @CanIgnoreReturnValue
    public <T> ListenableFuture<T> submit(Runnable task, @ParametricNullness T result) {
        return (ListenableFuture)super.submit(task, result);
    }

    @Override
    @CanIgnoreReturnValue
    public <T> ListenableFuture<T> submit(Callable<T> task) {
        return (ListenableFuture)super.submit(task);
    }
}

