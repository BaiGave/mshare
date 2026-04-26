/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import java.util.logging.Logger;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class LazyLogger {
    private final Object lock = new Object();
    private final String loggerName;
    private volatile @Nullable Logger logger;

    LazyLogger(Class<?> ownerOfLogger) {
        this.loggerName = ownerOfLogger.getName();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    Logger get() {
        Logger local = this.logger;
        if (local != null) {
            return local;
        }
        Object object = this.lock;
        synchronized (object) {
            local = this.logger;
            if (local != null) {
                return local;
            }
            this.logger = Logger.getLogger(this.loggerName);
            return this.logger;
        }
    }
}

