/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.floats;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface FloatShortBiConsumer
extends BiConsumer<Float, Short> {
    @Override
    public void accept(float var1, short var2);

    @Override
    @Deprecated
    default public void accept(Float key, Short value) {
        this.accept(key.floatValue(), (short)value);
    }

    default public FloatShortBiConsumer andThen(FloatShortBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Float, Short> andThen(BiConsumer<? super Float, ? super Short> after) {
        return BiConsumer.super.andThen(after);
    }
}

