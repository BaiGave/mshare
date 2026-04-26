/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.floats;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface FloatFloatBiConsumer
extends BiConsumer<Float, Float> {
    @Override
    public void accept(float var1, float var2);

    @Override
    @Deprecated
    default public void accept(Float key, Float value) {
        this.accept(key.floatValue(), value.floatValue());
    }

    default public FloatFloatBiConsumer andThen(FloatFloatBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Float, Float> andThen(BiConsumer<? super Float, ? super Float> after) {
        return BiConsumer.super.andThen(after);
    }
}

