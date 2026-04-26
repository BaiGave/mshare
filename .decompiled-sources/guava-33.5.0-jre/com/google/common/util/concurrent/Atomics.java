/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.util.concurrent.ParametricNullness;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import org.jspecify.annotations.Nullable;

@GwtIncompatible
public final class Atomics {
    private Atomics() {
    }

    public static <V> AtomicReference<@Nullable V> newReference() {
        return new AtomicReference();
    }

    public static <V> AtomicReference<V> newReference(@ParametricNullness V initialValue) {
        return new AtomicReference<V>(initialValue);
    }

    public static <E> AtomicReferenceArray<@Nullable E> newReferenceArray(int length) {
        return new AtomicReferenceArray(length);
    }

    public static <E> AtomicReferenceArray<E> newReferenceArray(E[] array) {
        return new AtomicReferenceArray<E>(array);
    }
}

