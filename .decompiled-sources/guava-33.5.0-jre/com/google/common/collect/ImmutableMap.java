/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractMapEntry;
import com.google.common.collect.CollectCollectors;
import com.google.common.collect.CollectPreconditions;
import com.google.common.collect.CollectSpliterators;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableEnumMap;
import com.google.common.collect.ImmutableMapEntry;
import com.google.common.collect.ImmutableMapEntrySet;
import com.google.common.collect.ImmutableMapKeySet;
import com.google.common.collect.ImmutableMapValues;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.JdkBackedImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.RegularImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.annotations.DoNotMock;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.google.j2objc.annotations.RetainedWith;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import org.jspecify.annotations.Nullable;

@DoNotMock(value="Use ImmutableMap.of or another implementation")
@GwtCompatible
public abstract class ImmutableMap<K, V>
implements Map<K, V>,
Serializable {
    static final Map.Entry<?, ?>[] EMPTY_ENTRY_ARRAY = new Map.Entry[0];
    @LazyInit
    @RetainedWith
    private transient @Nullable ImmutableSet<Map.Entry<K, V>> entrySet;
    @LazyInit
    @RetainedWith
    private transient @Nullable ImmutableSet<K> keySet;
    @LazyInit
    @RetainedWith
    private transient @Nullable ImmutableCollection<V> values;
    @LazyInit
    private transient @Nullable ImmutableSetMultimap<K, V> multimapView;
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = -889275714L;

    public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction) {
        return CollectCollectors.toImmutableMap(keyFunction, valueFunction);
    }

    public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction, BinaryOperator<V> mergeFunction) {
        return CollectCollectors.toImmutableMap(keyFunction, valueFunction, mergeFunction);
    }

    public static <K, V> ImmutableMap<K, V> of() {
        return RegularImmutableMap.EMPTY;
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1) {
        return ImmutableBiMap.of(k1, v1);
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2) {
        return RegularImmutableMap.fromEntries(ImmutableMap.entryOf(k1, v1), ImmutableMap.entryOf(k2, v2));
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        return RegularImmutableMap.fromEntries(ImmutableMap.entryOf(k1, v1), ImmutableMap.entryOf(k2, v2), ImmutableMap.entryOf(k3, v3));
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return RegularImmutableMap.fromEntries(ImmutableMap.entryOf(k1, v1), ImmutableMap.entryOf(k2, v2), ImmutableMap.entryOf(k3, v3), ImmutableMap.entryOf(k4, v4));
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return RegularImmutableMap.fromEntries(ImmutableMap.entryOf(k1, v1), ImmutableMap.entryOf(k2, v2), ImmutableMap.entryOf(k3, v3), ImmutableMap.entryOf(k4, v4), ImmutableMap.entryOf(k5, v5));
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        return RegularImmutableMap.fromEntries(ImmutableMap.entryOf(k1, v1), ImmutableMap.entryOf(k2, v2), ImmutableMap.entryOf(k3, v3), ImmutableMap.entryOf(k4, v4), ImmutableMap.entryOf(k5, v5), ImmutableMap.entryOf(k6, v6));
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        return RegularImmutableMap.fromEntries(ImmutableMap.entryOf(k1, v1), ImmutableMap.entryOf(k2, v2), ImmutableMap.entryOf(k3, v3), ImmutableMap.entryOf(k4, v4), ImmutableMap.entryOf(k5, v5), ImmutableMap.entryOf(k6, v6), ImmutableMap.entryOf(k7, v7));
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        return RegularImmutableMap.fromEntries(ImmutableMap.entryOf(k1, v1), ImmutableMap.entryOf(k2, v2), ImmutableMap.entryOf(k3, v3), ImmutableMap.entryOf(k4, v4), ImmutableMap.entryOf(k5, v5), ImmutableMap.entryOf(k6, v6), ImmutableMap.entryOf(k7, v7), ImmutableMap.entryOf(k8, v8));
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        return RegularImmutableMap.fromEntries(ImmutableMap.entryOf(k1, v1), ImmutableMap.entryOf(k2, v2), ImmutableMap.entryOf(k3, v3), ImmutableMap.entryOf(k4, v4), ImmutableMap.entryOf(k5, v5), ImmutableMap.entryOf(k6, v6), ImmutableMap.entryOf(k7, v7), ImmutableMap.entryOf(k8, v8), ImmutableMap.entryOf(k9, v9));
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        return RegularImmutableMap.fromEntries(ImmutableMap.entryOf(k1, v1), ImmutableMap.entryOf(k2, v2), ImmutableMap.entryOf(k3, v3), ImmutableMap.entryOf(k4, v4), ImmutableMap.entryOf(k5, v5), ImmutableMap.entryOf(k6, v6), ImmutableMap.entryOf(k7, v7), ImmutableMap.entryOf(k8, v8), ImmutableMap.entryOf(k9, v9), ImmutableMap.entryOf(k10, v10));
    }

    @SafeVarargs
    public static <K, V> ImmutableMap<K, V> ofEntries(Map.Entry<? extends K, ? extends V> ... entries) {
        Map.Entry<? extends K, ? extends V>[] entries2 = entries;
        return RegularImmutableMap.fromEntries(entries2);
    }

    static <K, V> Map.Entry<K, V> entryOf(K key, V value) {
        return new ImmutableMapEntry<K, V>(key, value);
    }

    public static <K, V> Builder<K, V> builder() {
        return new Builder();
    }

    public static <K, V> Builder<K, V> builderWithExpectedSize(int expectedSize) {
        CollectPreconditions.checkNonnegative(expectedSize, "expectedSize");
        return new Builder(expectedSize);
    }

    static void checkNoConflict(boolean safe, String conflictDescription, Object entry1, Object entry2) {
        if (!safe) {
            throw ImmutableMap.conflictException(conflictDescription, entry1, entry2);
        }
    }

    static IllegalArgumentException conflictException(String conflictDescription, Object entry1, Object entry2) {
        return new IllegalArgumentException("Multiple entries with same " + conflictDescription + ": " + entry1 + " and " + entry2);
    }

    public static <K, V> ImmutableMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
        if (map instanceof ImmutableMap && !(map instanceof SortedMap)) {
            ImmutableMap kvMap = (ImmutableMap)map;
            if (!kvMap.isPartialView()) {
                return kvMap;
            }
        } else if (map instanceof EnumMap) {
            ImmutableMap<K, V> kvMap = ImmutableMap.copyOfEnumMap((EnumMap)map);
            return kvMap;
        }
        return ImmutableMap.copyOf(map.entrySet());
    }

    public static <K, V> ImmutableMap<K, V> copyOf(Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        Map.Entry<?, ?>[] entryArray = Iterables.toArray(entries, EMPTY_ENTRY_ARRAY);
        switch (entryArray.length) {
            case 0: {
                return ImmutableMap.of();
            }
            case 1: {
                Map.Entry<?, ?> onlyEntry = Objects.requireNonNull(entryArray[0]);
                return ImmutableMap.of(onlyEntry.getKey(), onlyEntry.getValue());
            }
        }
        return RegularImmutableMap.fromEntries(entryArray);
    }

    private static <K extends Enum<K>, V> ImmutableMap<K, ? extends V> copyOfEnumMap(EnumMap<?, ? extends V> original) {
        EnumMap copy = new EnumMap(original);
        for (Map.Entry<?, V> entry : copy.entrySet()) {
            CollectPreconditions.checkEntryNotNull(entry.getKey(), entry.getValue());
        }
        return ImmutableEnumMap.asImmutable(copy);
    }

    ImmutableMap() {
    }

    @Override
    @Deprecated
    @CanIgnoreReturnValue
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final @Nullable V put(K k, V v) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    @CanIgnoreReturnValue
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final @Nullable V putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final boolean replace(K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final @Nullable V replace(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final @Nullable V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends @Nullable V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final @Nullable V compute(K key, BiFunction<? super K, ? super @Nullable V, ? extends @Nullable V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final @Nullable V merge(K key, V value, BiFunction<? super V, ? super V, ? extends @Nullable V> function) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final void putAll(Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final @Nullable V remove(@Nullable Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final boolean remove(@Nullable Object key, @Nullable Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        return this.get(key) != null;
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        return ((ImmutableCollection)this.values()).contains(value);
    }

    @Override
    public abstract @Nullable V get(@Nullable Object var1);

    @Override
    public final @Nullable V getOrDefault(@Nullable Object key, @Nullable V defaultValue) {
        V result = this.get(key);
        if (result != null) {
            return result;
        }
        return defaultValue;
    }

    @Override
    public ImmutableSet<Map.Entry<K, V>> entrySet() {
        ImmutableSet<Map.Entry<K, V>> result = this.entrySet;
        return result == null ? (this.entrySet = this.createEntrySet()) : result;
    }

    abstract ImmutableSet<Map.Entry<K, V>> createEntrySet();

    @Override
    public ImmutableSet<K> keySet() {
        ImmutableSet<K> result = this.keySet;
        return result == null ? (this.keySet = this.createKeySet()) : result;
    }

    abstract ImmutableSet<K> createKeySet();

    UnmodifiableIterator<K> keyIterator() {
        Iterator entryIterator = ((ImmutableSet)this.entrySet()).iterator();
        return new UnmodifiableIterator<K>(this, (UnmodifiableIterator)entryIterator){
            final /* synthetic */ UnmodifiableIterator val$entryIterator;
            final /* synthetic */ ImmutableMap this$0;
            {
                this.val$entryIterator = unmodifiableIterator;
                this.this$0 = this$0;
            }

            @Override
            public boolean hasNext() {
                return this.val$entryIterator.hasNext();
            }

            @Override
            public K next() {
                return ((Map.Entry)this.val$entryIterator.next()).getKey();
            }
        };
    }

    Spliterator<K> keySpliterator() {
        return CollectSpliterators.map(((ImmutableCollection)((Object)this.entrySet())).spliterator(), Map.Entry::getKey);
    }

    @Override
    public ImmutableCollection<V> values() {
        ImmutableCollection<V> result = this.values;
        return result == null ? (this.values = this.createValues()) : result;
    }

    abstract ImmutableCollection<V> createValues();

    public ImmutableSetMultimap<K, V> asMultimap() {
        if (this.isEmpty()) {
            return ImmutableSetMultimap.of();
        }
        ImmutableSetMultimap<K, V> result = this.multimapView;
        return result == null ? (this.multimapView = new ImmutableSetMultimap(new MapViewOfValuesAsSingletonSets(), this.size(), null)) : result;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        return Maps.equalsImpl(this, object);
    }

    abstract boolean isPartialView();

    @Override
    public int hashCode() {
        return Sets.hashCodeImpl(this.entrySet());
    }

    boolean isHashCodeFast() {
        return false;
    }

    public String toString() {
        return Maps.toStringImpl(this);
    }

    @J2ktIncompatible
    Object writeReplace() {
        return new SerializedForm(this);
    }

    @J2ktIncompatible
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Use SerializedForm");
    }

    @DoNotMock
    public static class Builder<K, V> {
        @Nullable Comparator<? super V> valueComparator;
        @Nullable Map.Entry<K, V>[] entries;
        int size;
        boolean entriesUsed;

        public Builder() {
            this(4);
        }

        Builder(int initialCapacity) {
            this.entries = new Map.Entry[initialCapacity];
            this.size = 0;
            this.entriesUsed = false;
        }

        private void ensureCapacity(int minCapacity) {
            if (minCapacity > this.entries.length) {
                this.entries = Arrays.copyOf(this.entries, ImmutableCollection.Builder.expandedCapacity(this.entries.length, minCapacity));
                this.entriesUsed = false;
            }
        }

        @CanIgnoreReturnValue
        public Builder<K, V> put(K key, V value) {
            this.ensureCapacity(this.size + 1);
            Map.Entry<K, V> entry = ImmutableMap.entryOf(key, value);
            this.entries[this.size++] = entry;
            return this;
        }

        @CanIgnoreReturnValue
        public Builder<K, V> put(Map.Entry<? extends K, ? extends V> entry) {
            return this.put(entry.getKey(), entry.getValue());
        }

        @CanIgnoreReturnValue
        public Builder<K, V> putAll(Map<? extends K, ? extends V> map) {
            return this.putAll(map.entrySet());
        }

        @CanIgnoreReturnValue
        public Builder<K, V> putAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
            if (entries instanceof Collection) {
                this.ensureCapacity(this.size + ((Collection)entries).size());
            }
            for (Map.Entry<K, V> entry : entries) {
                this.put(entry);
            }
            return this;
        }

        @CanIgnoreReturnValue
        public Builder<K, V> orderEntriesByValue(Comparator<? super V> valueComparator) {
            Preconditions.checkState(this.valueComparator == null, "valueComparator was already set");
            this.valueComparator = Preconditions.checkNotNull(valueComparator, "valueComparator");
            return this;
        }

        @CanIgnoreReturnValue
        Builder<K, V> combine(Builder<K, V> other) {
            Preconditions.checkNotNull(other);
            this.ensureCapacity(this.size + other.size);
            System.arraycopy(other.entries, 0, this.entries, this.size, other.size);
            this.size += other.size;
            return this;
        }

        private ImmutableMap<K, V> build(boolean throwIfDuplicateKeys) {
            Map.Entry<K, V>[] localEntries;
            switch (this.size) {
                case 0: {
                    return ImmutableMap.of();
                }
                case 1: {
                    Map.Entry<K, V> onlyEntry = Objects.requireNonNull(this.entries[0]);
                    return ImmutableMap.of(onlyEntry.getKey(), onlyEntry.getValue());
                }
            }
            int localSize = this.size;
            if (this.valueComparator == null) {
                localEntries = this.entries;
            } else {
                Map.Entry<K, V>[] lastEntryForEachKey;
                if (this.entriesUsed) {
                    this.entries = Arrays.copyOf(this.entries, this.size);
                }
                Map.Entry<K, V>[] nonNullEntries = this.entries;
                if (!throwIfDuplicateKeys && (lastEntryForEachKey = Builder.lastEntryForEachKey(nonNullEntries, this.size)) != null) {
                    nonNullEntries = lastEntryForEachKey;
                    localSize = lastEntryForEachKey.length;
                }
                Arrays.sort(nonNullEntries, 0, localSize, Ordering.from(this.valueComparator).onResultOf(Maps.valueFunction()));
                localEntries = nonNullEntries;
            }
            this.entriesUsed = true;
            return RegularImmutableMap.fromEntryArray(localSize, localEntries, throwIfDuplicateKeys);
        }

        public ImmutableMap<K, V> build() {
            return this.buildOrThrow();
        }

        public ImmutableMap<K, V> buildOrThrow() {
            return this.build(true);
        }

        public ImmutableMap<K, V> buildKeepingLast() {
            return this.build(false);
        }

        @VisibleForTesting
        ImmutableMap<K, V> buildJdkBacked() {
            Preconditions.checkState(this.valueComparator == null, "buildJdkBacked is only for testing; can't use valueComparator");
            switch (this.size) {
                case 0: {
                    return ImmutableMap.of();
                }
                case 1: {
                    Map.Entry<K, V> onlyEntry = Objects.requireNonNull(this.entries[0]);
                    return ImmutableMap.of(onlyEntry.getKey(), onlyEntry.getValue());
                }
            }
            this.entriesUsed = true;
            return JdkBackedImmutableMap.create(this.size, this.entries, true);
        }

        private static <K, V> Map.Entry<K, V> @Nullable [] lastEntryForEachKey(Map.Entry<K, V>[] entries, int size) {
            HashSet<K> seen = new HashSet<K>();
            BitSet dups = new BitSet();
            for (int i = size - 1; i >= 0; --i) {
                if (seen.add(entries[i].getKey())) continue;
                dups.set(i);
            }
            if (dups.isEmpty()) {
                return null;
            }
            Map.Entry[] newEntries = new Map.Entry[size - dups.cardinality()];
            int outI = 0;
            for (int inI = 0; inI < size; ++inI) {
                if (dups.get(inI)) continue;
                newEntries[outI++] = entries[inI];
            }
            return newEntries;
        }
    }

    private final class MapViewOfValuesAsSingletonSets
    extends IteratorBasedImmutableMap<K, ImmutableSet<V>> {
        private MapViewOfValuesAsSingletonSets() {
        }

        @Override
        public int size() {
            return ImmutableMap.this.size();
        }

        @Override
        ImmutableSet<K> createKeySet() {
            return ImmutableMap.this.keySet();
        }

        @Override
        public boolean containsKey(@Nullable Object key) {
            return ImmutableMap.this.containsKey(key);
        }

        @Override
        public @Nullable ImmutableSet<V> get(@Nullable Object key) {
            Object outerValue = ImmutableMap.this.get(key);
            return outerValue == null ? null : ImmutableSet.of(outerValue);
        }

        @Override
        boolean isPartialView() {
            return ImmutableMap.this.isPartialView();
        }

        @Override
        public int hashCode() {
            return ImmutableMap.this.hashCode();
        }

        @Override
        boolean isHashCodeFast() {
            return ImmutableMap.this.isHashCodeFast();
        }

        @Override
        UnmodifiableIterator<Map.Entry<K, ImmutableSet<V>>> entryIterator() {
            final Iterator backingIterator = ((ImmutableSet)ImmutableMap.this.entrySet()).iterator();
            return new UnmodifiableIterator<Map.Entry<K, ImmutableSet<V>>>(this){
                final /* synthetic */ MapViewOfValuesAsSingletonSets this$1;
                {
                    this.this$1 = this$1;
                }

                @Override
                public boolean hasNext() {
                    return backingIterator.hasNext();
                }

                @Override
                public Map.Entry<K, ImmutableSet<V>> next() {
                    final Map.Entry backingEntry = (Map.Entry)backingIterator.next();
                    return new AbstractMapEntry<K, ImmutableSet<V>>(this){
                        final /* synthetic */ 1 this$2;
                        {
                            this.this$2 = this$2;
                        }

                        @Override
                        public K getKey() {
                            return backingEntry.getKey();
                        }

                        @Override
                        public ImmutableSet<V> getValue() {
                            return ImmutableSet.of(backingEntry.getValue());
                        }
                    };
                }
            };
        }

        @Override
        @J2ktIncompatible
        @GwtIncompatible
        Object writeReplace() {
            return super.writeReplace();
        }
    }

    @J2ktIncompatible
    static class SerializedForm<K, V>
    implements Serializable {
        private static final boolean USE_LEGACY_SERIALIZATION = true;
        private final Object keys;
        private final Object values;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        SerializedForm(ImmutableMap<K, V> map) {
            Object[] keys = new Object[map.size()];
            Object[] values = new Object[map.size()];
            int i = 0;
            for (Map.Entry entry : map.entrySet()) {
                keys[i] = entry.getKey();
                values[i] = entry.getValue();
                ++i;
            }
            this.keys = keys;
            this.values = values;
        }

        final Object readResolve() {
            if (!(this.keys instanceof ImmutableSet)) {
                return this.legacyReadResolve();
            }
            ImmutableSet keySet = (ImmutableSet)this.keys;
            ImmutableCollection values = (ImmutableCollection)this.values;
            Builder builder = this.makeBuilder(keySet.size());
            Iterator keyIter = keySet.iterator();
            Iterator valueIter = values.iterator();
            while (keyIter.hasNext()) {
                builder.put(keyIter.next(), valueIter.next());
            }
            return builder.buildOrThrow();
        }

        final Object legacyReadResolve() {
            Object[] keys = (Object[])this.keys;
            Object[] values = (Object[])this.values;
            Builder<Object, Object> builder = this.makeBuilder(keys.length);
            for (int i = 0; i < keys.length; ++i) {
                builder.put(keys[i], values[i]);
            }
            return builder.buildOrThrow();
        }

        Builder<K, V> makeBuilder(int size) {
            return new Builder(size);
        }
    }

    static abstract class IteratorBasedImmutableMap<K, V>
    extends ImmutableMap<K, V> {
        IteratorBasedImmutableMap() {
        }

        abstract UnmodifiableIterator<Map.Entry<K, V>> entryIterator();

        Spliterator<Map.Entry<K, V>> entrySpliterator() {
            return Spliterators.spliterator(this.entryIterator(), (long)this.size(), 1297);
        }

        @Override
        ImmutableSet<K> createKeySet() {
            return new ImmutableMapKeySet(this);
        }

        @Override
        ImmutableSet<Map.Entry<K, V>> createEntrySet() {
            final class EntrySetImpl
            extends ImmutableMapEntrySet<K, V> {
                EntrySetImpl() {
                }

                @Override
                ImmutableMap<K, V> map() {
                    return IteratorBasedImmutableMap.this;
                }

                @Override
                public UnmodifiableIterator<Map.Entry<K, V>> iterator() {
                    return IteratorBasedImmutableMap.this.entryIterator();
                }

                @Override
                @J2ktIncompatible
                @GwtIncompatible
                Object writeReplace() {
                    return super.writeReplace();
                }
            }
            return new EntrySetImpl();
        }

        @Override
        ImmutableCollection<V> createValues() {
            return new ImmutableMapValues(this);
        }

        @Override
        @J2ktIncompatible
        @GwtIncompatible
        Object writeReplace() {
            return super.writeReplace();
        }
    }
}

