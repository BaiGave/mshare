/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.doubles;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface DoubleBooleanBiConsumer
extends BiConsumer<Double, Boolean> {
    @Override
    public void accept(double var1, boolean var3);

    @Override
    @Deprecated
    default public void accept(Double key, Boolean value) {
        this.accept((double)key, (boolean)value);
    }

    default public DoubleBooleanBiConsumer andThen(DoubleBooleanBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Double, Boolean> andThen(BiConsumer<? super Double, ? super Boolean> after) {
        return BiConsumer.super.andThen(after);
    }
}

