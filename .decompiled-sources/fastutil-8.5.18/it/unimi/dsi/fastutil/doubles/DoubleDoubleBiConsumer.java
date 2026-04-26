/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.doubles;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface DoubleDoubleBiConsumer
extends BiConsumer<Double, Double> {
    @Override
    public void accept(double var1, double var3);

    @Override
    @Deprecated
    default public void accept(Double key, Double value) {
        this.accept((double)key, (double)value);
    }

    default public DoubleDoubleBiConsumer andThen(DoubleDoubleBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Double, Double> andThen(BiConsumer<? super Double, ? super Double> after) {
        return BiConsumer.super.andThen(after);
    }
}

