/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.doubles;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface DoubleFloatBiConsumer
extends BiConsumer<Double, Float> {
    @Override
    public void accept(double var1, float var3);

    @Override
    @Deprecated
    default public void accept(Double key, Float value) {
        this.accept((double)key, value.floatValue());
    }

    default public DoubleFloatBiConsumer andThen(DoubleFloatBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Double, Float> andThen(BiConsumer<? super Double, ? super Float> after) {
        return BiConsumer.super.andThen(after);
    }
}

