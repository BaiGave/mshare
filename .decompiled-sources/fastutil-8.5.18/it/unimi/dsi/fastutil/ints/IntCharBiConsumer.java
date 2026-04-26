/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.ints;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface IntCharBiConsumer
extends BiConsumer<Integer, Character> {
    @Override
    public void accept(int var1, char var2);

    @Override
    @Deprecated
    default public void accept(Integer key, Character value) {
        this.accept((int)key, value.charValue());
    }

    default public IntCharBiConsumer andThen(IntCharBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Integer, Character> andThen(BiConsumer<? super Integer, ? super Character> after) {
        return BiConsumer.super.andThen(after);
    }
}

