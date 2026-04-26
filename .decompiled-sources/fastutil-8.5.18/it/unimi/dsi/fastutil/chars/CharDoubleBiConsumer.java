/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.chars;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface CharDoubleBiConsumer
extends BiConsumer<Character, Double> {
    @Override
    public void accept(char var1, double var2);

    @Override
    @Deprecated
    default public void accept(Character key, Double value) {
        this.accept(key.charValue(), (double)value);
    }

    default public CharDoubleBiConsumer andThen(CharDoubleBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Character, Double> andThen(BiConsumer<? super Character, ? super Double> after) {
        return BiConsumer.super.andThen(after);
    }
}

