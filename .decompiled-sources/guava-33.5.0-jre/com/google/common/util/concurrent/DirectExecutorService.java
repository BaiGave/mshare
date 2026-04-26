/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractListeningExecutorService;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

@J2ktIncompatible
@GwtIncompatible
final class DirectExecutorService
extends AbstractListeningExecutorService {
    private final Object lock = new Object();
    @GuardedBy(value="lock")
    private int runningTasks = 0;
    @GuardedBy(value="lock")
    private boolean shutdown = false;

    DirectExecutorService() {
    }

    @Override
    public void execute(Runnable command) {
        this.startTask();
        try {
            command.run();
        }
        finally {
            this.endTask();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isShutdown() {
        Object object = this.lock;
        synchronized (object) {
            return this.shutdown;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void shutdown() {
        Object object = this.lock;
        synchronized (object) {
            this.shutdown = true;
            if (this.runningTasks == 0) {
                this.lock.notifyAll();
            }
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        this.shutdown();
        return ImmutableList.of();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isTerminated() {
        Object object = this.lock;
        synchronized (object) {
            return this.shutdown && this.runningTasks == 0;
        }
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        Object object = this.lock;
        synchronized (object) {
            while (true) {
                if (this.shutdown && this.runningTasks == 0) {
                    return true;
                }
                if (nanos <= 0L) {
                    return false;
                }
                long now = System.nanoTime();
                TimeUnit.NANOSECONDS.timedWait(this.lock, nanos);
                nanos -= System.nanoTime() - now;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void startTask() {
        Object object = this.lock;
        synchronized (object) {
            if (this.shutdown) {
                throw new RejectedExecutionException("Executor already shutdown");
            }
            ++this.runningTasks;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void endTask() {
        Object object = this.lock;
        synchronized (object) {
            int numRunning = --this.runningTasks;
            if (numRunning == 0) {
                this.lock.notifyAll();
            }
        }
    }
}

