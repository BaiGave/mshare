/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.shorts;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ShortCharBiConsumer
extends BiConsumer<Short, Character> {
    @Override
    public void accept(short var1, char var2);

    @Override
    @Deprecated
    default public void accept(Short key, Character value) {
        this.accept((short)key, value.charValue());
    }

    default public ShortCharBiConsumer andThen(ShortCharBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Short, Character> andThen(BiConsumer<? super Short, ? super Character> after) {
        return BiConsumer.super.andThen(after);
    }
}

