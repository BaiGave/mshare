/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.longs;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface LongBooleanBiConsumer
extends BiConsumer<Long, Boolean> {
    @Override
    public void accept(long var1, boolean var3);

    @Override
    @Deprecated
    default public void accept(Long key, Boolean value) {
        this.accept((long)key, (boolean)value);
    }

    default public LongBooleanBiConsumer andThen(LongBooleanBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Long, Boolean> andThen(BiConsumer<? super Long, ? super Boolean> after) {
        return BiConsumer.super.andThen(after);
    }
}

