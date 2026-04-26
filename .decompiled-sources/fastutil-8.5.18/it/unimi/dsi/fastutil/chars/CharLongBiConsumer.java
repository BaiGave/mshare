/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.chars;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface CharLongBiConsumer
extends BiConsumer<Character, Long> {
    @Override
    public void accept(char var1, long var2);

    @Override
    @Deprecated
    default public void accept(Character key, Long value) {
        this.accept(key.charValue(), (long)value);
    }

    default public CharLongBiConsumer andThen(CharLongBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Character, Long> andThen(BiConsumer<? super Character, ? super Long> after) {
        return BiConsumer.super.andThen(after);
    }
}

