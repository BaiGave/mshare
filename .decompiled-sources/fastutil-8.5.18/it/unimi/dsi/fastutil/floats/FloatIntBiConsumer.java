/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.floats;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface FloatIntBiConsumer
extends BiConsumer<Float, Integer> {
    @Override
    public void accept(float var1, int var2);

    @Override
    @Deprecated
    default public void accept(Float key, Integer value) {
        this.accept(key.floatValue(), (int)value);
    }

    default public FloatIntBiConsumer andThen(FloatIntBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Float, Integer> andThen(BiConsumer<? super Float, ? super Integer> after) {
        return BiConsumer.super.andThen(after);
    }
}

