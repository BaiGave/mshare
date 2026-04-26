/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;

public class LazyBoolean
implements BooleanSupplier {
    private final BooleanSupplier supplier;
    private final Lock lock = new ReentrantLock();
    private volatile boolean initialized;
    private volatile boolean value;

    public LazyBoolean(BooleanSupplier supplier) {
        this.supplier = supplier;
    }

    @Override
    public boolean getAsBoolean() {
        boolean uninitialized = !this.initialized;
        boolean value = this.value;
        if (uninitialized) {
            this.lock.lock();
            try {
                boolean bl = uninitialized = !this.initialized;
                if (uninitialized) {
                    this.value = value = this.supplier.getAsBoolean();
                    this.initialized = true;
                }
            }
            finally {
                this.lock.unlock();
            }
        }
        return value;
    }

    public void setAsBoolean(boolean b) {
        this.lock.lock();
        try {
            this.initialized = false;
            this.value = b;
            this.initialized = true;
        }
        finally {
            this.lock.unlock();
        }
    }

    public void reset() {
        this.initialized = false;
    }
}

