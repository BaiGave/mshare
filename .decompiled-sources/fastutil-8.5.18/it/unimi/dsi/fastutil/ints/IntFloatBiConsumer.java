/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.ints;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface IntFloatBiConsumer
extends BiConsumer<Integer, Float> {
    @Override
    public void accept(int var1, float var2);

    @Override
    @Deprecated
    default public void accept(Integer key, Float value) {
        this.accept((int)key, value.floatValue());
    }

    default public IntFloatBiConsumer andThen(IntFloatBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Integer, Float> andThen(BiConsumer<? super Integer, ? super Float> after) {
        return BiConsumer.super.andThen(after);
    }
}

