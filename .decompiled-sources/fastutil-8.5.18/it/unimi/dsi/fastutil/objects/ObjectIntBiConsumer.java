/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.objects;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ObjectIntBiConsumer<K>
extends BiConsumer<K, Integer> {
    @Override
    public void accept(K var1, int var2);

    @Override
    @Deprecated
    default public void accept(K key, Integer value) {
        this.accept(key, (int)value);
    }

    default public ObjectIntBiConsumer<K> andThen(ObjectIntBiConsumer<K> after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept((K)k, v);
            after.accept((K)k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<K, Integer> andThen(BiConsumer<? super K, ? super Integer> after) {
        return BiConsumer.super.andThen(after);
    }
}

