/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.bytes;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ByteFloatBiConsumer
extends BiConsumer<Byte, Float> {
    @Override
    public void accept(byte var1, float var2);

    @Override
    @Deprecated
    default public void accept(Byte key, Float value) {
        this.accept((byte)key, value.floatValue());
    }

    default public ByteFloatBiConsumer andThen(ByteFloatBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Byte, Float> andThen(BiConsumer<? super Byte, ? super Float> after) {
        return BiConsumer.super.andThen(after);
    }
}

