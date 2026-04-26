/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.chars;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface CharByteBiConsumer
extends BiConsumer<Character, Byte> {
    @Override
    public void accept(char var1, byte var2);

    @Override
    @Deprecated
    default public void accept(Character key, Byte value) {
        this.accept(key.charValue(), (byte)value);
    }

    default public CharByteBiConsumer andThen(CharByteBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Character, Byte> andThen(BiConsumer<? super Character, ? super Byte> after) {
        return BiConsumer.super.andThen(after);
    }
}

