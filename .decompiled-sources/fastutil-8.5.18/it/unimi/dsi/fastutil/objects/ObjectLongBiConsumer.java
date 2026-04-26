/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.objects;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ObjectLongBiConsumer<K>
extends BiConsumer<K, Long> {
    @Override
    public void accept(K var1, long var2);

    @Override
    @Deprecated
    default public void accept(K key, Long value) {
        this.accept(key, (long)value);
    }

    default public ObjectLongBiConsumer<K> andThen(ObjectLongBiConsumer<K> after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept((K)k, v);
            after.accept((K)k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<K, Long> andThen(BiConsumer<? super K, ? super Long> after) {
        return BiConsumer.super.andThen(after);
    }
}

