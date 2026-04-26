/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AsyncCallable;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import com.google.common.util.concurrent.TrustedListenableFutureTask;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public final class ExecutionSequencer {
    private final AtomicReference<ListenableFuture<@Nullable Void>> ref = new AtomicReference<ListenableFuture<Void>>(Futures.immediateVoidFuture());
    @LazyInit
    private ThreadConfinedTaskQueue latestTaskQueue = new ThreadConfinedTaskQueue();

    private ExecutionSequencer() {
    }

    public static ExecutionSequencer create() {
        return new ExecutionSequencer();
    }

    public <T> ListenableFuture<T> submit(final Callable<T> callable, Executor executor) {
        Preconditions.checkNotNull(callable);
        Preconditions.checkNotNull(executor);
        return this.submitAsync(new AsyncCallable<T>(){
            final /* synthetic */ ExecutionSequencer this$0;
            {
                this.this$0 = this$0;
            }

            @Override
            public ListenableFuture<T> call() throws Exception {
                return Futures.immediateFuture(callable.call());
            }

            public String toString() {
                return callable.toString();
            }
        }, executor);
    }

    /*
     * Issues handling annotations - annotations may be inaccurate
     */
    public <T> ListenableFuture<T> submitAsync(final AsyncCallable<T> callable, Executor executor) {
        Preconditions.checkNotNull(callable);
        Preconditions.checkNotNull(executor);
        final TaskNonReentrantExecutor taskExecutor = new TaskNonReentrantExecutor(executor, this);
        AsyncCallable task = new AsyncCallable<T>(){
            final /* synthetic */ ExecutionSequencer this$0;
            {
                this.this$0 = this$0;
            }

            @Override
            public ListenableFuture<T> call() throws Exception {
                if (!taskExecutor.trySetStarted()) {
                    return Futures.immediateCancelledFuture();
                }
                return callable.call();
            }

            public String toString() {
                return callable.toString();
            }
        };
        SettableFuture<@Nullable V> newFuture = SettableFuture.create();
        @Nullable ListenableFuture oldFuture = this.ref.getAndSet(newFuture);
        TrustedListenableFutureTask taskFuture = TrustedListenableFutureTask.create(task);
        oldFuture.addListener(taskFuture, taskExecutor);
        ListenableFuture outputFuture = Futures.nonCancellationPropagating(taskFuture);
        Runnable listener = () -> {
            if (taskFuture.isDone()) {
                newFuture.setFuture(oldFuture);
            } else if (outputFuture.isCancelled() && taskExecutor.trySetCancelled()) {
                taskFuture.cancel(false);
            }
        };
        outputFuture.addListener(listener, MoreExecutors.directExecutor());
        taskFuture.addListener(listener, MoreExecutors.directExecutor());
        return outputFuture;
    }

    private static final class ThreadConfinedTaskQueue {
        @LazyInit
        @Nullable Thread thread;
        @Nullable Runnable nextTask;
        @Nullable Executor nextExecutor;

        private ThreadConfinedTaskQueue() {
        }
    }

    private static final class TaskNonReentrantExecutor
    extends AtomicReference<RunningState>
    implements Executor,
    Runnable {
        @Nullable ExecutionSequencer sequencer;
        @Nullable Executor delegate;
        @Nullable Runnable task;
        @LazyInit
        @Nullable Thread submitting;

        private TaskNonReentrantExecutor(Executor delegate, ExecutionSequencer sequencer) {
            super(RunningState.NOT_RUN);
            this.delegate = delegate;
            this.sequencer = sequencer;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void execute(Runnable task) {
            if (this.get() == RunningState.CANCELLED) {
                this.delegate = null;
                this.sequencer = null;
                return;
            }
            this.submitting = Thread.currentThread();
            try {
                ThreadConfinedTaskQueue submittingTaskQueue = Objects.requireNonNull(this.sequencer).latestTaskQueue;
                if (submittingTaskQueue.thread == this.submitting) {
                    this.sequencer = null;
                    Preconditions.checkState(submittingTaskQueue.nextTask == null);
                    submittingTaskQueue.nextTask = task;
                    submittingTaskQueue.nextExecutor = Objects.requireNonNull(this.delegate);
                    this.delegate = null;
                } else {
                    Executor localDelegate = Objects.requireNonNull(this.delegate);
                    this.delegate = null;
                    this.task = task;
                    localDelegate.execute(this);
                }
            }
            finally {
                this.submitting = null;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void run() {
            Thread currentThread = Thread.currentThread();
            if (currentThread != this.submitting) {
                Runnable localTask = Objects.requireNonNull(this.task);
                this.task = null;
                localTask.run();
                return;
            }
            ThreadConfinedTaskQueue executingTaskQueue = new ThreadConfinedTaskQueue();
            executingTaskQueue.thread = currentThread;
            Objects.requireNonNull(this.sequencer).latestTaskQueue = executingTaskQueue;
            this.sequencer = null;
            try {
                Executor queuedExecutor;
                Runnable queuedTask;
                Runnable localTask = Objects.requireNonNull(this.task);
                this.task = null;
                localTask.run();
                while ((queuedTask = executingTaskQueue.nextTask) != null && (queuedExecutor = executingTaskQueue.nextExecutor) != null) {
                    executingTaskQueue.nextTask = null;
                    executingTaskQueue.nextExecutor = null;
                    queuedExecutor.execute(queuedTask);
                }
            }
            finally {
                executingTaskQueue.thread = null;
            }
        }

        private boolean trySetStarted() {
            return this.compareAndSet(RunningState.NOT_RUN, RunningState.STARTED);
        }

        private boolean trySetCancelled() {
            return this.compareAndSet(RunningState.NOT_RUN, RunningState.CANCELLED);
        }
    }

    static enum RunningState {
        NOT_RUN,
        CANCELLED,
        STARTED;

    }
}

