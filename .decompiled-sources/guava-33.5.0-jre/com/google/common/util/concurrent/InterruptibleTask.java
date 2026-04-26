/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.NullnessCasts;
import com.google.common.util.concurrent.ParametricNullness;
import com.google.common.util.concurrent.Platform;
import com.google.j2objc.annotations.ReflectionSupport;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.AbstractOwnableSynchronizer;
import java.util.concurrent.locks.LockSupport;
import org.jspecify.annotations.Nullable;

@GwtCompatible
@ReflectionSupport(value=ReflectionSupport.Level.FULL)
abstract class InterruptibleTask<T>
extends AtomicReference<Runnable>
implements Runnable {
    private static final Runnable DONE;
    private static final Runnable PARKED;
    private static final int MAX_BUSY_WAIT_SPINS = 1000;

    InterruptibleTask() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void run() {
        Thread currentThread = Thread.currentThread();
        if (!this.compareAndSet(null, currentThread)) {
            return;
        }
        boolean run = !this.isDone();
        Object result = null;
        Throwable error = null;
        try {
            if (run) {
                result = this.runInterruptibly();
            }
        }
        catch (Throwable t) {
            Platform.restoreInterruptIfIsInterruptedException(t);
            error = t;
        }
        finally {
            if (!this.compareAndSet(currentThread, DONE)) {
                this.waitForInterrupt(currentThread);
            }
            if (run) {
                if (error == null) {
                    this.afterRanInterruptiblySuccess(NullnessCasts.uncheckedCastNullableTToT(result));
                } else {
                    this.afterRanInterruptiblyFailure(error);
                }
            }
        }
    }

    private void waitForInterrupt(Thread currentThread) {
        boolean restoreInterruptedBit = false;
        int spinCount = 0;
        Runnable state = (Runnable)this.get();
        Blocker blocker = null;
        while (state instanceof Blocker || state == PARKED) {
            if (state instanceof Blocker) {
                blocker = (Blocker)state;
            }
            if (++spinCount > 1000) {
                if (state == PARKED || this.compareAndSet(state, PARKED)) {
                    restoreInterruptedBit = Thread.interrupted() || restoreInterruptedBit;
                    LockSupport.park(blocker);
                }
            } else {
                Thread.yield();
            }
            state = (Runnable)this.get();
        }
        if (restoreInterruptedBit) {
            currentThread.interrupt();
        }
    }

    abstract boolean isDone();

    @ParametricNullness
    abstract T runInterruptibly() throws Exception;

    abstract void afterRanInterruptiblySuccess(@ParametricNullness T var1);

    abstract void afterRanInterruptiblyFailure(Throwable var1);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final void interruptTask() {
        Runnable currentRunner = (Runnable)this.get();
        if (currentRunner instanceof Thread) {
            Blocker blocker = new Blocker(this);
            blocker.setOwner(Thread.currentThread());
            if (this.compareAndSet(currentRunner, blocker)) {
                try {
                    ((Thread)currentRunner).interrupt();
                }
                finally {
                    Runnable prev = this.getAndSet(DONE);
                    if (prev == PARKED) {
                        LockSupport.unpark((Thread)currentRunner);
                    }
                }
            }
        }
    }

    @Override
    public final String toString() {
        Runnable state = (Runnable)this.get();
        String result = state == DONE ? "running=[DONE]" : (state instanceof Blocker ? "running=[INTERRUPTED]" : (state instanceof Thread ? "running=[RUNNING ON " + ((Thread)state).getName() + "]" : "running=[NOT STARTED YET]"));
        return result + ", " + this.toPendingString();
    }

    abstract String toPendingString();

    static {
        Class<LockSupport> clazz = LockSupport.class;
        DONE = new DoNothingRunnable();
        PARKED = new DoNothingRunnable();
    }

    @VisibleForTesting
    static final class Blocker
    extends AbstractOwnableSynchronizer
    implements Runnable {
        private final InterruptibleTask<?> task;

        private Blocker(InterruptibleTask<?> task) {
            this.task = task;
        }

        @Override
        public void run() {
        }

        private void setOwner(Thread thread) {
            super.setExclusiveOwnerThread(thread);
        }

        @VisibleForTesting
        @Nullable Thread getOwner() {
            return super.getExclusiveOwnerThread();
        }

        public String toString() {
            return this.task.toString();
        }
    }

    private static final class DoNothingRunnable
    implements Runnable {
        private DoNothingRunnable() {
        }

        @Override
        public void run() {
        }
    }
}

