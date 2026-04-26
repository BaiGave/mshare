/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.booleans;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface BooleanShortBiConsumer
extends BiConsumer<Boolean, Short> {
    @Override
    public void accept(boolean var1, short var2);

    @Override
    @Deprecated
    default public void accept(Boolean key, Short value) {
        this.accept((boolean)key, (short)value);
    }

    default public BooleanShortBiConsumer andThen(BooleanShortBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Boolean, Short> andThen(BiConsumer<? super Boolean, ? super Short> after) {
        return BiConsumer.super.andThen(after);
    }
}

