/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.CollectCollectors;
import com.google.common.collect.CollectPreconditions;
import com.google.common.collect.EmptyImmutableSetMultimap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Serialization;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.UnmodifiableIterator;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.google.j2objc.annotations.RetainedWith;
import com.google.j2objc.annotations.Weak;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public class ImmutableSetMultimap<K, V>
extends ImmutableMultimap<K, V>
implements SetMultimap<K, V> {
    private final transient ImmutableSet<V> emptySet;
    @LazyInit
    @RetainedWith
    private transient @Nullable ImmutableSetMultimap<V, K> inverse;
    @LazyInit
    @RetainedWith
    private transient @Nullable ImmutableSet<Map.Entry<K, V>> entries;
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = 0L;

    public static <T, K, V> Collector<T, ?, ImmutableSetMultimap<K, V>> toImmutableSetMultimap(Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction) {
        return CollectCollectors.toImmutableSetMultimap(keyFunction, valueFunction);
    }

    public static <T, K, V> Collector<T, ?, ImmutableSetMultimap<K, V>> flatteningToImmutableSetMultimap(Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends Stream<? extends V>> valuesFunction) {
        return CollectCollectors.flatteningToImmutableSetMultimap(keyFunction, valuesFunction);
    }

    public static <K, V> ImmutableSetMultimap<K, V> of() {
        return EmptyImmutableSetMultimap.INSTANCE;
    }

    public static <K, V> ImmutableSetMultimap<K, V> of(K k1, V v1) {
        Builder<K, V> builder = ImmutableSetMultimap.builder();
        builder.put((Object)k1, (Object)v1);
        return builder.build();
    }

    public static <K, V> ImmutableSetMultimap<K, V> of(K k1, V v1, K k2, V v2) {
        Builder<K, V> builder = ImmutableSetMultimap.builder();
        builder.put((Object)k1, (Object)v1);
        builder.put((Object)k2, (Object)v2);
        return builder.build();
    }

    public static <K, V> ImmutableSetMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        Builder<K, V> builder = ImmutableSetMultimap.builder();
        builder.put((Object)k1, (Object)v1);
        builder.put((Object)k2, (Object)v2);
        builder.put((Object)k3, (Object)v3);
        return builder.build();
    }

    public static <K, V> ImmutableSetMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        Builder<K, V> builder = ImmutableSetMultimap.builder();
        builder.put((Object)k1, (Object)v1);
        builder.put((Object)k2, (Object)v2);
        builder.put((Object)k3, (Object)v3);
        builder.put((Object)k4, (Object)v4);
        return builder.build();
    }

    public static <K, V> ImmutableSetMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        Builder<K, V> builder = ImmutableSetMultimap.builder();
        builder.put((Object)k1, (Object)v1);
        builder.put((Object)k2, (Object)v2);
        builder.put((Object)k3, (Object)v3);
        builder.put((Object)k4, (Object)v4);
        builder.put((Object)k5, (Object)v5);
        return builder.build();
    }

    public static <K, V> Builder<K, V> builder() {
        return new Builder();
    }

    public static <K, V> Builder<K, V> builderWithExpectedKeys(int expectedKeys) {
        CollectPreconditions.checkNonnegative(expectedKeys, "expectedKeys");
        return new Builder(expectedKeys);
    }

    public static <K, V> ImmutableSetMultimap<K, V> copyOf(Multimap<? extends K, ? extends V> multimap) {
        return ImmutableSetMultimap.copyOf(multimap, null);
    }

    private static <K, V> ImmutableSetMultimap<K, V> copyOf(Multimap<? extends K, ? extends V> multimap, @Nullable Comparator<? super V> valueComparator) {
        ImmutableSetMultimap kvMultimap;
        Preconditions.checkNotNull(multimap);
        if (multimap.isEmpty() && valueComparator == null) {
            return ImmutableSetMultimap.of();
        }
        if (multimap instanceof ImmutableSetMultimap && !(kvMultimap = (ImmutableSetMultimap)multimap).isPartialView()) {
            return kvMultimap;
        }
        return ImmutableSetMultimap.fromMapEntries(multimap.asMap().entrySet(), valueComparator);
    }

    public static <K, V> ImmutableSetMultimap<K, V> copyOf(Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        return ((Builder)new Builder().putAll((Iterable)entries)).build();
    }

    static <K, V> ImmutableSetMultimap<K, V> fromMapEntries(Collection<? extends Map.Entry<? extends K, ? extends Collection<? extends V>>> mapEntries, @Nullable Comparator<? super V> valueComparator) {
        if (mapEntries.isEmpty()) {
            return ImmutableSetMultimap.of();
        }
        ImmutableMap.Builder<K, ImmutableSet<V>> builder = new ImmutableMap.Builder<K, ImmutableSet<V>>(mapEntries.size());
        int size = 0;
        for (Map.Entry<K, Collection<V>> entry : mapEntries) {
            K key = entry.getKey();
            Collection<? extends V> values = entry.getValue();
            ImmutableSet<V> set = ImmutableSetMultimap.valueSet(valueComparator, values);
            if (set.isEmpty()) continue;
            builder.put(key, set);
            size += set.size();
        }
        return new ImmutableSetMultimap(builder.buildOrThrow(), size, valueComparator);
    }

    static <K, V> ImmutableSetMultimap<K, V> fromMapBuilderEntries(Collection<? extends Map.Entry<K, ImmutableCollection.Builder<V>>> mapEntries, @Nullable Comparator<? super V> valueComparator) {
        if (mapEntries.isEmpty()) {
            return ImmutableSetMultimap.of();
        }
        ImmutableMap.Builder<K, ImmutableSet<V>> builder = new ImmutableMap.Builder<K, ImmutableSet<V>>(mapEntries.size());
        int size = 0;
        for (Map.Entry<K, ImmutableCollection.Builder<V>> entry : mapEntries) {
            K key = entry.getKey();
            ImmutableSet.Builder values = (ImmutableSet.Builder)entry.getValue();
            ImmutableSet<V> set = ImmutableSetMultimap.valueSet(valueComparator, values.build());
            if (set.isEmpty()) continue;
            builder.put(key, set);
            size += set.size();
        }
        return new ImmutableSetMultimap(builder.buildOrThrow(), size, valueComparator);
    }

    ImmutableSetMultimap(ImmutableMap<K, ImmutableSet<V>> map, int size, @Nullable Comparator<? super V> valueComparator) {
        super(map, size);
        this.emptySet = ImmutableSetMultimap.emptySet(valueComparator);
    }

    @Override
    public ImmutableSet<V> get(K key) {
        ImmutableSet set = (ImmutableSet)this.map.get(key);
        return MoreObjects.firstNonNull(set, this.emptySet);
    }

    @Override
    public ImmutableSetMultimap<V, K> inverse() {
        ImmutableSetMultimap<K, V> result = this.inverse;
        return result == null ? (this.inverse = this.invert()) : result;
    }

    private ImmutableSetMultimap<V, K> invert() {
        Builder<K, V> builder = ImmutableSetMultimap.builder();
        for (Map.Entry entry : this.entries()) {
            builder.put(entry.getValue(), entry.getKey());
        }
        ImmutableMultimap invertedMultimap = builder.build();
        ((ImmutableSetMultimap)invertedMultimap).inverse = this;
        return invertedMultimap;
    }

    @Override
    @Deprecated
    @CanIgnoreReturnValue
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final ImmutableSet<V> removeAll(@Nullable Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    @CanIgnoreReturnValue
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final ImmutableSet<V> replaceValues(K key, Iterable<? extends V> values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ImmutableSet<Map.Entry<K, V>> entries() {
        ImmutableSet<Map.Entry<K, V>> result = this.entries;
        return result == null ? (this.entries = new EntrySet(this)) : result;
    }

    private static <V> ImmutableSet<V> valueSet(@Nullable Comparator<? super V> valueComparator, Collection<? extends V> values) {
        return valueComparator == null ? ImmutableSet.copyOf(values) : ImmutableSortedSet.copyOf(valueComparator, values);
    }

    private static <V> ImmutableSet<V> emptySet(@Nullable Comparator<? super V> valueComparator) {
        return valueComparator == null ? ImmutableSet.of() : ImmutableSortedSet.emptySet(valueComparator);
    }

    private static <V> ImmutableSet.Builder<V> valuesBuilder(@Nullable Comparator<? super V> valueComparator) {
        return valueComparator == null ? new ImmutableSet.Builder() : new ImmutableSortedSet.Builder<V>(valueComparator);
    }

    @GwtIncompatible
    @J2ktIncompatible
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeObject(this.valueComparator());
        Serialization.writeMultimap(this, stream);
    }

    @Nullable Comparator<? super V> valueComparator() {
        return this.emptySet instanceof ImmutableSortedSet ? ((ImmutableSortedSet)this.emptySet).comparator() : null;
    }

    @GwtIncompatible
    @J2ktIncompatible
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        ImmutableMap tmpMap;
        stream.defaultReadObject();
        Comparator valueComparator = (Comparator)stream.readObject();
        int keyCount = stream.readInt();
        if (keyCount < 0) {
            throw new InvalidObjectException("Invalid key count " + keyCount);
        }
        ImmutableMap.Builder<Object, ImmutableCollection> builder = ImmutableMap.builder();
        int tmpSize = 0;
        for (int i = 0; i < keyCount; ++i) {
            Object key = Objects.requireNonNull(stream.readObject());
            int valueCount = stream.readInt();
            if (valueCount <= 0) {
                throw new InvalidObjectException("Invalid value count " + valueCount);
            }
            ImmutableSet.Builder<V> valuesBuilder = ImmutableSetMultimap.valuesBuilder(valueComparator);
            for (int j = 0; j < valueCount; ++j) {
                valuesBuilder.add(Objects.requireNonNull(stream.readObject()));
            }
            ImmutableCollection valueSet = valuesBuilder.build();
            if (valueSet.size() != valueCount) {
                throw new InvalidObjectException("Duplicate key-value pairs exist for key " + key);
            }
            builder.put(key, valueSet);
            tmpSize += valueCount;
        }
        try {
            tmpMap = builder.buildOrThrow();
        }
        catch (IllegalArgumentException e) {
            throw (InvalidObjectException)new InvalidObjectException(e.getMessage()).initCause(e);
        }
        ImmutableMultimap.FieldSettersHolder.MAP_FIELD_SETTER.set(this, tmpMap);
        ImmutableMultimap.FieldSettersHolder.SIZE_FIELD_SETTER.set((ImmutableMultimap<?, ?>)this, tmpSize);
        SetFieldSettersHolder.EMPTY_SET_FIELD_SETTER.set(this, ImmutableSetMultimap.emptySet(valueComparator));
    }

    public static final class Builder<K, V>
    extends ImmutableMultimap.Builder<K, V> {
        public Builder() {
        }

        Builder(int expectedKeys) {
            super(expectedKeys);
        }

        @Override
        ImmutableCollection.Builder<V> newValueCollectionBuilderWithExpectedSize(int expectedSize) {
            return this.valueComparator == null ? ImmutableSet.builderWithExpectedSize(expectedSize) : new ImmutableSortedSet.Builder(this.valueComparator, expectedSize);
        }

        @Override
        int expectedValueCollectionSize(int defaultExpectedValues, Iterable<?> values) {
            if (values instanceof Set) {
                Set collection = (Set)values;
                return Math.max(defaultExpectedValues, collection.size());
            }
            return defaultExpectedValues;
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<K, V> expectedValuesPerKey(int expectedValuesPerKey) {
            super.expectedValuesPerKey(expectedValuesPerKey);
            return this;
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
        public Builder<K, V> putAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
            super.putAll(entries);
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<K, V> putAll(K key, Iterable<? extends V> values) {
            super.putAll(key, values);
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<K, V> putAll(K key, V ... values) {
            return this.putAll((Object)key, Arrays.asList(values));
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<K, V> putAll(Multimap<? extends K, ? extends V> multimap) {
            for (Map.Entry<K, Collection<V>> entry : multimap.asMap().entrySet()) {
                this.putAll((Object)entry.getKey(), entry.getValue());
            }
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        Builder<K, V> combine(ImmutableMultimap.Builder<K, V> other) {
            super.combine(other);
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<K, V> orderKeysBy(Comparator<? super K> keyComparator) {
            super.orderKeysBy(keyComparator);
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<K, V> orderValuesBy(Comparator<? super V> valueComparator) {
            super.orderValuesBy(valueComparator);
            return this;
        }

        @Override
        public ImmutableSetMultimap<K, V> build() {
            if (this.builderMap == null) {
                return ImmutableSetMultimap.of();
            }
            Collection mapEntries = this.builderMap.entrySet();
            if (this.keyComparator != null) {
                mapEntries = Ordering.from(this.keyComparator).onKeys().immutableSortedCopy(mapEntries);
            }
            return ImmutableSetMultimap.fromMapBuilderEntries(mapEntries, this.valueComparator);
        }
    }

    private static final class EntrySet<K, V>
    extends ImmutableSet<Map.Entry<K, V>> {
        @Weak
        private final transient ImmutableSetMultimap<K, V> multimap;

        EntrySet(ImmutableSetMultimap<K, V> multimap) {
            this.multimap = multimap;
        }

        @Override
        public boolean contains(@Nullable Object object) {
            if (object instanceof Map.Entry) {
                Map.Entry entry = (Map.Entry)object;
                return this.multimap.containsEntry(entry.getKey(), entry.getValue());
            }
            return false;
        }

        @Override
        public int size() {
            return this.multimap.size();
        }

        @Override
        public UnmodifiableIterator<Map.Entry<K, V>> iterator() {
            return this.multimap.entryIterator();
        }

        @Override
        boolean isPartialView() {
            return false;
        }

        @Override
        @J2ktIncompatible
        @GwtIncompatible
        Object writeReplace() {
            return super.writeReplace();
        }
    }

    @GwtIncompatible
    @J2ktIncompatible
    private static final class SetFieldSettersHolder {
        static final Serialization.FieldSetter<? super ImmutableSetMultimap<?, ?>> EMPTY_SET_FIELD_SETTER = Serialization.getFieldSetter(ImmutableSetMultimap.class, "emptySet");

        private SetFieldSettersHolder() {
        }
    }
}

