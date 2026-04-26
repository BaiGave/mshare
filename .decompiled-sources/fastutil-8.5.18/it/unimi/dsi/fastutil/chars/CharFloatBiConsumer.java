/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.chars;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface CharFloatBiConsumer
extends BiConsumer<Character, Float> {
    @Override
    public void accept(char var1, float var2);

    @Override
    @Deprecated
    default public void accept(Character key, Float value) {
        this.accept(key.charValue(), value.floatValue());
    }

    default public CharFloatBiConsumer andThen(CharFloatBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Character, Float> andThen(BiConsumer<? super Character, ? super Float> after) {
        return BiConsumer.super.andThen(after);
    }
}

