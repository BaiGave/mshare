/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.floats;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface FloatBooleanBiConsumer
extends BiConsumer<Float, Boolean> {
    @Override
    public void accept(float var1, boolean var2);

    @Override
    @Deprecated
    default public void accept(Float key, Boolean value) {
        this.accept(key.floatValue(), (boolean)value);
    }

    default public FloatBooleanBiConsumer andThen(FloatBooleanBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Float, Boolean> andThen(BiConsumer<? super Float, ? super Boolean> after) {
        return BiConsumer.super.andThen(after);
    }
}

