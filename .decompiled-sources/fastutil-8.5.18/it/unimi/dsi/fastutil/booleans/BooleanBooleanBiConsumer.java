/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.booleans;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface BooleanBooleanBiConsumer
extends BiConsumer<Boolean, Boolean> {
    @Override
    public void accept(boolean var1, boolean var2);

    @Override
    @Deprecated
    default public void accept(Boolean key, Boolean value) {
        this.accept((boolean)key, (boolean)value);
    }

    default public BooleanBooleanBiConsumer andThen(BooleanBooleanBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Boolean, Boolean> andThen(BiConsumer<? super Boolean, ? super Boolean> after) {
        return BiConsumer.super.andThen(after);
    }
}

