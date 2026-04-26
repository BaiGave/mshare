/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.shorts;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ShortShortBiConsumer
extends BiConsumer<Short, Short> {
    @Override
    public void accept(short var1, short var2);

    @Override
    @Deprecated
    default public void accept(Short key, Short value) {
        this.accept((short)key, (short)value);
    }

    default public ShortShortBiConsumer andThen(ShortShortBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Short, Short> andThen(BiConsumer<? super Short, ? super Short> after) {
        return BiConsumer.super.andThen(after);
    }
}

