/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.NullnessCasts;
import com.google.common.util.concurrent.ParametricNullness;
import com.google.common.util.concurrent.Platform;
import com.google.common.util.concurrent.internal.InternalFutureFailureAccess;
import com.google.common.util.concurrent.internal.InternalFutures;
import com.google.errorprone.annotations.ForOverride;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import org.jspecify.annotations.Nullable;

@GwtCompatible
abstract class AbstractCatchingFuture<V, X extends Throwable, F, T>
extends FluentFuture.TrustedFuture<V>
implements Runnable {
    @LazyInit
    @Nullable ListenableFuture<? extends V> inputFuture;
    @LazyInit
    @Nullable Class<X> exceptionType;
    @LazyInit
    @Nullable F fallback;

    static <V, X extends Throwable> ListenableFuture<V> create(ListenableFuture<? extends V> input, Class<X> exceptionType, Function<? super X, ? extends V> fallback, Executor executor) {
        CatchingFuture<? extends V, ? super X> output = new CatchingFuture<V, X>(input, exceptionType, fallback);
        input.addListener(output, MoreExecutors.rejectionPropagatingExecutor(executor, output));
        return output;
    }

    static <X extends Throwable, V> ListenableFuture<V> createAsync(ListenableFuture<? extends V> input, Class<X> exceptionType, AsyncFunction<? super X, ? extends V> fallback, Executor executor) {
        AsyncCatchingFuture<? extends V, ? super X> output = new AsyncCatchingFuture<V, X>(input, exceptionType, fallback);
        input.addListener(output, MoreExecutors.rejectionPropagatingExecutor(executor, output));
        return output;
    }

    AbstractCatchingFuture(ListenableFuture<? extends V> inputFuture, Class<X> exceptionType, F fallback) {
        this.inputFuture = Preconditions.checkNotNull(inputFuture);
        this.exceptionType = Preconditions.checkNotNull(exceptionType);
        this.fallback = Preconditions.checkNotNull(fallback);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void run() {
        T fallbackResult;
        F localFallback;
        Class<X> localExceptionType;
        ListenableFuture<V> localInputFuture = this.inputFuture;
        if (localInputFuture == null | (localExceptionType = this.exceptionType) == null | (localFallback = this.fallback) == null || this.isCancelled()) {
            return;
        }
        this.inputFuture = null;
        Object sourceResult = null;
        Throwable throwable = null;
        try {
            if (localInputFuture instanceof InternalFutureFailureAccess) {
                throwable = InternalFutures.tryInternalFastPathGetFailure((InternalFutureFailureAccess)((Object)localInputFuture));
            }
            if (throwable == null) {
                sourceResult = Futures.getDone(localInputFuture);
            }
        }
        catch (ExecutionException e) {
            throwable = e.getCause();
            if (throwable == null) {
                throwable = new NullPointerException("Future type " + localInputFuture.getClass() + " threw " + e.getClass() + " without a cause");
            }
        }
        catch (Throwable t) {
            throwable = t;
        }
        if (throwable == null) {
            this.set(NullnessCasts.uncheckedCastNullableTToT(sourceResult));
            return;
        }
        if (!Platform.isInstanceOfThrowableClass(throwable, localExceptionType)) {
            this.setFuture(localInputFuture);
            return;
        }
        Throwable castThrowable = throwable;
        try {
            fallbackResult = this.doFallback(localFallback, castThrowable);
        }
        catch (Throwable t) {
            Platform.restoreInterruptIfIsInterruptedException(t);
            this.setException(t);
            return;
        }
        finally {
            this.exceptionType = null;
            this.fallback = null;
        }
        this.setResult(fallbackResult);
    }

    @ForOverride
    @ParametricNullness
    abstract T doFallback(F var1, X var2) throws Exception;

    @ForOverride
    abstract void setResult(@ParametricNullness T var1);

    @Override
    protected final void afterDone() {
        ListenableFuture<? extends V> localInputFuture = this.inputFuture;
        this.maybePropagateCancellationTo(localInputFuture);
        this.inputFuture = null;
        this.exceptionType = null;
        this.fallback = null;
    }

    @Override
    protected @Nullable String pendingToString() {
        ListenableFuture<? extends V> localInputFuture = this.inputFuture;
        Class<X> localExceptionType = this.exceptionType;
        F localFallback = this.fallback;
        String superString = super.pendingToString();
        String resultString = "";
        if (localInputFuture != null) {
            resultString = "inputFuture=[" + localInputFuture + "], ";
        }
        if (localExceptionType != null && localFallback != null) {
            return resultString + "exceptionType=[" + localExceptionType + "], fallback=[" + localFallback + "]";
        }
        if (superString != null) {
            return resultString + superString;
        }
        return null;
    }

    private static final class CatchingFuture<V, X extends Throwable>
    extends AbstractCatchingFuture<V, X, Function<? super X, ? extends V>, V> {
        CatchingFuture(ListenableFuture<? extends V> input, Class<X> exceptionType, Function<? super X, ? extends V> fallback) {
            super(input, exceptionType, fallback);
        }

        @Override
        @ParametricNullness
        V doFallback(Function<? super X, ? extends V> fallback, X cause) throws Exception {
            return fallback.apply(cause);
        }

        @Override
        void setResult(@ParametricNullness V result) {
            this.set(result);
        }
    }

    private static final class AsyncCatchingFuture<V, X extends Throwable>
    extends AbstractCatchingFuture<V, X, AsyncFunction<? super X, ? extends V>, ListenableFuture<? extends V>> {
        AsyncCatchingFuture(ListenableFuture<? extends V> input, Class<X> exceptionType, AsyncFunction<? super X, ? extends V> fallback) {
            super(input, exceptionType, fallback);
        }

        @Override
        ListenableFuture<? extends V> doFallback(AsyncFunction<? super X, ? extends V> fallback, X cause) throws Exception {
            ListenableFuture<? extends V> output = fallback.apply(cause);
            Preconditions.checkNotNull(output, "AsyncFunction.apply returned null instead of a Future. Did you mean to return immediateFuture(null)? %s", fallback);
            return output;
        }

        @Override
        void setResult(ListenableFuture<? extends V> result) {
            this.setFuture(result);
        }
    }
}

