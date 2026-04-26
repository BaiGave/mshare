/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.shorts;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ShortObjectBiConsumer<V>
extends BiConsumer<Short, V> {
    @Override
    public void accept(short var1, V var2);

    @Override
    @Deprecated
    default public void accept(Short key, V value) {
        this.accept((short)key, value);
    }

    default public ShortObjectBiConsumer<V> andThen(ShortObjectBiConsumer<V> after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, (V)v);
            after.accept(k, (V)v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Short, V> andThen(BiConsumer<? super Short, ? super V> after) {
        return BiConsumer.super.andThen(after);
    }
}

