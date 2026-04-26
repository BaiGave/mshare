/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.booleans;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface BooleanObjectBiConsumer<V>
extends BiConsumer<Boolean, V> {
    @Override
    public void accept(boolean var1, V var2);

    @Override
    @Deprecated
    default public void accept(Boolean key, V value) {
        this.accept((boolean)key, value);
    }

    default public BooleanObjectBiConsumer<V> andThen(BooleanObjectBiConsumer<V> after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, (V)v);
            after.accept(k, (V)v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Boolean, V> andThen(BiConsumer<? super Boolean, ? super V> after) {
        return BiConsumer.super.andThen(after);
    }
}

