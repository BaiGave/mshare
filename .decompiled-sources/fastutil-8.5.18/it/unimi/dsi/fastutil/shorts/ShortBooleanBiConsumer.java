/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.shorts;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ShortBooleanBiConsumer
extends BiConsumer<Short, Boolean> {
    @Override
    public void accept(short var1, boolean var2);

    @Override
    @Deprecated
    default public void accept(Short key, Boolean value) {
        this.accept((short)key, (boolean)value);
    }

    default public ShortBooleanBiConsumer andThen(ShortBooleanBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Short, Boolean> andThen(BiConsumer<? super Short, ? super Boolean> after) {
        return BiConsumer.super.andThen(after);
    }
}

