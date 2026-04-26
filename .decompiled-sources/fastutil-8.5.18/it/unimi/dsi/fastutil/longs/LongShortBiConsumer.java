/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.longs;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface LongShortBiConsumer
extends BiConsumer<Long, Short> {
    @Override
    public void accept(long var1, short var3);

    @Override
    @Deprecated
    default public void accept(Long key, Short value) {
        this.accept((long)key, (short)value);
    }

    default public LongShortBiConsumer andThen(LongShortBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Long, Short> andThen(BiConsumer<? super Long, ? super Short> after) {
        return BiConsumer.super.andThen(after);
    }
}

