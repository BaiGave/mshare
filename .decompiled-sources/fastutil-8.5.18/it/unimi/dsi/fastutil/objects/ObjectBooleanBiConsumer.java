/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.objects;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ObjectBooleanBiConsumer<K>
extends BiConsumer<K, Boolean> {
    @Override
    public void accept(K var1, boolean var2);

    @Override
    @Deprecated
    default public void accept(K key, Boolean value) {
        this.accept(key, (boolean)value);
    }

    default public ObjectBooleanBiConsumer<K> andThen(ObjectBooleanBiConsumer<K> after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept((K)k, v);
            after.accept((K)k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<K, Boolean> andThen(BiConsumer<? super K, ? super Boolean> after) {
        return BiConsumer.super.andThen(after);
    }
}

