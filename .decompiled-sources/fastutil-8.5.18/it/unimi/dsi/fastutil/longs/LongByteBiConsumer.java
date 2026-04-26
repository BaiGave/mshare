/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.longs;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface LongByteBiConsumer
extends BiConsumer<Long, Byte> {
    @Override
    public void accept(long var1, byte var3);

    @Override
    @Deprecated
    default public void accept(Long key, Byte value) {
        this.accept((long)key, (byte)value);
    }

    default public LongByteBiConsumer andThen(LongByteBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Long, Byte> andThen(BiConsumer<? super Long, ? super Byte> after) {
        return BiConsumer.super.andThen(after);
    }
}

