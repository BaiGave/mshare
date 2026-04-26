/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.bytes;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ByteCharBiConsumer
extends BiConsumer<Byte, Character> {
    @Override
    public void accept(byte var1, char var2);

    @Override
    @Deprecated
    default public void accept(Byte key, Character value) {
        this.accept((byte)key, value.charValue());
    }

    default public ByteCharBiConsumer andThen(ByteCharBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Byte, Character> andThen(BiConsumer<? super Byte, ? super Character> after) {
        return BiConsumer.super.andThen(after);
    }
}

