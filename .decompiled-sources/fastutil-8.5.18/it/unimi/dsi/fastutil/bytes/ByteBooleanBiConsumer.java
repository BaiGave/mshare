/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.bytes;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ByteBooleanBiConsumer
extends BiConsumer<Byte, Boolean> {
    @Override
    public void accept(byte var1, boolean var2);

    @Override
    @Deprecated
    default public void accept(Byte key, Boolean value) {
        this.accept((byte)key, (boolean)value);
    }

    default public ByteBooleanBiConsumer andThen(ByteBooleanBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Byte, Boolean> andThen(BiConsumer<? super Byte, ? super Boolean> after) {
        return BiConsumer.super.andThen(after);
    }
}

