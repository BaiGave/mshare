/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.collect.ForwardingQueue;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public abstract class ForwardingBlockingQueue<E>
extends ForwardingQueue<E>
implements BlockingQueue<E> {
    protected ForwardingBlockingQueue() {
    }

    @Override
    protected abstract BlockingQueue<E> delegate();

    @Override
    @CanIgnoreReturnValue
    public int drainTo(Collection<? super E> c, int maxElements) {
        return this.delegate().drainTo(c, maxElements);
    }

    @Override
    @CanIgnoreReturnValue
    public int drainTo(Collection<? super E> c) {
        return this.delegate().drainTo(c);
    }

    @Override
    @CanIgnoreReturnValue
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return this.delegate().offer(e, timeout, unit);
    }

    @Override
    @CanIgnoreReturnValue
    public @Nullable E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return this.delegate().poll(timeout, unit);
    }

    @Override
    public void put(E e) throws InterruptedException {
        this.delegate().put(e);
    }

    @Override
    public int remainingCapacity() {
        return this.delegate().remainingCapacity();
    }

    @Override
    @CanIgnoreReturnValue
    public E take() throws InterruptedException {
        return this.delegate().take();
    }
}

