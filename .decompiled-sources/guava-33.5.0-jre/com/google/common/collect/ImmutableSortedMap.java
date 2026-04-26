/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.CollectCollectors;
import com.google.common.collect.CollectPreconditions;
import com.google.common.collect.CollectSpliterators;
import com.google.common.collect.ImmutableAsList;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMapEntrySet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.RegularImmutableList;
import com.google.common.collect.RegularImmutableSortedSet;
import com.google.common.collect.UnmodifiableIterator;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class ImmutableSortedMap<K, V>
extends ImmutableMap<K, V>
implements NavigableMap<K, V> {
    private static final Comparator<?> NATURAL_ORDER = Ordering.natural();
    private static final ImmutableSortedMap<Comparable<?>, Object> NATURAL_EMPTY_MAP = new ImmutableSortedMap(ImmutableSortedSet.emptySet(Ordering.natural()), ImmutableList.of());
    private final transient RegularImmutableSortedSet<K> keySet;
    private final transient ImmutableList<V> valueList;
    private final transient @Nullable ImmutableSortedMap<K, V> descendingMap;
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = 0L;

    public static <T, K, V> Collector<T, ?, ImmutableSortedMap<K, V>> toImmutableSortedMap(Comparator<? super K> comparator, Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction) {
        return CollectCollectors.toImmutableSortedMap(comparator, keyFunction, valueFunction);
    }

    public static <T, K, V> Collector<T, ?, ImmutableSortedMap<K, V>> toImmutableSortedMap(Comparator<? super K> comparator, Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction, BinaryOperator<V> mergeFunction) {
        return CollectCollectors.toImmutableSortedMap(comparator, keyFunction, valueFunction, mergeFunction);
    }

    static <K, V> ImmutableSortedMap<K, V> emptyMap(Comparator<? super K> comparator) {
        if (Ordering.natural().equals(comparator)) {
            return ImmutableSortedMap.of();
        }
        return new ImmutableSortedMap(ImmutableSortedSet.emptySet(comparator), ImmutableList.of());
    }

    public static <K, V> ImmutableSortedMap<K, V> of() {
        return NATURAL_EMPTY_MAP;
    }

    public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> of(K k1, V v1) {
        return ImmutableSortedMap.of(Ordering.natural(), k1, v1);
    }

    private static <K, V> ImmutableSortedMap<K, V> of(Comparator<? super K> comparator, K k1, V v1) {
        return new ImmutableSortedMap<K, V>(new RegularImmutableSortedSet<K>(ImmutableList.of(k1), Preconditions.checkNotNull(comparator)), ImmutableList.of(v1));
    }

    public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2) {
        return ImmutableSortedMap.fromEntries(ImmutableSortedMap.entryOf(k1, v1), ImmutableSortedMap.entryOf(k2, v2));
    }

    public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        return ImmutableSortedMap.fromEntries(ImmutableSortedMap.entryOf(k1, v1), ImmutableSortedMap.entryOf(k2, v2), ImmutableSortedMap.entryOf(k3, v3));
    }

    public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return ImmutableSortedMap.fromEntries(ImmutableSortedMap.entryOf(k1, v1), ImmutableSortedMap.entryOf(k2, v2), ImmutableSortedMap.entryOf(k3, v3), ImmutableSortedMap.entryOf(k4, v4));
    }

    public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return ImmutableSortedMap.fromEntries(ImmutableSortedMap.entryOf(k1, v1), ImmutableSortedMap.entryOf(k2, v2), ImmutableSortedMap.entryOf(k3, v3), ImmutableSortedMap.entryOf(k4, v4), ImmutableSortedMap.entryOf(k5, v5));
    }

    public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        return ImmutableSortedMap.fromEntries(ImmutableSortedMap.entryOf(k1, v1), ImmutableSortedMap.entryOf(k2, v2), ImmutableSortedMap.entryOf(k3, v3), ImmutableSortedMap.entryOf(k4, v4), ImmutableSortedMap.entryOf(k5, v5), ImmutableSortedMap.entryOf(k6, v6));
    }

    public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        return ImmutableSortedMap.fromEntries(ImmutableSortedMap.entryOf(k1, v1), ImmutableSortedMap.entryOf(k2, v2), ImmutableSortedMap.entryOf(k3, v3), ImmutableSortedMap.entryOf(k4, v4), ImmutableSortedMap.entryOf(k5, v5), ImmutableSortedMap.entryOf(k6, v6), ImmutableSortedMap.entryOf(k7, v7));
    }

    public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        return ImmutableSortedMap.fromEntries(ImmutableSortedMap.entryOf(k1, v1), ImmutableSortedMap.entryOf(k2, v2), ImmutableSortedMap.entryOf(k3, v3), ImmutableSortedMap.entryOf(k4, v4), ImmutableSortedMap.entryOf(k5, v5), ImmutableSortedMap.entryOf(k6, v6), ImmutableSortedMap.entryOf(k7, v7), ImmutableSortedMap.entryOf(k8, v8));
    }

    public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        return ImmutableSortedMap.fromEntries(ImmutableSortedMap.entryOf(k1, v1), ImmutableSortedMap.entryOf(k2, v2), ImmutableSortedMap.entryOf(k3, v3), ImmutableSortedMap.entryOf(k4, v4), ImmutableSortedMap.entryOf(k5, v5), ImmutableSortedMap.entryOf(k6, v6), ImmutableSortedMap.entryOf(k7, v7), ImmutableSortedMap.entryOf(k8, v8), ImmutableSortedMap.entryOf(k9, v9));
    }

    public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        return ImmutableSortedMap.fromEntries(ImmutableSortedMap.entryOf(k1, v1), ImmutableSortedMap.entryOf(k2, v2), ImmutableSortedMap.entryOf(k3, v3), ImmutableSortedMap.entryOf(k4, v4), ImmutableSortedMap.entryOf(k5, v5), ImmutableSortedMap.entryOf(k6, v6), ImmutableSortedMap.entryOf(k7, v7), ImmutableSortedMap.entryOf(k8, v8), ImmutableSortedMap.entryOf(k9, v9), ImmutableSortedMap.entryOf(k10, v10));
    }

    public static <K, V> ImmutableSortedMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
        Ordering naturalOrder = (Ordering)NATURAL_ORDER;
        return ImmutableSortedMap.copyOfInternal(map, naturalOrder);
    }

    public static <K, V> ImmutableSortedMap<K, V> copyOf(Map<? extends K, ? extends V> map, Comparator<? super K> comparator) {
        return ImmutableSortedMap.copyOfInternal(map, Preconditions.checkNotNull(comparator));
    }

    public static <K, V> ImmutableSortedMap<K, V> copyOf(Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        Ordering naturalOrder = (Ordering)NATURAL_ORDER;
        return ImmutableSortedMap.copyOf(entries, naturalOrder);
    }

    public static <K, V> ImmutableSortedMap<K, V> copyOf(Iterable<? extends Map.Entry<? extends K, ? extends V>> entries, Comparator<? super K> comparator) {
        return ImmutableSortedMap.fromEntries(Preconditions.checkNotNull(comparator), false, entries);
    }

    public static <K, V> ImmutableSortedMap<K, V> copyOfSorted(SortedMap<K, ? extends V> map) {
        ImmutableSortedMap kvMap;
        Comparator<Object> comparator = map.comparator();
        if (comparator == null) {
            comparator = NATURAL_ORDER;
        }
        if (map instanceof ImmutableSortedMap && !(kvMap = (ImmutableSortedMap)map).isPartialView()) {
            return kvMap;
        }
        return ImmutableSortedMap.fromEntries(comparator, true, map.entrySet());
    }

    private static <K, V> ImmutableSortedMap<K, V> copyOfInternal(Map<? extends K, ? extends V> map, Comparator<? super K> comparator) {
        ImmutableSortedMap kvMap;
        boolean sameComparator = false;
        if (map instanceof SortedMap) {
            SortedMap sortedMap = (SortedMap)map;
            Comparator comparator2 = sortedMap.comparator();
            boolean bl = comparator2 == null ? comparator == NATURAL_ORDER : (sameComparator = comparator.equals(comparator2));
        }
        if (sameComparator && map instanceof ImmutableSortedMap && !(kvMap = (ImmutableSortedMap)map).isPartialView()) {
            return kvMap;
        }
        return ImmutableSortedMap.fromEntries(comparator, sameComparator, map.entrySet());
    }

    private static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> fromEntries(Map.Entry<K, V> ... entries) {
        return ImmutableSortedMap.fromEntries(Ordering.natural(), false, entries, entries.length);
    }

    private static <K, V> ImmutableSortedMap<K, V> fromEntries(Comparator<? super K> comparator, boolean sameComparator, Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        Map.Entry[] entryArray = Iterables.toArray(entries, EMPTY_ENTRY_ARRAY);
        return ImmutableSortedMap.fromEntries(comparator, sameComparator, entryArray, entryArray.length);
    }

    private static <K, V> ImmutableSortedMap<K, V> fromEntries(Comparator<? super K> comparator, boolean sameComparator, @Nullable Map.Entry<K, V>[] entryArray, int size) {
        switch (size) {
            case 0: {
                return ImmutableSortedMap.emptyMap(comparator);
            }
            case 1: {
                Map.Entry<K, V> onlyEntry = Objects.requireNonNull(entryArray[0]);
                return ImmutableSortedMap.of(comparator, onlyEntry.getKey(), onlyEntry.getValue());
            }
        }
        Object[] keys = new Object[size];
        Object[] values = new Object[size];
        if (sameComparator) {
            for (int i = 0; i < size; ++i) {
                Map.Entry<K, V> entry = Objects.requireNonNull(entryArray[i]);
                K key = entry.getKey();
                V value = entry.getValue();
                CollectPreconditions.checkEntryNotNull(key, value);
                keys[i] = key;
                values[i] = value;
            }
        } else {
            Arrays.sort(entryArray, 0, size, (e1, e2) -> {
                Objects.requireNonNull(e1);
                Objects.requireNonNull(e2);
                return comparator.compare(e1.getKey(), e2.getKey());
            });
            Map.Entry<K, V> firstEntry = Objects.requireNonNull(entryArray[0]);
            K prevKey = firstEntry.getKey();
            keys[0] = prevKey;
            values[0] = firstEntry.getValue();
            CollectPreconditions.checkEntryNotNull(keys[0], values[0]);
            for (int i = 1; i < size; ++i) {
                Map.Entry<K, V> prevEntry = Objects.requireNonNull(entryArray[i - 1]);
                Map.Entry<K, V> entry = Objects.requireNonNull(entryArray[i]);
                K key = entry.getKey();
                V value = entry.getValue();
                CollectPreconditions.checkEntryNotNull(key, value);
                keys[i] = key;
                values[i] = value;
                ImmutableSortedMap.checkNoConflict(comparator.compare(prevKey, key) != 0, "key", prevEntry, entry);
                prevKey = key;
            }
        }
        return new ImmutableSortedMap(new RegularImmutableSortedSet<K>(new RegularImmutableList(keys), comparator), new RegularImmutableList(values));
    }

    public static <K extends Comparable<?>, V> Builder<K, V> naturalOrder() {
        return new Builder(Ordering.natural());
    }

    public static <K, V> Builder<K, V> orderedBy(Comparator<K> comparator) {
        return new Builder(comparator);
    }

    public static <K extends Comparable<?>, V> Builder<K, V> reverseOrder() {
        return new Builder(Ordering.natural().reverse());
    }

    ImmutableSortedMap(RegularImmutableSortedSet<K> keySet, ImmutableList<V> valueList) {
        this(keySet, valueList, null);
    }

    ImmutableSortedMap(RegularImmutableSortedSet<K> keySet, ImmutableList<V> valueList, @Nullable ImmutableSortedMap<K, V> descendingMap) {
        this.keySet = keySet;
        this.valueList = valueList;
        this.descendingMap = descendingMap;
    }

    @Override
    public int size() {
        return this.valueList.size();
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Preconditions.checkNotNull(action);
        ImmutableList keyList = this.keySet.asList();
        for (int i = 0; i < this.size(); ++i) {
            action.accept(keyList.get(i), this.valueList.get(i));
        }
    }

    @Override
    public @Nullable V get(@Nullable Object key) {
        int index = this.keySet.indexOf(key);
        return index == -1 ? null : (V)this.valueList.get(index);
    }

    @Override
    boolean isPartialView() {
        return this.keySet.isPartialView() || this.valueList.isPartialView();
    }

    @Override
    public ImmutableSet<Map.Entry<K, V>> entrySet() {
        return super.entrySet();
    }

    @Override
    ImmutableSet<Map.Entry<K, V>> createEntrySet() {
        final class EntrySet
        extends ImmutableMapEntrySet<K, V> {
            EntrySet() {
            }

            @Override
            public UnmodifiableIterator<Map.Entry<K, V>> iterator() {
                return this.asList().iterator();
            }

            @Override
            public Spliterator<Map.Entry<K, V>> spliterator() {
                return this.asList().spliterator();
            }

            @Override
            public void forEach(Consumer<? super Map.Entry<K, V>> action) {
                this.asList().forEach(action);
            }

            @Override
            ImmutableList<Map.Entry<K, V>> createAsList() {
                return new ImmutableAsList<Map.Entry<K, V>>(){

                    @Override
                    public Map.Entry<K, V> get(int index) {
                        return new AbstractMap.SimpleImmutableEntry(ImmutableSortedMap.this.keySet.asList().get(index), ImmutableSortedMap.this.valueList.get(index));
                    }

                    @Override
                    public Spliterator<Map.Entry<K, V>> spliterator() {
                        return CollectSpliterators.indexed(this.size(), 1297, n -> this.get(n));
                    }

                    @Override
                    ImmutableCollection<Map.Entry<K, V>> delegateCollection() {
                        return this;
                    }

                    @Override
                    @J2ktIncompatible
                    @GwtIncompatible
                    Object writeReplace() {
                        return super.writeReplace();
                    }
                };
            }

            @Override
            ImmutableMap<K, V> map() {
                return ImmutableSortedMap.this;
            }

            @Override
            @J2ktIncompatible
            @GwtIncompatible
            Object writeReplace() {
                return super.writeReplace();
            }
        }
        return this.isEmpty() ? ImmutableSet.of() : new EntrySet();
    }

    @Override
    public ImmutableSortedSet<K> keySet() {
        return this.keySet;
    }

    @Override
    ImmutableSet<K> createKeySet() {
        throw new AssertionError((Object)"should never be called");
    }

    @Override
    public ImmutableCollection<V> values() {
        return this.valueList;
    }

    @Override
    ImmutableCollection<V> createValues() {
        throw new AssertionError((Object)"should never be called");
    }

    @Override
    public Comparator<? super K> comparator() {
        return ((ImmutableSortedSet)this.keySet()).comparator();
    }

    @Override
    public K firstKey() {
        return (K)((ImmutableSortedSet)this.keySet()).first();
    }

    @Override
    public K lastKey() {
        return (K)((ImmutableSortedSet)this.keySet()).last();
    }

    private ImmutableSortedMap<K, V> getSubMap(int fromIndex, int toIndex) {
        if (fromIndex == 0 && toIndex == this.size()) {
            return this;
        }
        if (fromIndex == toIndex) {
            return ImmutableSortedMap.emptyMap(this.comparator());
        }
        return new ImmutableSortedMap<K, V>(this.keySet.getSubSet(fromIndex, toIndex), this.valueList.subList(fromIndex, toIndex));
    }

    @Override
    public ImmutableSortedMap<K, V> headMap(K toKey) {
        return this.headMap((Object)toKey, false);
    }

    @Override
    public ImmutableSortedMap<K, V> headMap(K toKey, boolean inclusive) {
        return this.getSubMap(0, this.keySet.headIndex(Preconditions.checkNotNull(toKey), inclusive));
    }

    @Override
    public ImmutableSortedMap<K, V> subMap(K fromKey, K toKey) {
        return this.subMap((Object)fromKey, true, (Object)toKey, false);
    }

    @Override
    public ImmutableSortedMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        Preconditions.checkNotNull(fromKey);
        Preconditions.checkNotNull(toKey);
        Preconditions.checkArgument(this.comparator().compare(fromKey, toKey) <= 0, "expected fromKey <= toKey but %s > %s", fromKey, toKey);
        return ((ImmutableSortedMap)this.headMap((Object)toKey, toInclusive)).tailMap((Object)fromKey, fromInclusive);
    }

    @Override
    public ImmutableSortedMap<K, V> tailMap(K fromKey) {
        return this.tailMap((Object)fromKey, true);
    }

    @Override
    public ImmutableSortedMap<K, V> tailMap(K fromKey, boolean inclusive) {
        return this.getSubMap(this.keySet.tailIndex(Preconditions.checkNotNull(fromKey), inclusive), this.size());
    }

    @Override
    public @Nullable Map.Entry<K, V> lowerEntry(K key) {
        return ((ImmutableSortedMap)this.headMap((Object)key, false)).lastEntry();
    }

    @Override
    public @Nullable K lowerKey(K key) {
        return Maps.keyOrNull(this.lowerEntry(key));
    }

    @Override
    public @Nullable Map.Entry<K, V> floorEntry(K key) {
        return ((ImmutableSortedMap)this.headMap((Object)key, true)).lastEntry();
    }

    @Override
    public @Nullable K floorKey(K key) {
        return Maps.keyOrNull(this.floorEntry(key));
    }

    @Override
    public @Nullable Map.Entry<K, V> ceilingEntry(K key) {
        return ((ImmutableSortedMap)this.tailMap((Object)key, true)).firstEntry();
    }

    @Override
    public @Nullable K ceilingKey(K key) {
        return Maps.keyOrNull(this.ceilingEntry(key));
    }

    @Override
    public @Nullable Map.Entry<K, V> higherEntry(K key) {
        return ((ImmutableSortedMap)this.tailMap((Object)key, false)).firstEntry();
    }

    @Override
    public @Nullable K higherKey(K key) {
        return Maps.keyOrNull(this.higherEntry(key));
    }

    @Override
    public @Nullable Map.Entry<K, V> firstEntry() {
        return this.isEmpty() ? null : (Map.Entry)((ImmutableCollection)((Object)this.entrySet())).asList().get(0);
    }

    @Override
    public @Nullable Map.Entry<K, V> lastEntry() {
        return this.isEmpty() ? null : (Map.Entry)((ImmutableCollection)((Object)this.entrySet())).asList().get(this.size() - 1);
    }

    @Override
    @Deprecated
    @CanIgnoreReturnValue
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final @Nullable Map.Entry<K, V> pollFirstEntry() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    @CanIgnoreReturnValue
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final @Nullable Map.Entry<K, V> pollLastEntry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ImmutableSortedMap<K, V> descendingMap() {
        ImmutableSortedMap<K, V> result = this.descendingMap;
        if (result == null) {
            if (this.isEmpty()) {
                return ImmutableSortedMap.emptyMap(Ordering.from(this.comparator()).reverse());
            }
            return new ImmutableSortedMap<K, V>((RegularImmutableSortedSet)this.keySet.descendingSet(), this.valueList.reverse(), this);
        }
        return result;
    }

    @Override
    public ImmutableSortedSet<K> navigableKeySet() {
        return this.keySet;
    }

    @Override
    public ImmutableSortedSet<K> descendingKeySet() {
        return this.keySet.descendingSet();
    }

    @Override
    @J2ktIncompatible
    Object writeReplace() {
        return new SerializedForm(this);
    }

    @J2ktIncompatible
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Use SerializedForm");
    }

    @Deprecated
    @DoNotCall(value="Use toImmutableSortedMap")
    public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @DoNotCall(value="Use toImmutableSortedMap")
    public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction, BinaryOperator<V> mergeFunction) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @DoNotCall(value="Use naturalOrder")
    public static <K, V> Builder<K, V> builder() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @DoNotCall(value="Use naturalOrder (which does not accept an expected size)")
    public static <K, V> Builder<K, V> builderWithExpectedSize(int expectedSize) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @DoNotCall(value="Pass a key of type Comparable")
    public static <K, V> ImmutableSortedMap<K, V> of(K k1, V v1) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @DoNotCall(value="Pass keys of type Comparable")
    public static <K, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @DoNotCall(value="Pass keys of type Comparable")
    public static <K, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @DoNotCall(value="Pass keys of type Comparable")
    public static <K, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @DoNotCall(value="Pass keys of type Comparable")
    public static <K, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @DoNotCall(value="Pass keys of type Comparable")
    public static <K, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @DoNotCall(value="Pass keys of type Comparable")
    public static <K, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @DoNotCall(value="Pass keys of type Comparable")
    public static <K, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @DoNotCall(value="Pass keys of type Comparable")
    public static <K, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @DoNotCall(value="Pass keys of type Comparable")
    public static <K, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @SafeVarargs
    @DoNotCall(value="ImmutableSortedMap.ofEntries not currently available; use ImmutableSortedMap.copyOf")
    public static <K, V> ImmutableSortedMap<K, V> ofEntries(Map.Entry<? extends K, ? extends V> ... entries) {
        throw new UnsupportedOperationException();
    }

    public static class Builder<K, V>
    extends ImmutableMap.Builder<K, V> {
        private final Comparator<? super K> comparator;

        public Builder(Comparator<? super K> comparator) {
            this.comparator = Preconditions.checkNotNull(comparator);
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<K, V> put(K key, V value) {
            super.put(key, value);
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<K, V> put(Map.Entry<? extends K, ? extends V> entry) {
            super.put(entry);
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<K, V> putAll(Map<? extends K, ? extends V> map) {
            super.putAll(map);
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<K, V> putAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
            super.putAll(entries);
            return this;
        }

        @Override
        @Deprecated
        @CanIgnoreReturnValue
        @DoNotCall(value="Always throws UnsupportedOperationException")
        public final Builder<K, V> orderEntriesByValue(Comparator<? super V> valueComparator) {
            throw new UnsupportedOperationException("Not available on ImmutableSortedMap.Builder");
        }

        @Override
        Builder<K, V> combine(ImmutableMap.Builder<K, V> other) {
            super.combine(other);
            return this;
        }

        @Override
        public ImmutableSortedMap<K, V> build() {
            return this.buildOrThrow();
        }

        @Override
        public ImmutableSortedMap<K, V> buildOrThrow() {
            switch (this.size) {
                case 0: {
                    return ImmutableSortedMap.emptyMap(this.comparator);
                }
                case 1: {
                    Map.Entry onlyEntry = Objects.requireNonNull(this.entries[0]);
                    return ImmutableSortedMap.of(this.comparator, onlyEntry.getKey(), onlyEntry.getValue());
                }
            }
            return ImmutableSortedMap.fromEntries(this.comparator, false, this.entries, this.size);
        }

        @Override
        @Deprecated
        @DoNotCall
        public final ImmutableSortedMap<K, V> buildKeepingLast() {
            throw new UnsupportedOperationException("ImmutableSortedMap.Builder does not yet implement buildKeepingLast()");
        }
    }

    @J2ktIncompatible
    private static final class SerializedForm<K, V>
    extends ImmutableMap.SerializedForm<K, V> {
        private final Comparator<? super K> comparator;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        SerializedForm(ImmutableSortedMap<K, V> sortedMap) {
            super(sortedMap);
            this.comparator = sortedMap.comparator();
        }

        @Override
        Builder<K, V> makeBuilder(int size) {
            return new Builder(this.comparator);
        }
    }
}

