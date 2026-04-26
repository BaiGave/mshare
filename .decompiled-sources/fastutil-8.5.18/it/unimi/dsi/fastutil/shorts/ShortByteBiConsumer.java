/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.shorts;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ShortByteBiConsumer
extends BiConsumer<Short, Byte> {
    @Override
    public void accept(short var1, byte var2);

    @Override
    @Deprecated
    default public void accept(Short key, Byte value) {
        this.accept((short)key, (byte)value);
    }

    default public ShortByteBiConsumer andThen(ShortByteBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Short, Byte> andThen(BiConsumer<? super Short, ? super Byte> after) {
        return BiConsumer.super.andThen(after);
    }
}

