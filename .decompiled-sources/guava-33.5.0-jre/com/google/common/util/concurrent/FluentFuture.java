/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.ForwardingFluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.GwtFluentFutureCatchingSpecialization;
import com.google.common.util.concurrent.Internal;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ParametricNullness;
import com.google.common.util.concurrent.Partially;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotMock;
import com.google.errorprone.annotations.InlineMe;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@DoNotMock(value="Use FluentFuture.from(Futures.immediate*Future) or SettableFuture")
@GwtCompatible
public abstract class FluentFuture<V>
extends GwtFluentFutureCatchingSpecialization<V> {
    FluentFuture() {
    }

    public static <V> FluentFuture<V> from(ListenableFuture<V> future) {
        return future instanceof FluentFuture ? (FluentFuture<V>)future : new ForwardingFluentFuture<V>(future);
    }

    @Deprecated
    @InlineMe(replacement="checkNotNull(future)", staticImports={"com.google.common.base.Preconditions.checkNotNull"})
    public static <V> FluentFuture<V> from(FluentFuture<V> future) {
        return Preconditions.checkNotNull(future);
    }

    @J2ktIncompatible
    @Partially.GwtIncompatible(value="AVAILABLE but requires exceptionType to be Throwable.class")
    public final <X extends Throwable> FluentFuture<V> catching(Class<X> exceptionType, Function<? super X, ? extends V> fallback, Executor executor) {
        return (FluentFuture)Futures.catching(this, exceptionType, fallback, executor);
    }

    @J2ktIncompatible
    @Partially.GwtIncompatible(value="AVAILABLE but requires exceptionType to be Throwable.class")
    public final <X extends Throwable> FluentFuture<V> catchingAsync(Class<X> exceptionType, AsyncFunction<? super X, ? extends V> fallback, Executor executor) {
        return (FluentFuture)Futures.catchingAsync(this, exceptionType, fallback, executor);
    }

    @J2ktIncompatible
    @GwtIncompatible
    public final FluentFuture<V> withTimeout(Duration timeout, ScheduledExecutorService scheduledExecutor) {
        return this.withTimeout(Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS, scheduledExecutor);
    }

    @J2ktIncompatible
    @GwtIncompatible
    public final FluentFuture<V> withTimeout(long timeout, TimeUnit unit, ScheduledExecutorService scheduledExecutor) {
        return (FluentFuture)Futures.withTimeout(this, timeout, unit, scheduledExecutor);
    }

    public final <T> FluentFuture<T> transformAsync(AsyncFunction<? super V, T> function, Executor executor) {
        return (FluentFuture)Futures.transformAsync(this, function, executor);
    }

    public final <T> FluentFuture<T> transform(Function<? super V, T> function, Executor executor) {
        return (FluentFuture)Futures.transform(this, function, executor);
    }

    public final void addCallback(FutureCallback<? super V> callback, Executor executor) {
        Futures.addCallback(this, callback, executor);
    }

    static abstract class TrustedFuture<V>
    extends FluentFuture<V>
    implements AbstractFuture.Trusted<V> {
        TrustedFuture() {
        }

        @Override
        @CanIgnoreReturnValue
        @ParametricNullness
        public final V get() throws InterruptedException, ExecutionException {
            return super.get();
        }

        @Override
        @CanIgnoreReturnValue
        @ParametricNullness
        public final V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return super.get(timeout, unit);
        }

        @Override
        public final boolean isDone() {
            return super.isDone();
        }

        @Override
        public final boolean isCancelled() {
            return super.isCancelled();
        }

        @Override
        public final void addListener(Runnable listener, Executor executor) {
            super.addListener(listener, executor);
        }

        @Override
        @CanIgnoreReturnValue
        public final boolean cancel(boolean mayInterruptIfRunning) {
            return super.cancel(mayInterruptIfRunning);
        }
    }
}

