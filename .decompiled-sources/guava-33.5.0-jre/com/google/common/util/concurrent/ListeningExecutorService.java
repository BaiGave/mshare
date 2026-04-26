/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.util.concurrent.Internal;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ParametricNullness;
import com.google.errorprone.annotations.DoNotMock;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@DoNotMock(value="Use TestingExecutors.sameThreadScheduledExecutor, or wrap a real Executor from java.util.concurrent.Executors with MoreExecutors.listeningDecorator")
@GwtIncompatible
public interface ListeningExecutorService
extends ExecutorService {
    public <T> ListenableFuture<T> submit(Callable<T> var1);

    public ListenableFuture<?> submit(Runnable var1);

    public <T> ListenableFuture<T> submit(Runnable var1, @ParametricNullness T var2);

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> var1) throws InterruptedException;

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> var1, long var2, TimeUnit var4) throws InterruptedException;

    @J2ktIncompatible
    default public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, Duration timeout) throws InterruptedException {
        return this.invokeAll(tasks, Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
    }

    @J2ktIncompatible
    default public <T> T invokeAny(Collection<? extends Callable<T>> tasks, Duration timeout) throws InterruptedException, ExecutionException, TimeoutException {
        return this.invokeAny(tasks, Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
    }

    @J2ktIncompatible
    default public boolean awaitTermination(Duration timeout) throws InterruptedException {
        return this.awaitTermination(Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
    }
}

