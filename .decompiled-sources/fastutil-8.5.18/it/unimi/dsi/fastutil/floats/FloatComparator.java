/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.floats;

import it.unimi.dsi.fastutil.floats.Float2DoubleFunction;
import it.unimi.dsi.fastutil.floats.Float2IntFunction;
import it.unimi.dsi.fastutil.floats.Float2LongFunction;
import it.unimi.dsi.fastutil.floats.Float2ObjectFunction;
import it.unimi.dsi.fastutil.floats.FloatComparators;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

@FunctionalInterface
public interface FloatComparator
extends Comparator<Float> {
    @Override
    public int compare(float var1, float var2);

    default public FloatComparator reversed() {
        return FloatComparators.oppositeComparator(this);
    }

    @Override
    @Deprecated
    default public int compare(Float ok1, Float ok2) {
        return this.compare(ok1.floatValue(), ok2.floatValue());
    }

    default public FloatComparator thenComparing(FloatComparator second) {
        return (FloatComparator & Serializable)(k1, k2) -> {
            int comp = this.compare(k1, k2);
            return comp == 0 ? second.compare(k1, k2) : comp;
        };
    }

    @Override
    default public Comparator<Float> thenComparing(Comparator<? super Float> second) {
        if (second instanceof FloatComparator) {
            return this.thenComparing((FloatComparator)second);
        }
        return Comparator.super.thenComparing(second);
    }

    public static <U extends Comparable<? super U>> FloatComparator comparing(Float2ObjectFunction<? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (FloatComparator & Serializable)(k1, k2) -> ((Comparable)keyExtractor.get(k1)).compareTo(keyExtractor.get(k2));
    }

    public static <U extends Comparable<? super U>> FloatComparator comparing(Float2ObjectFunction<? extends U> keyExtractor, Comparator<? super U> keyComparator) {
        Objects.requireNonNull(keyExtractor);
        Objects.requireNonNull(keyComparator);
        return (FloatComparator & Serializable)(k1, k2) -> keyComparator.compare((Object)keyExtractor.get(k1), (Object)keyExtractor.get(k2));
    }

    public static FloatComparator comparingInt(Float2IntFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (FloatComparator & Serializable)(k1, k2) -> Integer.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }

    public static FloatComparator comparingLong(Float2LongFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (FloatComparator & Serializable)(k1, k2) -> Long.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }

    public static FloatComparator comparingDouble(Float2DoubleFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (FloatComparator & Serializable)(k1, k2) -> Double.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }
}

