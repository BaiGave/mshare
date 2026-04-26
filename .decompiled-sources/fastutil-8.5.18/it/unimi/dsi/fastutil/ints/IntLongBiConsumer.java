/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.ints;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface IntLongBiConsumer
extends BiConsumer<Integer, Long> {
    @Override
    public void accept(int var1, long var2);

    @Override
    @Deprecated
    default public void accept(Integer key, Long value) {
        this.accept((int)key, (long)value);
    }

    default public IntLongBiConsumer andThen(IntLongBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Integer, Long> andThen(BiConsumer<? super Integer, ? super Long> after) {
        return BiConsumer.super.andThen(after);
    }
}

