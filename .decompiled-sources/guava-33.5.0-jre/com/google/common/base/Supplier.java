/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.ParametricNullness;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
@GwtCompatible
public interface Supplier<T>
extends java.util.function.Supplier<T> {
    @Override
    @ParametricNullness
    public T get();

    public boolean equals(@Nullable Object var1);
}

