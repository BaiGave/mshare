/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.objects;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ObjectByteBiConsumer<K>
extends BiConsumer<K, Byte> {
    @Override
    public void accept(K var1, byte var2);

    @Override
    @Deprecated
    default public void accept(K key, Byte value) {
        this.accept(key, (byte)value);
    }

    default public ObjectByteBiConsumer<K> andThen(ObjectByteBiConsumer<K> after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept((K)k, v);
            after.accept((K)k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<K, Byte> andThen(BiConsumer<? super K, ? super Byte> after) {
        return BiConsumer.super.andThen(after);
    }
}

