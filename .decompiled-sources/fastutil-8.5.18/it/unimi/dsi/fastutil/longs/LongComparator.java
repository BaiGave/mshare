/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.longs;

import it.unimi.dsi.fastutil.longs.Long2DoubleFunction;
import it.unimi.dsi.fastutil.longs.Long2IntFunction;
import it.unimi.dsi.fastutil.longs.Long2LongFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.LongComparators;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

@FunctionalInterface
public interface LongComparator
extends Comparator<Long> {
    @Override
    public int compare(long var1, long var3);

    default public LongComparator reversed() {
        return LongComparators.oppositeComparator(this);
    }

    @Override
    @Deprecated
    default public int compare(Long ok1, Long ok2) {
        return this.compare((long)ok1, (long)ok2);
    }

    default public LongComparator thenComparing(LongComparator second) {
        return (LongComparator & Serializable)(k1, k2) -> {
            int comp = this.compare(k1, k2);
            return comp == 0 ? second.compare(k1, k2) : comp;
        };
    }

    @Override
    default public Comparator<Long> thenComparing(Comparator<? super Long> second) {
        if (second instanceof LongComparator) {
            return this.thenComparing((LongComparator)second);
        }
        return Comparator.super.thenComparing(second);
    }

    public static <U extends Comparable<? super U>> LongComparator comparing(Long2ObjectFunction<? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (LongComparator & Serializable)(k1, k2) -> ((Comparable)keyExtractor.get(k1)).compareTo(keyExtractor.get(k2));
    }

    public static <U extends Comparable<? super U>> LongComparator comparing(Long2ObjectFunction<? extends U> keyExtractor, Comparator<? super U> keyComparator) {
        Objects.requireNonNull(keyExtractor);
        Objects.requireNonNull(keyComparator);
        return (LongComparator & Serializable)(k1, k2) -> keyComparator.compare((Object)keyExtractor.get(k1), (Object)keyExtractor.get(k2));
    }

    public static LongComparator comparingInt(Long2IntFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (LongComparator & Serializable)(k1, k2) -> Integer.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }

    public static LongComparator comparingLong(Long2LongFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (LongComparator & Serializable)(k1, k2) -> Long.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }

    public static LongComparator comparingDouble(Long2DoubleFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (LongComparator & Serializable)(k1, k2) -> Double.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }
}

