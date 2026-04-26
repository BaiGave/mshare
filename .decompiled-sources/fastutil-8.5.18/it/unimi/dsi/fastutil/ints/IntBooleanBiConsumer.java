/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.ints;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface IntBooleanBiConsumer
extends BiConsumer<Integer, Boolean> {
    @Override
    public void accept(int var1, boolean var2);

    @Override
    @Deprecated
    default public void accept(Integer key, Boolean value) {
        this.accept((int)key, (boolean)value);
    }

    default public IntBooleanBiConsumer andThen(IntBooleanBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Integer, Boolean> andThen(BiConsumer<? super Integer, ? super Boolean> after) {
        return BiConsumer.super.andThen(after);
    }
}

