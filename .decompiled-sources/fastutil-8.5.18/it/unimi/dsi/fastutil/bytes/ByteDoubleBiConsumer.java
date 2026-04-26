/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.bytes;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ByteDoubleBiConsumer
extends BiConsumer<Byte, Double> {
    @Override
    public void accept(byte var1, double var2);

    @Override
    @Deprecated
    default public void accept(Byte key, Double value) {
        this.accept((byte)key, (double)value);
    }

    default public ByteDoubleBiConsumer andThen(ByteDoubleBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Byte, Double> andThen(BiConsumer<? super Byte, ? super Double> after) {
        return BiConsumer.super.andThen(after);
    }
}

