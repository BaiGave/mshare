/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.doubles;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface DoubleCharBiConsumer
extends BiConsumer<Double, Character> {
    @Override
    public void accept(double var1, char var3);

    @Override
    @Deprecated
    default public void accept(Double key, Character value) {
        this.accept((double)key, value.charValue());
    }

    default public DoubleCharBiConsumer andThen(DoubleCharBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Double, Character> andThen(BiConsumer<? super Double, ? super Character> after) {
        return BiConsumer.super.andThen(after);
    }
}

