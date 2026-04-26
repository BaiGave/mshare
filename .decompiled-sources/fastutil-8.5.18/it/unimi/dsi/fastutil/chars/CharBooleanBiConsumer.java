/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.chars;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface CharBooleanBiConsumer
extends BiConsumer<Character, Boolean> {
    @Override
    public void accept(char var1, boolean var2);

    @Override
    @Deprecated
    default public void accept(Character key, Boolean value) {
        this.accept(key.charValue(), (boolean)value);
    }

    default public CharBooleanBiConsumer andThen(CharBooleanBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Character, Boolean> andThen(BiConsumer<? super Character, ? super Boolean> after) {
        return BiConsumer.super.andThen(after);
    }
}

