/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.collect.CompactHashSet;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@GwtCompatible
final class Platform {
    static <K, V> Map<K, V> newHashMapWithExpectedSize(int expectedSize) {
        return Maps.newHashMapWithExpectedSize(expectedSize);
    }

    static <K, V> Map<K, V> newLinkedHashMapWithExpectedSize(int expectedSize) {
        return Maps.newLinkedHashMapWithExpectedSize(expectedSize);
    }

    static <E> Set<E> newHashSetWithExpectedSize(int expectedSize) {
        return Sets.newHashSetWithExpectedSize(expectedSize);
    }

    static <E> Set<E> newConcurrentHashSet() {
        return ConcurrentHashMap.newKeySet();
    }

    static <E> Set<E> newLinkedHashSetWithExpectedSize(int expectedSize) {
        return Sets.newLinkedHashSetWithExpectedSize(expectedSize);
    }

    static <K, V> Map<K, V> preservesInsertionOrderOnPutsMap() {
        return new LinkedHashMap();
    }

    static <K, V> Map<K, V> preservesInsertionOrderOnPutsMapWithExpectedSize(int expectedSize) {
        return Maps.newLinkedHashMapWithExpectedSize(expectedSize);
    }

    static <E> Set<E> preservesInsertionOrderOnAddsSet() {
        return CompactHashSet.create();
    }

    static <T> T[] newArray(T[] reference, int length) {
        T[] empty = reference.length == 0 ? reference : Arrays.copyOf(reference, 0);
        return Arrays.copyOf(empty, length);
    }

    static <T> T[] copy(Object[] source, int from, int to, T[] arrayOfType) {
        return Arrays.copyOfRange(source, from, to, arrayOfType.getClass());
    }

    @J2ktIncompatible
    static MapMaker tryWeakKeys(MapMaker mapMaker) {
        return mapMaker.weakKeys();
    }

    static <E extends Enum<E>> Class<E> getDeclaringClassOrObjectForJ2cl(E e) {
        return e.getDeclaringClass();
    }

    static int reduceIterationsIfGwt(int iterations) {
        return iterations;
    }

    static int reduceExponentIfGwt(int exponent) {
        return exponent;
    }

    private Platform() {
    }
}

