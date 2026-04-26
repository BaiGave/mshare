/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.doubles;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface DoubleShortBiConsumer
extends BiConsumer<Double, Short> {
    @Override
    public void accept(double var1, short var3);

    @Override
    @Deprecated
    default public void accept(Double key, Short value) {
        this.accept((double)key, (short)value);
    }

    default public DoubleShortBiConsumer andThen(DoubleShortBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Double, Short> andThen(BiConsumer<? super Double, ? super Short> after) {
        return BiConsumer.super.andThen(after);
    }
}

