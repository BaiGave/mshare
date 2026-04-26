/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ParametricNullness;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class NullnessCasts {
    @ParametricNullness
    static <T> T uncheckedCastNullableTToT(@Nullable T t) {
        return t;
    }

    @ParametricNullness
    static <T> T unsafeNull() {
        return null;
    }

    private NullnessCasts() {
    }
}

