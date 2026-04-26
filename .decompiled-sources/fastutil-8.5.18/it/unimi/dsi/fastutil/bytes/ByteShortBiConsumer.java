/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.bytes;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ByteShortBiConsumer
extends BiConsumer<Byte, Short> {
    @Override
    public void accept(byte var1, short var2);

    @Override
    @Deprecated
    default public void accept(Byte key, Short value) {
        this.accept((byte)key, (short)value);
    }

    default public ByteShortBiConsumer andThen(ByteShortBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Byte, Short> andThen(BiConsumer<? super Byte, ? super Short> after) {
        return BiConsumer.super.andThen(after);
    }
}

