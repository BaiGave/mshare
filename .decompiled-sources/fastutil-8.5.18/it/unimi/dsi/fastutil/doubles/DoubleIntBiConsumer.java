/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.doubles;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface DoubleIntBiConsumer
extends BiConsumer<Double, Integer> {
    @Override
    public void accept(double var1, int var3);

    @Override
    @Deprecated
    default public void accept(Double key, Integer value) {
        this.accept((double)key, (int)value);
    }

    default public DoubleIntBiConsumer andThen(DoubleIntBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Double, Integer> andThen(BiConsumer<? super Double, ? super Integer> after) {
        return BiConsumer.super.andThen(after);
    }
}

