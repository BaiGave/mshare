/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.longs;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface LongCharBiConsumer
extends BiConsumer<Long, Character> {
    @Override
    public void accept(long var1, char var3);

    @Override
    @Deprecated
    default public void accept(Long key, Character value) {
        this.accept((long)key, value.charValue());
    }

    default public LongCharBiConsumer andThen(LongCharBiConsumer after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept(k, v);
            after.accept(k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<Long, Character> andThen(BiConsumer<? super Long, ? super Character> after) {
        return BiConsumer.super.andThen(after);
    }
}

