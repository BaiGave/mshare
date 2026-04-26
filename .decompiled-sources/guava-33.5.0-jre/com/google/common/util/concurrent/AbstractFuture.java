/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.AbstractFutureState;
import com.google.common.util.concurrent.DirectExecutor;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.NullnessCasts;
import com.google.common.util.concurrent.ParametricNullness;
import com.google.common.util.concurrent.Platform;
import com.google.common.util.concurrent.internal.InternalFutureFailureAccess;
import com.google.common.util.concurrent.internal.InternalFutures;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.ForOverride;
import com.google.j2objc.annotations.ReflectionSupport;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import org.jspecify.annotations.Nullable;

@GwtCompatible
@ReflectionSupport(value=ReflectionSupport.Level.FULL)
public abstract class AbstractFuture<V>
extends AbstractFutureState<V> {
    protected AbstractFuture() {
    }

    @Override
    @CanIgnoreReturnValue
    @ParametricNullness
    public V get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException, ExecutionException {
        return Platform.get(this, timeout, unit);
    }

    @Override
    @CanIgnoreReturnValue
    @ParametricNullness
    public V get() throws InterruptedException, ExecutionException {
        return Platform.get(this);
    }

    @ParametricNullness
    final V getFromAlreadyDoneTrustedFuture() throws ExecutionException {
        Object localValue = this.value();
        if (localValue == null | localValue instanceof DelegatingToFuture) {
            throw new IllegalStateException("Cannot get() on a pending future.");
        }
        return AbstractFuture.getDoneValue(localValue);
    }

    @ParametricNullness
    static <V> V getDoneValue(Object obj) throws ExecutionException {
        if (obj instanceof Cancellation) {
            Cancellation cancellation = (Cancellation)obj;
            Throwable cause = cancellation.cause;
            throw AbstractFuture.cancellationExceptionWithCause("Task was cancelled.", cause);
        }
        if (obj instanceof Failure) {
            Failure failure = (Failure)obj;
            Throwable exception = failure.exception;
            throw new ExecutionException(exception);
        }
        if (obj == NULL) {
            return (V)NullnessCasts.uncheckedNull();
        }
        Object asV = obj;
        return (V)asV;
    }

    static boolean notInstanceOfDelegatingToFuture(@Nullable Object obj) {
        return !(obj instanceof DelegatingToFuture);
    }

    @Override
    public boolean isDone() {
        Object localValue = this.value();
        return localValue != null & AbstractFuture.notInstanceOfDelegatingToFuture(localValue);
    }

    @Override
    public boolean isCancelled() {
        Object localValue = this.value();
        return localValue instanceof Cancellation;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    @CanIgnoreReturnValue
    public boolean cancel(boolean mayInterruptIfRunning) {
        Object localValue = this.value();
        boolean rValue = false;
        if (!(localValue == null | localValue instanceof DelegatingToFuture)) return rValue;
        Cancellation valueToSet = GENERATE_CANCELLATION_CAUSES ? new Cancellation(mayInterruptIfRunning, new CancellationException("Future.cancel() was called.")) : Objects.requireNonNull(mayInterruptIfRunning ? Cancellation.CAUSELESS_INTERRUPTED : Cancellation.CAUSELESS_CANCELLED);
        AbstractFuture abstractFuture = this;
        while (true) {
            if (AbstractFuture.casValue(abstractFuture, localValue, valueToSet)) {
                rValue = true;
                AbstractFuture.complete(abstractFuture, mayInterruptIfRunning);
                if (!(localValue instanceof DelegatingToFuture)) return rValue;
                ListenableFuture futureToPropagateTo = ((DelegatingToFuture)localValue).future;
                if (futureToPropagateTo instanceof Trusted) {
                    AbstractFuture trusted = (AbstractFuture)futureToPropagateTo;
                    localValue = trusted.value();
                    if (!(localValue == null | localValue instanceof DelegatingToFuture)) return rValue;
                    abstractFuture = trusted;
                    continue;
                }
                futureToPropagateTo.cancel(mayInterruptIfRunning);
                return rValue;
            }
            localValue = abstractFuture.value();
            if (AbstractFuture.notInstanceOfDelegatingToFuture(localValue)) return rValue;
        }
    }

    protected void interruptTask() {
    }

    protected final boolean wasInterrupted() {
        Object localValue = this.value();
        return localValue instanceof Cancellation && ((Cancellation)localValue).wasInterrupted;
    }

    @Override
    public void addListener(Runnable listener, Executor executor) {
        Listener oldHead;
        Preconditions.checkNotNull(listener, "Runnable was null.");
        Preconditions.checkNotNull(executor, "Executor was null.");
        if (!this.isDone() && (oldHead = this.listeners()) != Listener.TOMBSTONE) {
            Listener newNode = new Listener(listener, executor);
            do {
                newNode.next = oldHead;
                if (!this.casListeners(oldHead, newNode)) continue;
                return;
            } while ((oldHead = this.listeners()) != Listener.TOMBSTONE);
        }
        AbstractFuture.executeListener(listener, executor);
    }

    @CanIgnoreReturnValue
    protected boolean set(@ParametricNullness V value) {
        Object valueToSet;
        Object object = valueToSet = value == null ? NULL : value;
        if (AbstractFuture.casValue(this, null, valueToSet)) {
            AbstractFuture.complete(this, false);
            return true;
        }
        return false;
    }

    @CanIgnoreReturnValue
    protected boolean setException(Throwable throwable) {
        Failure valueToSet = new Failure(Preconditions.checkNotNull(throwable));
        if (AbstractFuture.casValue(this, null, valueToSet)) {
            AbstractFuture.complete(this, false);
            return true;
        }
        return false;
    }

    @CanIgnoreReturnValue
    protected boolean setFuture(ListenableFuture<? extends V> future) {
        Preconditions.checkNotNull(future);
        Object localValue = this.value();
        if (localValue == null) {
            if (future.isDone()) {
                Object value = AbstractFuture.getFutureValue(future);
                if (AbstractFuture.casValue(this, null, value)) {
                    AbstractFuture.complete(this, false);
                    return true;
                }
                return false;
            }
            DelegatingToFuture<? extends V> valueToSet = new DelegatingToFuture<V>(this, future);
            if (AbstractFuture.casValue(this, null, valueToSet)) {
                try {
                    future.addListener(valueToSet, DirectExecutor.INSTANCE);
                }
                catch (Throwable t) {
                    Failure failure;
                    try {
                        failure = new Failure(t);
                    }
                    catch (Error | Exception oomMostLikely) {
                        failure = Failure.FALLBACK_INSTANCE;
                    }
                    boolean bl = AbstractFuture.casValue(this, valueToSet, failure);
                }
                return true;
            }
            localValue = this.value();
        }
        if (localValue instanceof Cancellation) {
            future.cancel(((Cancellation)localValue).wasInterrupted);
        }
        return false;
    }

    private static Object getFutureValue(ListenableFuture<?> future) {
        boolean wasCancelled;
        Throwable throwable;
        if (future instanceof Trusted) {
            Object v = ((AbstractFuture)future).value();
            if (v instanceof Cancellation) {
                Cancellation c = (Cancellation)v;
                if (c.wasInterrupted) {
                    v = c.cause != null ? new Cancellation(false, c.cause) : Cancellation.CAUSELESS_CANCELLED;
                }
            }
            return Objects.requireNonNull(v);
        }
        if (future instanceof InternalFutureFailureAccess && (throwable = InternalFutures.tryInternalFastPathGetFailure((InternalFutureFailureAccess)((Object)future))) != null) {
            return new Failure(throwable);
        }
        if (!GENERATE_CANCELLATION_CAUSES & (wasCancelled = future.isCancelled())) {
            return Objects.requireNonNull(Cancellation.CAUSELESS_CANCELLED);
        }
        try {
            Object v = AbstractFuture.getUninterruptibly(future);
            if (wasCancelled) {
                return new Cancellation(false, new IllegalArgumentException("get() did not throw CancellationException, despite reporting isCancelled() == true: " + future));
            }
            return v == null ? NULL : v;
        }
        catch (ExecutionException exception) {
            if (wasCancelled) {
                return new Cancellation(false, new IllegalArgumentException("get() did not throw CancellationException, despite reporting isCancelled() == true: " + future, exception));
            }
            return new Failure(exception.getCause());
        }
        catch (CancellationException cancellation) {
            if (!wasCancelled) {
                return new Failure(new IllegalArgumentException("get() threw CancellationException, despite reporting isCancelled() == false: " + future, cancellation));
            }
            return new Cancellation(false, cancellation);
        }
        catch (Error | Exception t) {
            return new Failure(t);
        }
    }

    @ParametricNullness
    private static <V> V getUninterruptibly(Future<V> future) throws ExecutionException {
        boolean interrupted = false;
        while (true) {
            try {
                V v = future.get();
                return v;
            }
            catch (InterruptedException e) {
                interrupted = true;
                continue;
            }
            break;
        }
        finally {
            if (interrupted) {
                Platform.interruptCurrentThread();
            }
        }
    }

    private static void complete(AbstractFuture<?> param, boolean callInterruptTask) {
        AbstractFuture<Object> future = param;
        Listener next = null;
        block0: while (true) {
            future.releaseWaiters();
            if (callInterruptTask) {
                future.interruptTask();
                callInterruptTask = false;
            }
            future.afterDone();
            next = super.clearListeners(next);
            future = null;
            while (next != null) {
                Listener curr = next;
                next = next.next;
                Runnable task = Objects.requireNonNull(curr.task);
                if (task instanceof DelegatingToFuture) {
                    Object valueToSet;
                    DelegatingToFuture setFuture = (DelegatingToFuture)task;
                    future = setFuture.owner;
                    if (future.value() != setFuture || !AbstractFuture.casValue(future, setFuture, valueToSet = AbstractFuture.getFutureValue(setFuture.future))) continue;
                    continue block0;
                }
                AbstractFuture.executeListener(task, Objects.requireNonNull(curr.executor));
            }
            break;
        }
    }

    @ForOverride
    protected void afterDone() {
    }

    @Override
    protected final @Nullable Throwable tryInternalFastPathGetFailure() {
        Object localValue;
        if (this instanceof Trusted && (localValue = this.value()) instanceof Failure) {
            return ((Failure)localValue).exception;
        }
        return null;
    }

    final void maybePropagateCancellationTo(@Nullable Future<?> related) {
        if (related != null & this.isCancelled()) {
            related.cancel(this.wasInterrupted());
        }
    }

    private @Nullable Listener clearListeners(@Nullable Listener onto) {
        Listener head = this.gasListeners(Listener.TOMBSTONE);
        Listener reversedList = onto;
        while (head != null) {
            Listener tmp = head;
            head = head.next;
            tmp.next = reversedList;
            reversedList = tmp;
        }
        return reversedList;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (this.getClass().getName().startsWith("com.google.common.util.concurrent.")) {
            builder.append(this.getClass().getSimpleName());
        } else {
            builder.append(this.getClass().getName());
        }
        builder.append('@').append(Integer.toHexString(System.identityHashCode(this))).append("[status=");
        if (this.isCancelled()) {
            builder.append("CANCELLED");
        } else if (this.isDone()) {
            this.addDoneString(builder);
        } else {
            this.addPendingString(builder);
        }
        return builder.append("]").toString();
    }

    protected @Nullable String pendingToString() {
        if (this instanceof ScheduledFuture) {
            return "remaining delay=[" + ((ScheduledFuture)((Object)this)).getDelay(TimeUnit.MILLISECONDS) + " ms]";
        }
        return null;
    }

    private void addPendingString(StringBuilder builder) {
        int truncateLength = builder.length();
        builder.append("PENDING");
        Object localValue = this.value();
        if (localValue instanceof DelegatingToFuture) {
            builder.append(", setFuture=[");
            this.appendUserObject(builder, ((DelegatingToFuture)localValue).future);
            builder.append("]");
        } else {
            String pendingDescription;
            try {
                pendingDescription = Strings.emptyToNull(this.pendingToString());
            }
            catch (Throwable e) {
                Platform.rethrowIfErrorOtherThanStackOverflow(e);
                pendingDescription = "Exception thrown from implementation: " + e.getClass();
            }
            if (pendingDescription != null) {
                builder.append(", info=[").append(pendingDescription).append("]");
            }
        }
        if (this.isDone()) {
            builder.delete(truncateLength, builder.length());
            this.addDoneString(builder);
        }
    }

    private void addDoneString(StringBuilder builder) {
        try {
            V value = AbstractFuture.getUninterruptibly(this);
            builder.append("SUCCESS, result=[");
            this.appendResultObject(builder, value);
            builder.append("]");
        }
        catch (ExecutionException e) {
            builder.append("FAILURE, cause=[").append(e.getCause()).append("]");
        }
        catch (CancellationException e) {
            builder.append("CANCELLED");
        }
        catch (Exception e) {
            builder.append("UNKNOWN, cause=[").append(e.getClass()).append(" thrown from get()]");
        }
    }

    private void appendResultObject(StringBuilder builder, @Nullable Object o) {
        if (o == null) {
            builder.append("null");
        } else if (o == this) {
            builder.append("this future");
        } else {
            builder.append(o.getClass().getName()).append("@").append(Integer.toHexString(System.identityHashCode(o)));
        }
    }

    private void appendUserObject(StringBuilder builder, @Nullable Object o) {
        try {
            if (o == this) {
                builder.append("this future");
            } else {
                builder.append(o);
            }
        }
        catch (Throwable e) {
            Platform.rethrowIfErrorOtherThanStackOverflow(e);
            builder.append("Exception thrown from implementation: ").append(e.getClass());
        }
    }

    private static void executeListener(Runnable runnable, Executor executor) {
        try {
            executor.execute(runnable);
        }
        catch (Exception e) {
            log.get().log(Level.SEVERE, "RuntimeException while executing runnable " + runnable + " with executor " + executor, e);
        }
    }

    private static CancellationException cancellationExceptionWithCause(String message, @Nullable Throwable cause) {
        CancellationException exception = new CancellationException(message);
        exception.initCause(cause);
        return exception;
    }

    private static final class DelegatingToFuture<V>
    implements Runnable {
        final AbstractFuture<V> owner;
        final ListenableFuture<? extends V> future;

        DelegatingToFuture(AbstractFuture<V> owner, ListenableFuture<? extends V> future) {
            this.owner = owner;
            this.future = future;
        }

        @Override
        public void run() {
            if (this.owner.value() != this) {
                return;
            }
            Object valueToSet = AbstractFuture.getFutureValue(this.future);
            if (AbstractFutureState.casValue(this.owner, this, valueToSet)) {
                AbstractFuture.complete((AbstractFuture)this.owner, false);
            }
        }
    }

    private static final class Cancellation {
        static final @Nullable Cancellation CAUSELESS_INTERRUPTED;
        static final @Nullable Cancellation CAUSELESS_CANCELLED;
        final boolean wasInterrupted;
        final @Nullable Throwable cause;

        Cancellation(boolean wasInterrupted, @Nullable Throwable cause) {
            this.wasInterrupted = wasInterrupted;
            this.cause = cause;
        }

        static {
            if (AbstractFutureState.GENERATE_CANCELLATION_CAUSES) {
                CAUSELESS_CANCELLED = null;
                CAUSELESS_INTERRUPTED = null;
            } else {
                CAUSELESS_CANCELLED = new Cancellation(false, null);
                CAUSELESS_INTERRUPTED = new Cancellation(true, null);
            }
        }
    }

    private static final class Failure {
        static final Failure FALLBACK_INSTANCE = new Failure(new Throwable("Failure occurred while trying to finish a future."){

            @Override
            public Throwable fillInStackTrace() {
                return this;
            }
        });
        final Throwable exception;

        Failure(Throwable exception) {
            this.exception = Preconditions.checkNotNull(exception);
        }
    }

    static interface Trusted<V>
    extends ListenableFuture<V> {
    }

    static final class Listener {
        static final Listener TOMBSTONE = new Listener();
        final @Nullable Runnable task;
        final @Nullable Executor executor;
        @Nullable Listener next;

        Listener(Runnable task, Executor executor) {
            this.task = task;
            this.executor = executor;
        }

        Listener() {
            this.task = null;
            this.executor = null;
        }
    }

    static abstract class TrustedFuture<V>
    extends AbstractFuture<V>
    implements Trusted<V> {
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

