/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.booleans;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface BooleanByteBiConsumer
extends BiConsumer<Boolean, Byte> {
    @Override
    public void accept(boolean var1, byte var2);

    @Override
    @Deprecated
    default public void accept(Boolean key, Byte value) {
        this.accept((boolean)key, (byte)value);
    }

    default public BooleanByteBiConsumer andThen(BooleanByteBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Boolean, Byte> andThen(BiConsumer<? super Boolean, ? super Byte> after) {
        return BiConsumer.super.andThen(after);
    }
}

