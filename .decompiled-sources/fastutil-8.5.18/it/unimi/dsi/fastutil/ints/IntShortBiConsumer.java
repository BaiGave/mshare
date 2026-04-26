/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.ints;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface IntShortBiConsumer
extends BiConsumer<Integer, Short> {
    @Override
    public void accept(int var1, short var2);

    @Override
    @Deprecated
    default public void accept(Integer key, Short value) {
        this.accept((int)key, (short)value);
    }

    default public IntShortBiConsumer andThen(IntShortBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Integer, Short> andThen(BiConsumer<? super Integer, ? super Short> after) {
        return BiConsumer.super.andThen(after);
    }
}

