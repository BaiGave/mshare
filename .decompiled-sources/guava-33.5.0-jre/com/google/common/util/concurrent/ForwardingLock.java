/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

@J2ktIncompatible
@GwtIncompatible
abstract class ForwardingLock
implements Lock {
    ForwardingLock() {
    }

    abstract Lock delegate();

    @Override
    public void lock() {
        this.delegate().lock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        this.delegate().lockInterruptibly();
    }

    @Override
    public boolean tryLock() {
        return this.delegate().tryLock();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return this.delegate().tryLock(time, unit);
    }

    @Override
    public void unlock() {
        this.delegate().unlock();
    }

    @Override
    public Condition newCondition() {
        return this.delegate().newCondition();
    }
}

