/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.doubles;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface DoubleByteBiConsumer
extends BiConsumer<Double, Byte> {
    @Override
    public void accept(double var1, byte var3);

    @Override
    @Deprecated
    default public void accept(Double key, Byte value) {
        this.accept((double)key, (byte)value);
    }

    default public DoubleByteBiConsumer andThen(DoubleByteBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Double, Byte> andThen(BiConsumer<? super Double, ? super Byte> after) {
        return BiConsumer.super.andThen(after);
    }
}

