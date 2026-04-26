/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.bytes;

import it.unimi.dsi.fastutil.bytes.Byte2DoubleFunction;
import it.unimi.dsi.fastutil.bytes.Byte2IntFunction;
import it.unimi.dsi.fastutil.bytes.Byte2LongFunction;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectFunction;
import it.unimi.dsi.fastutil.bytes.ByteComparators;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

@FunctionalInterface
public interface ByteComparator
extends Comparator<Byte> {
    @Override
    public int compare(byte var1, byte var2);

    default public ByteComparator reversed() {
        return ByteComparators.oppositeComparator(this);
    }

    @Override
    @Deprecated
    default public int compare(Byte ok1, Byte ok2) {
        return this.compare((byte)ok1, (byte)ok2);
    }

    default public ByteComparator thenComparing(ByteComparator second) {
        return (ByteComparator & Serializable)(k1, k2) -> {
            int comp = this.compare(k1, k2);
            return comp == 0 ? second.compare(k1, k2) : comp;
        };
    }

    @Override
    default public Comparator<Byte> thenComparing(Comparator<? super Byte> second) {
        if (second instanceof ByteComparator) {
            return this.thenComparing((ByteComparator)second);
        }
        return Comparator.super.thenComparing(second);
    }

    public static <U extends Comparable<? super U>> ByteComparator comparing(Byte2ObjectFunction<? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (ByteComparator & Serializable)(k1, k2) -> ((Comparable)keyExtractor.get(k1)).compareTo(keyExtractor.get(k2));
    }

    public static <U extends Comparable<? super U>> ByteComparator comparing(Byte2ObjectFunction<? extends U> keyExtractor, Comparator<? super U> keyComparator) {
        Objects.requireNonNull(keyExtractor);
        Objects.requireNonNull(keyComparator);
        return (ByteComparator & Serializable)(k1, k2) -> keyComparator.compare((Object)keyExtractor.get(k1), (Object)keyExtractor.get(k2));
    }

    public static ByteComparator comparingInt(Byte2IntFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (ByteComparator & Serializable)(k1, k2) -> Integer.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }

    public static ByteComparator comparingLong(Byte2LongFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (ByteComparator & Serializable)(k1, k2) -> Long.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }

    public static ByteComparator comparingDouble(Byte2DoubleFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (ByteComparator & Serializable)(k1, k2) -> Double.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }
}

