/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.ints;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface IntDoubleBiConsumer
extends BiConsumer<Integer, Double> {
    @Override
    public void accept(int var1, double var2);

    @Override
    @Deprecated
    default public void accept(Integer key, Double value) {
        this.accept((int)key, (double)value);
    }

    default public IntDoubleBiConsumer andThen(IntDoubleBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Integer, Double> andThen(BiConsumer<? super Integer, ? super Double> after) {
        return BiConsumer.super.andThen(after);
    }
}

