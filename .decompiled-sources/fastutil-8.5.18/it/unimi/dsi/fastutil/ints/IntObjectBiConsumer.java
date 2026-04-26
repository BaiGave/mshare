/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.ints;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface IntObjectBiConsumer<V>
extends BiConsumer<Integer, V> {
    @Override
    public void accept(int var1, V var2);

    @Override
    @Deprecated
    default public void accept(Integer key, V value) {
        this.accept((int)key, value);
    }

    default public IntObjectBiConsumer<V> andThen(IntObjectBiConsumer<V> after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, (V)v);
            after.accept(k, (V)v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Integer, V> andThen(BiConsumer<? super Integer, ? super V> after) {
        return BiConsumer.super.andThen(after);
    }
}

