/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.objects;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ObjectShortBiConsumer<K>
extends BiConsumer<K, Short> {
    @Override
    public void accept(K var1, short var2);

    @Override
    @Deprecated
    default public void accept(K key, Short value) {
        this.accept(key, (short)value);
    }

    default public ObjectShortBiConsumer<K> andThen(ObjectShortBiConsumer<K> after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept((K)k, v);
            after.accept((K)k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<K, Short> andThen(BiConsumer<? super K, ? super Short> after) {
        return BiConsumer.super.andThen(after);
    }
}

