/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.chars;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface CharCharBiConsumer
extends BiConsumer<Character, Character> {
    @Override
    public void accept(char var1, char var2);

    @Override
    @Deprecated
    default public void accept(Character key, Character value) {
        this.accept(key.charValue(), value.charValue());
    }

    default public CharCharBiConsumer andThen(CharCharBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Character, Character> andThen(BiConsumer<? super Character, ? super Character> after) {
        return BiConsumer.super.andThen(after);
    }
}

