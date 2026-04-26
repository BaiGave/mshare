/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.floats;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface FloatCharBiConsumer
extends BiConsumer<Float, Character> {
    @Override
    public void accept(float var1, char var2);

    @Override
    @Deprecated
    default public void accept(Float key, Character value) {
        this.accept(key.floatValue(), value.charValue());
    }

    default public FloatCharBiConsumer andThen(FloatCharBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Float, Character> andThen(BiConsumer<? super Float, ? super Character> after) {
        return BiConsumer.super.andThen(after);
    }
}

