/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.doubles;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface DoubleObjectBiConsumer<V>
extends BiConsumer<Double, V> {
    @Override
    public void accept(double var1, V var3);

    @Override
    @Deprecated
    default public void accept(Double key, V value) {
        this.accept((double)key, value);
    }

    default public DoubleObjectBiConsumer<V> andThen(DoubleObjectBiConsumer<V> after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, (V)v);
            after.accept(k, (V)v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Double, V> andThen(BiConsumer<? super Double, ? super V> after) {
        return BiConsumer.super.andThen(after);
    }
}

