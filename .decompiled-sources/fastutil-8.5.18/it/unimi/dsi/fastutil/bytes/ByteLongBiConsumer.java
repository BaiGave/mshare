/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.bytes;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ByteLongBiConsumer
extends BiConsumer<Byte, Long> {
    @Override
    public void accept(byte var1, long var2);

    @Override
    @Deprecated
    default public void accept(Byte key, Long value) {
        this.accept((byte)key, (long)value);
    }

    default public ByteLongBiConsumer andThen(ByteLongBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Byte, Long> andThen(BiConsumer<? super Byte, ? super Long> after) {
        return BiConsumer.super.andThen(after);
    }
}

