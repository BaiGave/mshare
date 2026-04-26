/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.booleans;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface BooleanLongBiConsumer
extends BiConsumer<Boolean, Long> {
    @Override
    public void accept(boolean var1, long var2);

    @Override
    @Deprecated
    default public void accept(Boolean key, Long value) {
        this.accept((boolean)key, (long)value);
    }

    default public BooleanLongBiConsumer andThen(BooleanLongBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Boolean, Long> andThen(BiConsumer<? super Boolean, ? super Long> after) {
        return BiConsumer.super.andThen(after);
    }
}

