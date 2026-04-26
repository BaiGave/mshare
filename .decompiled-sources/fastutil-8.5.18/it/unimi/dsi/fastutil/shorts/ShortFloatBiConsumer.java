/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.shorts;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ShortFloatBiConsumer
extends BiConsumer<Short, Float> {
    @Override
    public void accept(short var1, float var2);

    @Override
    @Deprecated
    default public void accept(Short key, Float value) {
        this.accept((short)key, value.floatValue());
    }

    default public ShortFloatBiConsumer andThen(ShortFloatBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Short, Float> andThen(BiConsumer<? super Short, ? super Float> after) {
        return BiConsumer.super.andThen(after);
    }
}

