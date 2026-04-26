/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ParametricNullness;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@GwtCompatible
final class ForwardingFluentFuture<V>
extends FluentFuture<V> {
    private final ListenableFuture<V> delegate;

    ForwardingFluentFuture(ListenableFuture<V> delegate) {
        this.delegate = Preconditions.checkNotNull(delegate);
    }

    @Override
    public void addListener(Runnable listener, Executor executor) {
        this.delegate.addListener(listener, executor);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return this.delegate.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return this.delegate.isCancelled();
    }

    @Override
    public boolean isDone() {
        return this.delegate.isDone();
    }

    @Override
    @ParametricNullness
    public V get() throws InterruptedException, ExecutionException {
        return this.delegate.get();
    }

    @Override
    @ParametricNullness
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.delegate.get(timeout, unit);
    }

    @Override
    public String toString() {
        return this.delegate.toString();
    }
}

