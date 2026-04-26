/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.ints;

import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.Int2LongFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntComparators;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

@FunctionalInterface
public interface IntComparator
extends Comparator<Integer> {
    @Override
    public int compare(int var1, int var2);

    default public IntComparator reversed() {
        return IntComparators.oppositeComparator(this);
    }

    @Override
    @Deprecated
    default public int compare(Integer ok1, Integer ok2) {
        return this.compare((int)ok1, (int)ok2);
    }

    default public IntComparator thenComparing(IntComparator second) {
        return (IntComparator & Serializable)(k1, k2) -> {
            int comp = this.compare(k1, k2);
            return comp == 0 ? second.compare(k1, k2) : comp;
        };
    }

    @Override
    default public Comparator<Integer> thenComparing(Comparator<? super Integer> second) {
        if (second instanceof IntComparator) {
            return this.thenComparing((IntComparator)second);
        }
        return Comparator.super.thenComparing(second);
    }

    public static <U extends Comparable<? super U>> IntComparator comparing(Int2ObjectFunction<? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (IntComparator & Serializable)(k1, k2) -> ((Comparable)keyExtractor.get(k1)).compareTo(keyExtractor.get(k2));
    }

    public static <U extends Comparable<? super U>> IntComparator comparing(Int2ObjectFunction<? extends U> keyExtractor, Comparator<? super U> keyComparator) {
        Objects.requireNonNull(keyExtractor);
        Objects.requireNonNull(keyComparator);
        return (IntComparator & Serializable)(k1, k2) -> keyComparator.compare((Object)keyExtractor.get(k1), (Object)keyExtractor.get(k2));
    }

    public static IntComparator comparingInt(Int2IntFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (IntComparator & Serializable)(k1, k2) -> Integer.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }

    public static IntComparator comparingLong(Int2LongFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (IntComparator & Serializable)(k1, k2) -> Long.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }

    public static IntComparator comparingDouble(Int2DoubleFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (IntComparator & Serializable)(k1, k2) -> Double.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }
}

