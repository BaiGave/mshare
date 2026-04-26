/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.chars;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface CharIntBiConsumer
extends BiConsumer<Character, Integer> {
    @Override
    public void accept(char var1, int var2);

    @Override
    @Deprecated
    default public void accept(Character key, Integer value) {
        this.accept(key.charValue(), (int)value);
    }

    default public CharIntBiConsumer andThen(CharIntBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Character, Integer> andThen(BiConsumer<? super Character, ? super Integer> after) {
        return BiConsumer.super.andThen(after);
    }
}

