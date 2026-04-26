/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.doubles;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.doubles.Double2IntFunction;
import it.unimi.dsi.fastutil.doubles.Double2LongFunction;
import it.unimi.dsi.fastutil.doubles.Double2ObjectFunction;
import it.unimi.dsi.fastutil.doubles.DoubleComparators;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

@FunctionalInterface
public interface DoubleComparator
extends Comparator<Double> {
    @Override
    public int compare(double var1, double var3);

    default public DoubleComparator reversed() {
        return DoubleComparators.oppositeComparator(this);
    }

    @Override
    @Deprecated
    default public int compare(Double ok1, Double ok2) {
        return this.compare((double)ok1, (double)ok2);
    }

    default public DoubleComparator thenComparing(DoubleComparator second) {
        return (DoubleComparator & Serializable)(k1, k2) -> {
            int comp = this.compare(k1, k2);
            return comp == 0 ? second.compare(k1, k2) : comp;
        };
    }

    @Override
    default public Comparator<Double> thenComparing(Comparator<? super Double> second) {
        if (second instanceof DoubleComparator) {
            return this.thenComparing((DoubleComparator)second);
        }
        return Comparator.super.thenComparing(second);
    }

    public static <U extends Comparable<? super U>> DoubleComparator comparing(Double2ObjectFunction<? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (DoubleComparator & Serializable)(k1, k2) -> ((Comparable)keyExtractor.get(k1)).compareTo(keyExtractor.get(k2));
    }

    public static <U extends Comparable<? super U>> DoubleComparator comparing(Double2ObjectFunction<? extends U> keyExtractor, Comparator<? super U> keyComparator) {
        Objects.requireNonNull(keyExtractor);
        Objects.requireNonNull(keyComparator);
        return (DoubleComparator & Serializable)(k1, k2) -> keyComparator.compare((Object)keyExtractor.get(k1), (Object)keyExtractor.get(k2));
    }

    public static DoubleComparator comparingInt(Double2IntFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (DoubleComparator & Serializable)(k1, k2) -> Integer.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }

    public static DoubleComparator comparingLong(Double2LongFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (DoubleComparator & Serializable)(k1, k2) -> Long.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }

    public static DoubleComparator comparingDouble(Double2DoubleFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (DoubleComparator & Serializable)(k1, k2) -> Double.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }
}

