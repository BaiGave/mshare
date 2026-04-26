/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.doubles;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface DoubleLongBiConsumer
extends BiConsumer<Double, Long> {
    @Override
    public void accept(double var1, long var3);

    @Override
    @Deprecated
    default public void accept(Double key, Long value) {
        this.accept((double)key, (long)value);
    }

    default public DoubleLongBiConsumer andThen(DoubleLongBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Double, Long> andThen(BiConsumer<? super Double, ? super Long> after) {
        return BiConsumer.super.andThen(after);
    }
}

