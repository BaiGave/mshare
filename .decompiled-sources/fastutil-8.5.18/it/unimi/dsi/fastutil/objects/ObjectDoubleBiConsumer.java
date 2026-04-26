/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.objects;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ObjectDoubleBiConsumer<K>
extends BiConsumer<K, Double> {
    @Override
    public void accept(K var1, double var2);

    @Override
    @Deprecated
    default public void accept(K key, Double value) {
        this.accept(key, (double)value);
    }

    default public ObjectDoubleBiConsumer<K> andThen(ObjectDoubleBiConsumer<K> after) {
        Objects.requireNonNull(after);
        return (k, v) -> {
            this.accept((K)k, v);
            after.accept((K)k, v);
        };
    }

    @Override
    @Deprecated
    default public BiConsumer<K, Double> andThen(BiConsumer<? super K, ? super Double> after) {
        return BiConsumer.super.andThen(after);
    }
}

