/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.booleans;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface BooleanDoubleBiConsumer
extends BiConsumer<Boolean, Double> {
    @Override
    public void accept(boolean var1, double var2);

    @Override
    @Deprecated
    default public void accept(Boolean key, Double value) {
        this.accept((boolean)key, (double)value);
    }

    default public BooleanDoubleBiConsumer andThen(BooleanDoubleBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Boolean, Double> andThen(BiConsumer<? super Boolean, ? super Double> after) {
        return BiConsumer.super.andThen(after);
    }
}

