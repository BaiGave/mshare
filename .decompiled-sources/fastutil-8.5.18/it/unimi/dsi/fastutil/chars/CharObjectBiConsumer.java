/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.chars;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface CharObjectBiConsumer<V>
extends BiConsumer<Character, V> {
    @Override
    public void accept(char var1, V var2);

    @Override
    @Deprecated
    default public void accept(Character key, V value) {
        this.accept(key.charValue(), value);
    }

    default public CharObjectBiConsumer<V> andThen(CharObjectBiConsumer<V> after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, (V)v);
            after.accept(k, (V)v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Character, V> andThen(BiConsumer<? super Character, ? super V> after) {
        return BiConsumer.super.andThen(after);
    }
}

