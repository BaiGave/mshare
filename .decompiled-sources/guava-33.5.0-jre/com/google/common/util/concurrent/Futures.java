/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractCatchingFuture;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.AbstractTransformFuture;
import com.google.common.util.concurrent.AsyncCallable;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.CollectionFuture;
import com.google.common.util.concurrent.CombinedFuture;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.FuturesGetChecked;
import com.google.common.util.concurrent.GwtFuturesCatchingSpecialization;
import com.google.common.util.concurrent.ImmediateFuture;
import com.google.common.util.concurrent.Internal;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ParametricNullness;
import com.google.common.util.concurrent.Partially;
import com.google.common.util.concurrent.TimeoutFuture;
import com.google.common.util.concurrent.TrustedListenableFutureTask;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.common.util.concurrent.internal.InternalFutureFailureAccess;
import com.google.common.util.concurrent.internal.InternalFutures;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class Futures
extends GwtFuturesCatchingSpecialization {
    private Futures() {
    }

    public static <V> ListenableFuture<V> immediateFuture(@ParametricNullness V value) {
        if (value == null) {
            ListenableFuture<?> typedNull = ImmediateFuture.NULL;
            return typedNull;
        }
        return new ImmediateFuture<V>(value);
    }

    public static ListenableFuture<@Nullable Void> immediateVoidFuture() {
        return ImmediateFuture.NULL;
    }

    public static <V> ListenableFuture<V> immediateFailedFuture(Throwable throwable) {
        Preconditions.checkNotNull(throwable);
        return new ImmediateFuture.ImmediateFailedFuture(throwable);
    }

    public static <V> ListenableFuture<V> immediateCancelledFuture() {
        ImmediateFuture.ImmediateCancelledFuture<Object> instance = ImmediateFuture.ImmediateCancelledFuture.INSTANCE;
        if (instance != null) {
            return instance;
        }
        return new ImmediateFuture.ImmediateCancelledFuture();
    }

    public static <O> ListenableFuture<O> submit(Callable<O> callable, Executor executor) {
        TrustedListenableFutureTask<O> task = TrustedListenableFutureTask.create(callable);
        executor.execute(task);
        return task;
    }

    public static ListenableFuture<@Nullable Void> submit(Runnable runnable, Executor executor) {
        TrustedListenableFutureTask<@Nullable Object> task = TrustedListenableFutureTask.create(runnable, null);
        executor.execute(task);
        return task;
    }

    public static <O> ListenableFuture<O> submitAsync(AsyncCallable<O> callable, Executor executor) {
        TrustedListenableFutureTask<O> task = TrustedListenableFutureTask.create(callable);
        executor.execute(task);
        return task;
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <O> ListenableFuture<O> scheduleAsync(AsyncCallable<O> callable, Duration delay, ScheduledExecutorService executorService) {
        return Futures.scheduleAsync(callable, Internal.toNanosSaturated(delay), TimeUnit.NANOSECONDS, executorService);
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <O> ListenableFuture<O> scheduleAsync(AsyncCallable<O> callable, long delay, TimeUnit timeUnit, ScheduledExecutorService executorService) {
        TrustedListenableFutureTask<O> task = TrustedListenableFutureTask.create(callable);
        ScheduledFuture<?> scheduled = executorService.schedule(task, delay, timeUnit);
        task.addListener(() -> scheduled.cancel(false), MoreExecutors.directExecutor());
        return task;
    }

    @J2ktIncompatible
    @Partially.GwtIncompatible(value="AVAILABLE but requires exceptionType to be Throwable.class")
    public static <V, X extends Throwable> ListenableFuture<V> catching(ListenableFuture<? extends V> input, Class<X> exceptionType, Function<? super X, ? extends V> fallback, Executor executor) {
        return AbstractCatchingFuture.create(input, exceptionType, fallback, executor);
    }

    @J2ktIncompatible
    @Partially.GwtIncompatible(value="AVAILABLE but requires exceptionType to be Throwable.class")
    public static <V, X extends Throwable> ListenableFuture<V> catchingAsync(ListenableFuture<? extends V> input, Class<X> exceptionType, AsyncFunction<? super X, ? extends V> fallback, Executor executor) {
        return AbstractCatchingFuture.createAsync(input, exceptionType, fallback, executor);
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <V> ListenableFuture<V> withTimeout(ListenableFuture<V> delegate, Duration time, ScheduledExecutorService scheduledExecutor) {
        return Futures.withTimeout(delegate, Internal.toNanosSaturated(time), TimeUnit.NANOSECONDS, scheduledExecutor);
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <V> ListenableFuture<V> withTimeout(ListenableFuture<V> delegate, long time, TimeUnit unit, ScheduledExecutorService scheduledExecutor) {
        if (delegate.isDone()) {
            return delegate;
        }
        return TimeoutFuture.create(delegate, time, unit, scheduledExecutor);
    }

    public static <I, O> ListenableFuture<O> transformAsync(ListenableFuture<I> input, AsyncFunction<? super I, ? extends O> function, Executor executor) {
        return AbstractTransformFuture.createAsync(input, function, executor);
    }

    public static <I, O> ListenableFuture<O> transform(ListenableFuture<I> input, Function<? super I, ? extends O> function, Executor executor) {
        return AbstractTransformFuture.create(input, function, executor);
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <I, O> Future<O> lazyTransform(final Future<I> input, final Function<? super I, ? extends O> function) {
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(function);
        return new Future<O>(){

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return input.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return input.isCancelled();
            }

            @Override
            public boolean isDone() {
                return input.isDone();
            }

            @Override
            public O get() throws InterruptedException, ExecutionException {
                return this.applyTransformation(input.get());
            }

            @Override
            public O get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return this.applyTransformation(input.get(timeout, unit));
            }

            private O applyTransformation(I input2) throws ExecutionException {
                try {
                    return function.apply(input2);
                }
                catch (Throwable t) {
                    throw new ExecutionException(t);
                }
            }
        };
    }

    @SafeVarargs
    public static <V> ListenableFuture<List<V>> allAsList(ListenableFuture<? extends V> ... futures) {
        CollectionFuture.ListFuture<? extends V> nullable;
        CollectionFuture.ListFuture<? extends V> nonNull = nullable = new CollectionFuture.ListFuture<V>(ImmutableList.copyOf(futures), true);
        return nonNull;
    }

    public static <V> ListenableFuture<List<V>> allAsList(Iterable<? extends ListenableFuture<? extends V>> futures) {
        CollectionFuture.ListFuture nullable;
        CollectionFuture.ListFuture nonNull = nullable = new CollectionFuture.ListFuture(ImmutableList.copyOf(futures), true);
        return nonNull;
    }

    @SafeVarargs
    public static <V> FutureCombiner<V> whenAllComplete(ListenableFuture<? extends V> ... futures) {
        return new FutureCombiner(false, ImmutableList.copyOf(futures));
    }

    public static <V> FutureCombiner<V> whenAllComplete(Iterable<? extends ListenableFuture<? extends V>> futures) {
        return new FutureCombiner(false, ImmutableList.copyOf(futures));
    }

    @SafeVarargs
    public static <V> FutureCombiner<V> whenAllSucceed(ListenableFuture<? extends V> ... futures) {
        return new FutureCombiner(true, ImmutableList.copyOf(futures));
    }

    public static <V> FutureCombiner<V> whenAllSucceed(Iterable<? extends ListenableFuture<? extends V>> futures) {
        return new FutureCombiner(true, ImmutableList.copyOf(futures));
    }

    public static <V> ListenableFuture<V> nonCancellationPropagating(ListenableFuture<V> future) {
        if (future.isDone()) {
            return future;
        }
        NonCancellationPropagatingFuture<V> output = new NonCancellationPropagatingFuture<V>(future);
        future.addListener(output, MoreExecutors.directExecutor());
        return output;
    }

    @SafeVarargs
    public static <V> ListenableFuture<List<@Nullable V>> successfulAsList(ListenableFuture<? extends V> ... futures) {
        return new CollectionFuture.ListFuture<V>(ImmutableList.copyOf(futures), false);
    }

    public static <V> ListenableFuture<List<@Nullable V>> successfulAsList(Iterable<? extends ListenableFuture<? extends V>> futures) {
        return new CollectionFuture.ListFuture(ImmutableList.copyOf(futures), false);
    }

    public static <T> ImmutableList<ListenableFuture<T>> inCompletionOrder(Iterable<? extends ListenableFuture<? extends T>> futures) {
        ListenableFuture[] copy = Futures.gwtCompatibleToArray(futures);
        InCompletionOrderState state = new InCompletionOrderState(copy);
        ImmutableList.Builder delegatesBuilder = ImmutableList.builderWithExpectedSize(copy.length);
        for (int i = 0; i < copy.length; ++i) {
            delegatesBuilder.add(new InCompletionOrderFuture(state));
        }
        ImmutableCollection delegates = delegatesBuilder.build();
        for (int i = 0; i < copy.length; ++i) {
            int localI = i;
            copy[i].addListener(() -> Futures.lambda$inCompletionOrder$0(state, (ImmutableList)delegates, localI), MoreExecutors.directExecutor());
        }
        ImmutableCollection delegatesCast = delegates;
        return delegatesCast;
    }

    private static <T> ListenableFuture<? extends T>[] gwtCompatibleToArray(Iterable<? extends ListenableFuture<? extends T>> futures) {
        ImmutableList<ListenableFuture<ListenableFuture>> collection = futures instanceof Collection ? (ImmutableList<ListenableFuture<ListenableFuture>>)futures : ImmutableList.copyOf(futures);
        return collection.toArray(new ListenableFuture[0]);
    }

    public static <V> void addCallback(ListenableFuture<V> future, FutureCallback<? super V> callback, Executor executor) {
        Preconditions.checkNotNull(callback);
        future.addListener(new CallbackListener<V>(future, callback), executor);
    }

    @CanIgnoreReturnValue
    @ParametricNullness
    public static <V> V getDone(Future<V> future) throws ExecutionException {
        Preconditions.checkState(future.isDone(), "Future was expected to be done: %s", future);
        return Uninterruptibles.getUninterruptibly(future);
    }

    @CanIgnoreReturnValue
    @J2ktIncompatible
    @GwtIncompatible
    @ParametricNullness
    public static <V, X extends Exception> V getChecked(Future<V> future, Class<X> exceptionClass) throws X {
        return FuturesGetChecked.getChecked(future, exceptionClass);
    }

    @CanIgnoreReturnValue
    @J2ktIncompatible
    @GwtIncompatible
    @ParametricNullness
    public static <V, X extends Exception> V getChecked(Future<V> future, Class<X> exceptionClass, Duration timeout) throws X {
        return Futures.getChecked(future, exceptionClass, Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
    }

    @CanIgnoreReturnValue
    @J2ktIncompatible
    @GwtIncompatible
    @ParametricNullness
    public static <V, X extends Exception> V getChecked(Future<V> future, Class<X> exceptionClass, long timeout, TimeUnit unit) throws X {
        return FuturesGetChecked.getChecked(future, exceptionClass, timeout, unit);
    }

    @CanIgnoreReturnValue
    @ParametricNullness
    public static <V> V getUnchecked(Future<V> future) {
        Preconditions.checkNotNull(future);
        try {
            return Uninterruptibles.getUninterruptibly(future);
        }
        catch (ExecutionException wrapper) {
            if (wrapper.getCause() instanceof Error) {
                throw new ExecutionError((Error)wrapper.getCause());
            }
            throw new UncheckedExecutionException(wrapper.getCause());
        }
    }

    private static /* synthetic */ void lambda$inCompletionOrder$0(InCompletionOrderState state, ImmutableList delegates, int localI) {
        state.recordInputCompletion(delegates, localI);
    }

    @GwtCompatible
    public static final class FutureCombiner<V> {
        private final boolean allMustSucceed;
        private final ImmutableList<ListenableFuture<? extends V>> futures;

        private FutureCombiner(boolean allMustSucceed, ImmutableList<ListenableFuture<? extends V>> futures) {
            this.allMustSucceed = allMustSucceed;
            this.futures = futures;
        }

        public <C> ListenableFuture<C> callAsync(AsyncCallable<C> combiner, Executor executor) {
            return new CombinedFuture<C>(this.futures, this.allMustSucceed, executor, combiner);
        }

        public <C> ListenableFuture<C> call(Callable<C> combiner, Executor executor) {
            return new CombinedFuture<C>(this.futures, this.allMustSucceed, executor, combiner);
        }

        public ListenableFuture<?> run(final Runnable combiner, Executor executor) {
            return this.call(new Callable<Void>(){
                final /* synthetic */ FutureCombiner this$0;
                {
                    this.this$0 = this$0;
                }

                @Override
                public @Nullable Void call() throws Exception {
                    combiner.run();
                    return null;
                }
            }, executor);
        }
    }

    private static final class NonCancellationPropagatingFuture<V>
    extends AbstractFuture.TrustedFuture<V>
    implements Runnable {
        @LazyInit
        private @Nullable ListenableFuture<V> delegate;

        NonCancellationPropagatingFuture(ListenableFuture<V> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            ListenableFuture<V> localDelegate = this.delegate;
            if (localDelegate != null) {
                this.setFuture(localDelegate);
            }
        }

        @Override
        protected @Nullable String pendingToString() {
            ListenableFuture<V> localDelegate = this.delegate;
            if (localDelegate != null) {
                return "delegate=[" + localDelegate + "]";
            }
            return null;
        }

        @Override
        protected void afterDone() {
            this.delegate = null;
        }
    }

    private static final class InCompletionOrderState<T> {
        private boolean wasCancelled = false;
        private boolean shouldInterrupt = true;
        private final AtomicInteger incompleteOutputCount;
        private final @Nullable ListenableFuture<? extends T>[] inputFutures;
        private volatile int delegateIndex = 0;

        private InCompletionOrderState(ListenableFuture<? extends T>[] inputFutures) {
            this.inputFutures = inputFutures;
            this.incompleteOutputCount = new AtomicInteger(inputFutures.length);
        }

        private void recordOutputCancellation(boolean interruptIfRunning) {
            this.wasCancelled = true;
            if (!interruptIfRunning) {
                this.shouldInterrupt = false;
            }
            this.recordCompletion();
        }

        private void recordInputCompletion(ImmutableList<AbstractFuture<T>> delegates, int inputFutureIndex) {
            ListenableFuture<? extends T> inputFuture = Objects.requireNonNull(this.inputFutures[inputFutureIndex]);
            this.inputFutures[inputFutureIndex] = null;
            for (int i = this.delegateIndex; i < delegates.size(); ++i) {
                if (!((AbstractFuture)delegates.get(i)).setFuture(inputFuture)) continue;
                this.recordCompletion();
                this.delegateIndex = i + 1;
                return;
            }
            this.delegateIndex = delegates.size();
        }

        private void recordCompletion() {
            if (this.incompleteOutputCount.decrementAndGet() == 0 && this.wasCancelled) {
                for (ListenableFuture<T> listenableFuture : this.inputFutures) {
                    if (listenableFuture == null) continue;
                    listenableFuture.cancel(this.shouldInterrupt);
                }
            }
        }
    }

    private static final class InCompletionOrderFuture<T>
    extends AbstractFuture<T> {
        private @Nullable InCompletionOrderState<T> state;

        private InCompletionOrderFuture(InCompletionOrderState<T> state) {
            this.state = state;
        }

        @Override
        public boolean cancel(boolean interruptIfRunning) {
            InCompletionOrderState<T> localState = this.state;
            if (super.cancel(interruptIfRunning)) {
                ((InCompletionOrderState)Objects.requireNonNull(localState)).recordOutputCancellation(interruptIfRunning);
                return true;
            }
            return false;
        }

        @Override
        protected void afterDone() {
            this.state = null;
        }

        @Override
        protected @Nullable String pendingToString() {
            InCompletionOrderState<T> localState = this.state;
            if (localState != null) {
                return "inputCount=[" + ((InCompletionOrderState)localState).inputFutures.length + "], remaining=[" + ((InCompletionOrderState)localState).incompleteOutputCount.get() + "]";
            }
            return null;
        }
    }

    private static final class CallbackListener<V>
    implements Runnable {
        final Future<V> future;
        final FutureCallback<? super V> callback;

        CallbackListener(Future<V> future, FutureCallback<? super V> callback) {
            this.future = future;
            this.callback = callback;
        }

        @Override
        public void run() {
            V value;
            Throwable failure;
            if (this.future instanceof InternalFutureFailureAccess && (failure = InternalFutures.tryInternalFastPathGetFailure((InternalFutureFailureAccess)((Object)this.future))) != null) {
                this.callback.onFailure(failure);
                return;
            }
            try {
                value = Futures.getDone(this.future);
            }
            catch (ExecutionException e) {
                this.callback.onFailure(e.getCause());
                return;
            }
            catch (Throwable e) {
                this.callback.onFailure(e);
                return;
            }
            this.callback.onSuccess(value);
        }

        public String toString() {
            return MoreObjects.toStringHelper(this).addValue(this.callback).toString();
        }
    }
}

