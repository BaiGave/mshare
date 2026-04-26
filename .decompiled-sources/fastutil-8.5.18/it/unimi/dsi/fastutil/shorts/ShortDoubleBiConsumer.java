/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.shorts;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ShortDoubleBiConsumer
extends BiConsumer<Short, Double> {
    @Override
    public void accept(short var1, double var2);

    @Override
    @Deprecated
    default public void accept(Short key, Double value) {
        this.accept((short)key, (double)value);
    }

    default public ShortDoubleBiConsumer andThen(ShortDoubleBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Short, Double> andThen(BiConsumer<? super Short, ? super Double> after) {
        return BiConsumer.super.andThen(after);
    }
}

