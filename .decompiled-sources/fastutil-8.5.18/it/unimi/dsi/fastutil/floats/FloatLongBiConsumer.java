/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.floats;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface FloatLongBiConsumer
extends BiConsumer<Float, Long> {
    @Override
    public void accept(float var1, long var2);

    @Override
    @Deprecated
    default public void accept(Float key, Long value) {
        this.accept(key.floatValue(), (long)value);
    }

    default public FloatLongBiConsumer andThen(FloatLongBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Float, Long> andThen(BiConsumer<? super Float, ? super Long> after) {
        return BiConsumer.super.andThen(after);
    }
}

