/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.ints;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface IntIntBiConsumer
extends BiConsumer<Integer, Integer> {
    @Override
    public void accept(int var1, int var2);

    @Override
    @Deprecated
    default public void accept(Integer key, Integer value) {
        this.accept((int)key, (int)value);
    }

    default public IntIntBiConsumer andThen(IntIntBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Integer, Integer> andThen(BiConsumer<? super Integer, ? super Integer> after) {
        return BiConsumer.super.andThen(after);
    }
}

