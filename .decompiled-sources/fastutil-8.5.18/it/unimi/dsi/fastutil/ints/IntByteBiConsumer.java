/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.ints;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface IntByteBiConsumer
extends BiConsumer<Integer, Byte> {
    @Override
    public void accept(int var1, byte var2);

    @Override
    @Deprecated
    default public void accept(Integer key, Byte value) {
        this.accept((int)key, (byte)value);
    }

    default public IntByteBiConsumer andThen(IntByteBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Integer, Byte> andThen(BiConsumer<? super Integer, ? super Byte> after) {
        return BiConsumer.super.andThen(after);
    }
}

