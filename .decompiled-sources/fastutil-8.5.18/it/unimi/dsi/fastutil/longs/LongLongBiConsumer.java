/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.longs;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface LongLongBiConsumer
extends BiConsumer<Long, Long> {
    @Override
    public void accept(long var1, long var3);

    @Override
    @Deprecated
    default public void accept(Long key, Long value) {
        this.accept((long)key, (long)value);
    }

    default public LongLongBiConsumer andThen(LongLongBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Long, Long> andThen(BiConsumer<? super Long, ? super Long> after) {
        return BiConsumer.super.andThen(after);
    }
}

