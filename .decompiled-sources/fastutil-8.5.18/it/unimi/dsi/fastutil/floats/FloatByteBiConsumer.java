/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.floats;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface FloatByteBiConsumer
extends BiConsumer<Float, Byte> {
    @Override
    public void accept(float var1, byte var2);

    @Override
    @Deprecated
    default public void accept(Float key, Byte value) {
        this.accept(key.floatValue(), (byte)value);
    }

    default public FloatByteBiConsumer andThen(FloatByteBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Float, Byte> andThen(BiConsumer<? super Float, ? super Byte> after) {
        return BiConsumer.super.andThen(after);
    }
}

