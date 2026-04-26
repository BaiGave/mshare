/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.booleans;

import it.unimi.dsi.fastutil.booleans.Boolean2DoubleFunction;
import it.unimi.dsi.fastutil.booleans.Boolean2IntFunction;
import it.unimi.dsi.fastutil.booleans.Boolean2LongFunction;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import it.unimi.dsi.fastutil.booleans.BooleanComparators;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

@FunctionalInterface
public interface BooleanComparator
extends Comparator<Boolean> {
    @Override
    public int compare(boolean var1, boolean var2);

    default public BooleanComparator reversed() {
        return BooleanComparators.oppositeComparator(this);
    }

    @Override
    @Deprecated
    default public int compare(Boolean ok1, Boolean ok2) {
        return this.compare((boolean)ok1, (boolean)ok2);
    }

    default public BooleanComparator thenComparing(BooleanComparator second) {
        return (BooleanComparator & Serializable)(k1, k2) -> {
            int comp = this.compare(k1, k2);
            return comp == 0 ? second.compare(k1, k2) : comp;
        };
    }

    @Override
    default public Comparator<Boolean> thenComparing(Comparator<? super Boolean> second) {
        if (second instanceof BooleanComparator) {
            return this.thenComparing((BooleanComparator)second);
        }
        return Comparator.super.thenComparing(second);
    }

    public static <U extends Comparable<? super U>> BooleanComparator comparing(Boolean2ObjectFunction<? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (BooleanComparator & Serializable)(k1, k2) -> ((Comparable)keyExtractor.get(k1)).compareTo(keyExtractor.get(k2));
    }

    public static <U extends Comparable<? super U>> BooleanComparator comparing(Boolean2ObjectFunction<? extends U> keyExtractor, Comparator<? super U> keyComparator) {
        Objects.requireNonNull(keyExtractor);
        Objects.requireNonNull(keyComparator);
        return (BooleanComparator & Serializable)(k1, k2) -> keyComparator.compare((Object)keyExtractor.get(k1), (Object)keyExtractor.get(k2));
    }

    public static BooleanComparator comparingInt(Boolean2IntFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (BooleanComparator & Serializable)(k1, k2) -> Integer.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }

    public static BooleanComparator comparingLong(Boolean2LongFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (BooleanComparator & Serializable)(k1, k2) -> Long.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }

    public static BooleanComparator comparingDouble(Boolean2DoubleFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (BooleanComparator & Serializable)(k1, k2) -> Double.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }
}

