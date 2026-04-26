/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.objects;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ObjectFloatBiConsumer<K>
extends BiConsumer<K, Float> {
    @Override
    public void accept(K var1, float var2);

    @Override
    @Deprecated
    default public void accept(K key, Float value) {
        this.accept(key, value.floatValue());
    }

    default public ObjectFloatBiConsumer<K> andThen(ObjectFloatBiConsumer<K> after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept((K)k, v);
            after.accept((K)k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<K, Float> andThen(BiConsumer<? super K, ? super Float> after) {
        return BiConsumer.super.andThen(after);
    }
}

