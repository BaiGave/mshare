/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.floats;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface FloatObjectBiConsumer<V>
extends BiConsumer<Float, V> {
    @Override
    public void accept(float var1, V var2);

    @Override
    @Deprecated
    default public void accept(Float key, V value) {
        this.accept(key.floatValue(), value);
    }

    default public FloatObjectBiConsumer<V> andThen(FloatObjectBiConsumer<V> after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, (V)v);
            after.accept(k, (V)v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Float, V> andThen(BiConsumer<? super Float, ? super V> after) {
        return BiConsumer.super.andThen(after);
    }
}

