/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.booleans;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface BooleanIntBiConsumer
extends BiConsumer<Boolean, Integer> {
    @Override
    public void accept(boolean var1, int var2);

    @Override
    @Deprecated
    default public void accept(Boolean key, Integer value) {
        this.accept((boolean)key, (int)value);
    }

    default public BooleanIntBiConsumer andThen(BooleanIntBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Boolean, Integer> andThen(BiConsumer<? super Boolean, ? super Integer> after) {
        return BiConsumer.super.andThen(after);
    }
}

