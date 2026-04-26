/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.shorts;

import it.unimi.dsi.fastutil.shorts.Short2DoubleFunction;
import it.unimi.dsi.fastutil.shorts.Short2IntFunction;
import it.unimi.dsi.fastutil.shorts.Short2LongFunction;
import it.unimi.dsi.fastutil.shorts.Short2ObjectFunction;
import it.unimi.dsi.fastutil.shorts.ShortComparators;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

@FunctionalInterface
public interface ShortComparator
extends Comparator<Short> {
    @Override
    public int compare(short var1, short var2);

    default public ShortComparator reversed() {
        return ShortComparators.oppositeComparator(this);
    }

    @Override
    @Deprecated
    default public int compare(Short ok1, Short ok2) {
        return this.compare((short)ok1, (short)ok2);
    }

    default public ShortComparator thenComparing(ShortComparator second) {
        return (ShortComparator & Serializable)(k1, k2) -> {
            int comp = this.compare(k1, k2);
            return comp == 0 ? second.compare(k1, k2) : comp;
        };
    }

    @Override
    default public Comparator<Short> thenComparing(Comparator<? super Short> second) {
        if (second instanceof ShortComparator) {
            return this.thenComparing((ShortComparator)second);
        }
        return Comparator.super.thenComparing(second);
    }

    public static <U extends Comparable<? super U>> ShortComparator comparing(Short2ObjectFunction<? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (ShortComparator & Serializable)(k1, k2) -> ((Comparable)keyExtractor.get(k1)).compareTo(keyExtractor.get(k2));
    }

    public static <U extends Comparable<? super U>> ShortComparator comparing(Short2ObjectFunction<? extends U> keyExtractor, Comparator<? super U> keyComparator) {
        Objects.requireNonNull(keyExtractor);
        Objects.requireNonNull(keyComparator);
        return (ShortComparator & Serializable)(k1, k2) -> keyComparator.compare((Object)keyExtractor.get(k1), (Object)keyExtractor.get(k2));
    }

    public static ShortComparator comparingInt(Short2IntFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (ShortComparator & Serializable)(k1, k2) -> Integer.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }

    public static ShortComparator comparingLong(Short2LongFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (ShortComparator & Serializable)(k1, k2) -> Long.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }

    public static ShortComparator comparingDouble(Short2DoubleFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (ShortComparator & Serializable)(k1, k2) -> Double.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }
}

