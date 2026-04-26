/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.longs;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface LongDoubleBiConsumer
extends BiConsumer<Long, Double> {
    @Override
    public void accept(long var1, double var3);

    @Override
    @Deprecated
    default public void accept(Long key, Double value) {
        this.accept((long)key, (double)value);
    }

    default public LongDoubleBiConsumer andThen(LongDoubleBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Long, Double> andThen(BiConsumer<? super Long, ? super Double> after) {
        return BiConsumer.super.andThen(after);
    }
}

