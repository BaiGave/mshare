/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.bytes;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ByteObjectBiConsumer<V>
extends BiConsumer<Byte, V> {
    @Override
    public void accept(byte var1, V var2);

    @Override
    @Deprecated
    default public void accept(Byte key, V value) {
        this.accept((byte)key, value);
    }

    default public ByteObjectBiConsumer<V> andThen(ByteObjectBiConsumer<V> after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, (V)v);
            after.accept(k, (V)v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Byte, V> andThen(BiConsumer<? super Byte, ? super V> after) {
        return BiConsumer.super.andThen(after);
    }
}

