/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.chars;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface CharShortBiConsumer
extends BiConsumer<Character, Short> {
    @Override
    public void accept(char var1, short var2);

    @Override
    @Deprecated
    default public void accept(Character key, Short value) {
        this.accept(key.charValue(), (short)value);
    }

    default public CharShortBiConsumer andThen(CharShortBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Character, Short> andThen(BiConsumer<? super Character, ? super Short> after) {
        return BiConsumer.super.andThen(after);
    }
}

