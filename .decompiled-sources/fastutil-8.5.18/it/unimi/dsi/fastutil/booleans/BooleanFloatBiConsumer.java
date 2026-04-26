/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.booleans;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface BooleanFloatBiConsumer
extends BiConsumer<Boolean, Float> {
    @Override
    public void accept(boolean var1, float var2);

    @Override
    @Deprecated
    default public void accept(Boolean key, Float value) {
        this.accept((boolean)key, value.floatValue());
    }

    default public BooleanFloatBiConsumer andThen(BooleanFloatBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Boolean, Float> andThen(BiConsumer<? super Boolean, ? super Float> after) {
        return BiConsumer.super.andThen(after);
    }
}

