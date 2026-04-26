/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.chars;

import it.unimi.dsi.fastutil.chars.Char2DoubleFunction;
import it.unimi.dsi.fastutil.chars.Char2IntFunction;
import it.unimi.dsi.fastutil.chars.Char2LongFunction;
import it.unimi.dsi.fastutil.chars.Char2ObjectFunction;
import it.unimi.dsi.fastutil.chars.CharComparators;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

@FunctionalInterface
public interface CharComparator
extends Comparator<Character> {
    @Override
    public int compare(char var1, char var2);

    default public CharComparator reversed() {
        return CharComparators.oppositeComparator(this);
    }

    @Override
    @Deprecated
    default public int compare(Character ok1, Character ok2) {
        return this.compare(ok1.charValue(), ok2.charValue());
    }

    default public CharComparator thenComparing(CharComparator second) {
        return (CharComparator & Serializable)(k1, k2) -> {
            int comp = this.compare(k1, k2);
            return comp == 0 ? second.compare(k1, k2) : comp;
        };
    }

    @Override
    default public Comparator<Character> thenComparing(Comparator<? super Character> second) {
        if (second instanceof CharComparator) {
            return this.thenComparing((CharComparator)second);
        }
        return Comparator.super.thenComparing(second);
    }

    public static <U extends Comparable<? super U>> CharComparator comparing(Char2ObjectFunction<? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (CharComparator & Serializable)(k1, k2) -> ((Comparable)keyExtractor.get(k1)).compareTo(keyExtractor.get(k2));
    }

    public static <U extends Comparable<? super U>> CharComparator comparing(Char2ObjectFunction<? extends U> keyExtractor, Comparator<? super U> keyComparator) {
        Objects.requireNonNull(keyExtractor);
        Objects.requireNonNull(keyComparator);
        return (CharComparator & Serializable)(k1, k2) -> keyComparator.compare((Object)keyExtractor.get(k1), (Object)keyExtractor.get(k2));
    }

    public static CharComparator comparingInt(Char2IntFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (CharComparator & Serializable)(k1, k2) -> Integer.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }

    public static CharComparator comparingLong(Char2LongFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (CharComparator & Serializable)(k1, k2) -> Long.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }

    public static CharComparator comparingDouble(Char2DoubleFunction keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (CharComparator & Serializable)(k1, k2) -> Double.compare(keyExtractor.get(k1), keyExtractor.get(k2));
    }
}

