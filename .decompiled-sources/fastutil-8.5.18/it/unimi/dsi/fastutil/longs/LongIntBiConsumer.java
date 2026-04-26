/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.longs;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface LongIntBiConsumer
extends BiConsumer<Long, Integer> {
    @Override
    public void accept(long var1, int var3);

    @Override
    @Deprecated
    default public void accept(Long key, Integer value) {
        this.accept((long)key, (int)value);
    }

    default public LongIntBiConsumer andThen(LongIntBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Long, Integer> andThen(BiConsumer<? super Long, ? super Integer> after) {
        return BiConsumer.super.andThen(after);
    }
}

