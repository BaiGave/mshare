/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.util.concurrent.ParametricNullness;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class NullnessCasts {
    @ParametricNullness
    static <T> T uncheckedCastNullableTToT(@Nullable T t) {
        return t;
    }

    @ParametricNullness
    static <T> T uncheckedNull() {
        return null;
    }

    private NullnessCasts() {
    }
}

