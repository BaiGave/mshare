/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.shorts;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ShortIntBiConsumer
extends BiConsumer<Short, Integer> {
    @Override
    public void accept(short var1, int var2);

    @Override
    @Deprecated
    default public void accept(Short key, Integer value) {
        this.accept((short)key, (int)value);
    }

    default public ShortIntBiConsumer andThen(ShortIntBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Short, Integer> andThen(BiConsumer<? super Short, ? super Integer> after) {
        return BiConsumer.super.andThen(after);
    }
}

