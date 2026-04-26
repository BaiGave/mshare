/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.longs;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface LongObjectBiConsumer<V>
extends BiConsumer<Long, V> {
    @Override
    public void accept(long var1, V var3);

    @Override
    @Deprecated
    default public void accept(Long key, V value) {
        this.accept((long)key, value);
    }

    default public LongObjectBiConsumer<V> andThen(LongObjectBiConsumer<V> after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, (V)v);
            after.accept(k, (V)v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Long, V> andThen(BiConsumer<? super Long, ? super V> after) {
        return BiConsumer.super.andThen(after);
    }
}

