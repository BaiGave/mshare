/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.cache;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.cache.RemovalListener;
import java.util.concurrent.Executor;

@GwtIncompatible
public final class RemovalListeners {
    private RemovalListeners() {
    }

    public static <K, V> RemovalListener<K, V> asynchronous(RemovalListener<K, V> listener, Executor executor) {
        Preconditions.checkNotNull(listener);
        Preconditions.checkNotNull(executor);
        return notification -> executor.execute(() -> listener.onRemoval(notification));
    }
}

