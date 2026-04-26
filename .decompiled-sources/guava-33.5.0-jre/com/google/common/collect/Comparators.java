/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.CollectPreconditions;
import com.google.common.collect.LexicographicalOrdering;
import com.google.common.collect.ParametricNullness;
import com.google.common.collect.TopKSelector;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class Comparators {
    private Comparators() {
    }

    public static <T, S extends T> Comparator<Iterable<S>> lexicographical(Comparator<T> comparator) {
        return new LexicographicalOrdering<T>(Preconditions.checkNotNull(comparator));
    }

    public static <T> boolean isInOrder(Iterable<? extends T> iterable, Comparator<T> comparator) {
        Preconditions.checkNotNull(comparator);
        Iterator<T> it = iterable.iterator();
        if (it.hasNext()) {
            T prev = it.next();
            while (it.hasNext()) {
                T next = it.next();
                if (comparator.compare(prev, next) > 0) {
                    return false;
                }
                prev = next;
            }
        }
        return true;
    }

    public static <T> boolean isInStrictOrder(Iterable<? extends T> iterable, Comparator<T> comparator) {
        Preconditions.checkNotNull(comparator);
        Iterator<T> it = iterable.iterator();
        if (it.hasNext()) {
            T prev = it.next();
            while (it.hasNext()) {
                T next = it.next();
                if (comparator.compare(prev, next) >= 0) {
                    return false;
                }
                prev = next;
            }
        }
        return true;
    }

    public static <T> Collector<T, ?, List<T>> least(int k, Comparator<? super T> comparator) {
        CollectPreconditions.checkNonnegative(k, "k");
        Preconditions.checkNotNull(comparator);
        return Collector.of(() -> TopKSelector.least(k, comparator), TopKSelector::offer, TopKSelector::combine, TopKSelector::topK, Collector.Characteristics.UNORDERED);
    }

    public static <T> Collector<T, ?, List<T>> greatest(int k, Comparator<? super T> comparator) {
        return Comparators.least(k, comparator.reversed());
    }

    public static <T> Comparator<Optional<T>> emptiesFirst(Comparator<? super T> valueComparator) {
        Preconditions.checkNotNull(valueComparator);
        return Comparator.comparing(o -> Comparators.orElseNull(o), Comparator.nullsFirst(valueComparator));
    }

    public static <T> Comparator<Optional<T>> emptiesLast(Comparator<? super T> valueComparator) {
        Preconditions.checkNotNull(valueComparator);
        return Comparator.comparing(o -> Comparators.orElseNull(o), Comparator.nullsLast(valueComparator));
    }

    private static <T> @Nullable T orElseNull(Optional<T> optional) {
        return optional.orElse(null);
    }

    public static <T extends Comparable<? super T>> T min(T a, T b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    @ParametricNullness
    public static <T> T min(@ParametricNullness T a, @ParametricNullness T b, Comparator<? super T> comparator) {
        return comparator.compare(a, b) <= 0 ? a : b;
    }

    public static <T extends Comparable<? super T>> T max(T a, T b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    @ParametricNullness
    public static <T> T max(@ParametricNullness T a, @ParametricNullness T b, Comparator<? super T> comparator) {
        return comparator.compare(a, b) >= 0 ? a : b;
    }
}

