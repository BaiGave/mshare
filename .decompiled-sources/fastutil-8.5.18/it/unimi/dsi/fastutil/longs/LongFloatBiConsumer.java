/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.longs;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface LongFloatBiConsumer
extends BiConsumer<Long, Float> {
    @Override
    public void accept(long var1, float var3);

    @Override
    @Deprecated
    default public void accept(Long key, Float value) {
        this.accept((long)key, value.floatValue());
    }

    default public LongFloatBiConsumer andThen(LongFloatBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Long, Float> andThen(BiConsumer<? super Long, ? super Float> after) {
        return BiConsumer.super.andThen(after);
    }
}

