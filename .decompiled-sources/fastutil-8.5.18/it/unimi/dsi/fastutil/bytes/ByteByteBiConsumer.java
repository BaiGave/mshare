/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.bytes;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ByteByteBiConsumer
extends BiConsumer<Byte, Byte> {
    @Override
    public void accept(byte var1, byte var2);

    @Override
    @Deprecated
    default public void accept(Byte key, Byte value) {
        this.accept((byte)key, (byte)value);
    }

    default public ByteByteBiConsumer andThen(ByteByteBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Byte, Byte> andThen(BiConsumer<? super Byte, ? super Byte> after) {
        return BiConsumer.super.andThen(after);
    }
}

