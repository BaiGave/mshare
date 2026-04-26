/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.bytes;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ByteIntBiConsumer
extends BiConsumer<Byte, Integer> {
    @Override
    public void accept(byte var1, int var2);

    @Override
    @Deprecated
    default public void accept(Byte key, Integer value) {
        this.accept((byte)key, (int)value);
    }

    default public ByteIntBiConsumer andThen(ByteIntBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Byte, Integer> andThen(BiConsumer<? super Byte, ? super Integer> after) {
        return BiConsumer.super.andThen(after);
    }
}

