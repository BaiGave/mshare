/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.objects;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ObjectCharBiConsumer<K>
extends BiConsumer<K, Character> {
    @Override
    public void accept(K var1, char var2);

    @Override
    @Deprecated
    default public void accept(K key, Character value) {
        this.accept(key, value.charValue());
    }

    default public ObjectCharBiConsumer<K> andThen(ObjectCharBiConsumer<K> after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept((K)k, v);
            after.accept((K)k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<K, Character> andThen(BiConsumer<? super K, ? super Character> after) {
        return BiConsumer.super.andThen(after);
    }
}

