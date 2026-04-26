/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.ParametricNullness;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
@GwtCompatible
public interface Function<F, T>
extends java.util.function.Function<F, T> {
    @Override
    @ParametricNullness
    public T apply(@ParametricNullness F var1);

    public boolean equals(@Nullable Object var1);
}

