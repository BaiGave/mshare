/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.ParametricNullness;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
@GwtCompatible
public interface Predicate<T>
extends java.util.function.Predicate<T> {
    public boolean apply(@ParametricNullness T var1);

    public boolean equals(@Nullable Object var1);

    @Override
    default public boolean test(@ParametricNullness T input) {
        return this.apply(input);
    }
}

