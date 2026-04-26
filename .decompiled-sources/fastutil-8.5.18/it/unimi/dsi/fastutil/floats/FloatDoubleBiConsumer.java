/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.floats;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface FloatDoubleBiConsumer
extends BiConsumer<Float, Double> {
    @Override
    public void accept(float var1, double var2);

    @Override
    @Deprecated
    default public void accept(Float key, Double value) {
        this.accept(key.floatValue(), (double)value);
    }

    default public FloatDoubleBiConsumer andThen(FloatDoubleBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Float, Double> andThen(BiConsumer<? super Float, ? super Double> after) {
        return BiConsumer.super.andThen(after);
    }
}

