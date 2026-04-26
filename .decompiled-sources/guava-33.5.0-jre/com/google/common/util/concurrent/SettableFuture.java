/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ParametricNullness;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible
public final class SettableFuture<V>
extends AbstractFuture.TrustedFuture<V> {
    public static <V> SettableFuture<V> create() {
        return new SettableFuture<V>();
    }

    @Override
    @CanIgnoreReturnValue
    public boolean set(@ParametricNullness V value) {
        return super.set(value);
    }

    @Override
    @CanIgnoreReturnValue
    public boolean setException(Throwable throwable) {
        return super.setException(throwable);
    }

    @Override
    @CanIgnoreReturnValue
    public boolean setFuture(ListenableFuture<? extends V> future) {
        return super.setFuture(future);
    }

    private SettableFuture() {
    }
}

