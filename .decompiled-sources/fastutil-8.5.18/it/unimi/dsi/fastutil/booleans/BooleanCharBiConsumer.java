/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.booleans;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface BooleanCharBiConsumer
extends BiConsumer<Boolean, Character> {
    @Override
    public void accept(boolean var1, char var2);

    @Override
    @Deprecated
    default public void accept(Boolean key, Character value) {
        this.accept((boolean)key, value.charValue());
    }

    default public BooleanCharBiConsumer andThen(BooleanCharBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Boolean, Character> andThen(BiConsumer<? super Boolean, ? super Character> after) {
        return BiConsumer.super.andThen(after);
    }
}

