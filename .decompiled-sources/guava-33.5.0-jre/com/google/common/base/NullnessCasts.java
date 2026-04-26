/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.ParametricNullness;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class NullnessCasts {
    @ParametricNullness
    static <T> T uncheckedCastNullableTToT(@Nullable T t) {
        return t;
    }

    private NullnessCasts() {
    }
}

