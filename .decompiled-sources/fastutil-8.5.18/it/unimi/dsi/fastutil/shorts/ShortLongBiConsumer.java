/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.shorts;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ShortLongBiConsumer
extends BiConsumer<Short, Long> {
    @Override
    public void accept(short var1, long var2);

    @Override
    @Deprecated
    default public void accept(Short key, Long value) {
        this.accept((short)key, (long)value);
    }

    default public ShortLongBiConsumer andThen(ShortLongBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Short, Long> andThen(BiConsumer<? super Short, ? super Long> after) {
        return BiConsumer.super.andThen(after);
    }
}

