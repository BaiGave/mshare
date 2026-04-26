/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.BaseImmutableMultimap;
import com.google.common.collect.CollectPreconditions;
import com.google.common.collect.CollectSpliterators;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.Ordering;
import com.google.common.collect.Platform;
import com.google.common.collect.Serialization;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.UnmodifiableIterator;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.annotations.DoNotMock;
import com.google.j2objc.annotations.Weak;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class ImmutableMultimap<K, V>
extends BaseImmutableMultimap<K, V>
implements Serializable {
    final transient ImmutableMap<K, ? extends ImmutableCollection<V>> map;
    final transient int size;
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = 0L;

    public static <K, V> ImmutableMultimap<K, V> of() {
        return ImmutableListMultimap.of();
    }

    public static <K, V> ImmutableMultimap<K, V> of(K k1, V v1) {
        return ImmutableListMultimap.of(k1, v1);
    }

    public static <K, V> ImmutableMultimap<K, V> of(K k1, V v1, K k2, V v2) {
        return ImmutableListMultimap.of(k1, v1, k2, v2);
    }

    public static <K, V> ImmutableMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        return ImmutableListMultimap.of(k1, v1, k2, v2, k3, v3);
    }

    public static <K, V> ImmutableMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return ImmutableListMultimap.of(k1, v1, k2, v2, k3, v3, k4, v4);
    }

    public static <K, V> ImmutableMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return ImmutableListMultimap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
    }

    public static <K, V> Builder<K, V> builder() {
        return new Builder();
    }

    public static <K, V> Builder<K, V> builderWithExpectedKeys(int expectedKeys) {
        CollectPreconditions.checkNonnegative(expectedKeys, "expectedKeys");
        return new Builder(expectedKeys);
    }

    public static <K, V> ImmutableMultimap<K, V> copyOf(Multimap<? extends K, ? extends V> multimap) {
        ImmutableMultimap kvMultimap;
        if (multimap instanceof ImmutableMultimap && !(kvMultimap = (ImmutableMultimap)multimap).isPartialView()) {
            return kvMultimap;
        }
        return ImmutableListMultimap.copyOf(multimap);
    }

    public static <K, V> ImmutableMultimap<K, V> copyOf(Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        return ImmutableListMultimap.copyOf(entries);
    }

    ImmutableMultimap(ImmutableMap<K, ? extends ImmutableCollection<V>> map, int size) {
        this.map = map;
        this.size = size;
    }

    @Override
    @Deprecated
    @CanIgnoreReturnValue
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public ImmutableCollection<V> removeAll(@Nullable Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    @CanIgnoreReturnValue
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public ImmutableCollection<V> replaceValues(K key, Iterable<? extends V> values) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public abstract ImmutableCollection<V> get(K var1);

    public abstract ImmutableMultimap<V, K> inverse();

    @Override
    @Deprecated
    @CanIgnoreReturnValue
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final boolean put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    @CanIgnoreReturnValue
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final boolean putAll(K key, Iterable<? extends V> values) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    @CanIgnoreReturnValue
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final boolean putAll(Multimap<? extends K, ? extends V> multimap) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    @CanIgnoreReturnValue
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final boolean remove(@Nullable Object key, @Nullable Object value) {
        throw new UnsupportedOperationException();
    }

    boolean isPartialView() {
        return this.map.isPartialView();
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        return value != null && super.containsValue(value);
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public ImmutableSet<K> keySet() {
        return this.map.keySet();
    }

    @Override
    Set<K> createKeySet() {
        throw new AssertionError((Object)"unreachable");
    }

    @Override
    public ImmutableMap<K, Collection<V>> asMap() {
        return this.map;
    }

    @Override
    Map<K, Collection<V>> createAsMap() {
        throw new AssertionError((Object)"should never be called");
    }

    @Override
    public ImmutableCollection<Map.Entry<K, V>> entries() {
        return (ImmutableCollection)super.entries();
    }

    @Override
    ImmutableCollection<Map.Entry<K, V>> createEntries() {
        return new EntryCollection(this);
    }

    @Override
    UnmodifiableIterator<Map.Entry<K, V>> entryIterator() {
        return new UnmodifiableIterator<Map.Entry<K, V>>(){
            final Iterator<? extends Map.Entry<K, ? extends ImmutableCollection<V>>> asMapItr;
            @Nullable K currentKey;
            Iterator<V> valueItr;
            {
                this.asMapItr = ((ImmutableSet)ImmutableMultimap.this.map.entrySet()).iterator();
                this.currentKey = null;
                this.valueItr = Iterators.emptyIterator();
            }

            @Override
            public boolean hasNext() {
                return this.valueItr.hasNext() || this.asMapItr.hasNext();
            }

            @Override
            public Map.Entry<K, V> next() {
                if (!this.valueItr.hasNext()) {
                    Map.Entry entry = this.asMapItr.next();
                    this.currentKey = entry.getKey();
                    this.valueItr = entry.getValue().iterator();
                }
                return Maps.immutableEntry(Objects.requireNonNull(this.currentKey), this.valueItr.next());
            }
        };
    }

    @Override
    Spliterator<Map.Entry<K, V>> entrySpliterator() {
        return CollectSpliterators.flatMap(((ImmutableCollection)((Object)((ImmutableMap)this.asMap()).entrySet())).spliterator(), keyToValueCollectionEntry -> {
            Object key = keyToValueCollectionEntry.getKey();
            Collection valueCollection = (Collection)keyToValueCollectionEntry.getValue();
            return CollectSpliterators.map(valueCollection.spliterator(), value -> Maps.immutableEntry(key, value));
        }, 0x40 | (this instanceof SetMultimap ? 1 : 0), this.size());
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Preconditions.checkNotNull(action);
        this.asMap().forEach((key, valueCollection) -> valueCollection.forEach((? super T value) -> action.accept((Object)key, (Object)value)));
    }

    @Override
    public ImmutableMultiset<K> keys() {
        return (ImmutableMultiset)super.keys();
    }

    @Override
    ImmutableMultiset<K> createKeys() {
        return new Keys();
    }

    @Override
    public ImmutableCollection<V> values() {
        return (ImmutableCollection)super.values();
    }

    @Override
    ImmutableCollection<V> createValues() {
        return new Values(this);
    }

    @Override
    UnmodifiableIterator<V> valueIterator() {
        return new UnmodifiableIterator<V>(){
            final Iterator<? extends ImmutableCollection<V>> valueCollectionItr;
            Iterator<V> valueItr;
            {
                this.valueCollectionItr = ((ImmutableCollection)ImmutableMultimap.this.map.values()).iterator();
                this.valueItr = Iterators.emptyIterator();
            }

            @Override
            public boolean hasNext() {
                return this.valueItr.hasNext() || this.valueCollectionItr.hasNext();
            }

            @Override
            public V next() {
                if (!this.valueItr.hasNext()) {
                    this.valueItr = this.valueCollectionItr.next().iterator();
                }
                return this.valueItr.next();
            }
        };
    }

    @DoNotMock
    public static class Builder<K, V> {
        @Nullable Map<K, ImmutableCollection.Builder<V>> builderMap;
        @Nullable Comparator<? super K> keyComparator;
        @Nullable Comparator<? super V> valueComparator;
        int expectedValuesPerKey = 4;

        public Builder() {
        }

        Builder(int expectedKeys) {
            if (expectedKeys > 0) {
                this.builderMap = Platform.preservesInsertionOrderOnPutsMapWithExpectedSize(expectedKeys);
            }
        }

        Map<K, ImmutableCollection.Builder<V>> ensureBuilderMapNonNull() {
            Map<K, ImmutableCollection.Builder<V>> result = this.builderMap;
            if (result == null) {
                this.builderMap = result = Platform.preservesInsertionOrderOnPutsMap();
            }
            return result;
        }

        ImmutableCollection.Builder<V> newValueCollectionBuilderWithExpectedSize(int expectedSize) {
            return ImmutableList.builderWithExpectedSize(expectedSize);
        }

        @CanIgnoreReturnValue
        public Builder<K, V> expectedValuesPerKey(int expectedValuesPerKey) {
            CollectPreconditions.checkNonnegative(expectedValuesPerKey, "expectedValuesPerKey");
            this.expectedValuesPerKey = Math.max(expectedValuesPerKey, 1);
            return this;
        }

        int expectedValueCollectionSize(int defaultExpectedValues, Iterable<?> values) {
            if (values instanceof Collection) {
                Collection collection = (Collection)values;
                return Math.max(defaultExpectedValues, collection.size());
            }
            return defaultExpectedValues;
        }

        @CanIgnoreReturnValue
        public Builder<K, V> put(K key, V value) {
            CollectPreconditions.checkEntryNotNull(key, value);
            ImmutableCollection.Builder<V> valuesBuilder = this.ensureBuilderMapNonNull().get(key);
            if (valuesBuilder == null) {
                valuesBuilder = this.newValueCollectionBuilderWithExpectedSize(this.expectedValuesPerKey);
                this.ensureBuilderMapNonNull().put(key, valuesBuilder);
            }
            valuesBuilder.add(value);
            return this;
        }

        @CanIgnoreReturnValue
        public Builder<K, V> put(Map.Entry<? extends K, ? extends V> entry) {
            return this.put(entry.getKey(), entry.getValue());
        }

        @CanIgnoreReturnValue
        public Builder<K, V> putAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
            for (Map.Entry<K, V> entry : entries) {
                this.put(entry);
            }
            return this;
        }

        @CanIgnoreReturnValue
        public Builder<K, V> putAll(K key, Iterable<? extends V> values) {
            if (key == null) {
                throw new NullPointerException("null key in entry: null=" + Iterables.toString(values));
            }
            Iterator<V> valuesItr = values.iterator();
            if (!valuesItr.hasNext()) {
                return this;
            }
            ImmutableCollection.Builder<V> valuesBuilder = this.ensureBuilderMapNonNull().get(key);
            if (valuesBuilder == null) {
                valuesBuilder = this.newValueCollectionBuilderWithExpectedSize(this.expectedValueCollectionSize(this.expectedValuesPerKey, values));
                this.ensureBuilderMapNonNull().put(key, valuesBuilder);
            }
            while (valuesItr.hasNext()) {
                V value = valuesItr.next();
                CollectPreconditions.checkEntryNotNull(key, value);
                valuesBuilder.add(value);
            }
            return this;
        }

        @CanIgnoreReturnValue
        public Builder<K, V> putAll(K key, V ... values) {
            return this.putAll(key, (Iterable<? extends V>)Arrays.asList(values));
        }

        @CanIgnoreReturnValue
        public Builder<K, V> putAll(Multimap<? extends K, ? extends V> multimap) {
            for (Map.Entry<K, Collection<V>> entry : multimap.asMap().entrySet()) {
                this.putAll(entry.getKey(), (Iterable)entry.getValue());
            }
            return this;
        }

        @CanIgnoreReturnValue
        public Builder<K, V> orderKeysBy(Comparator<? super K> keyComparator) {
            this.keyComparator = Preconditions.checkNotNull(keyComparator);
            return this;
        }

        @CanIgnoreReturnValue
        public Builder<K, V> orderValuesBy(Comparator<? super V> valueComparator) {
            this.valueComparator = Preconditions.checkNotNull(valueComparator);
            return this;
        }

        @CanIgnoreReturnValue
        Builder<K, V> combine(Builder<K, V> other) {
            if (other.builderMap != null) {
                for (Map.Entry<K, ImmutableCollection.Builder<V>> entry : other.builderMap.entrySet()) {
                    this.putAll(entry.getKey(), (Iterable<? extends V>)entry.getValue().build());
                }
            }
            return this;
        }

        public ImmutableMultimap<K, V> build() {
            if (this.builderMap == null) {
                return ImmutableListMultimap.of();
            }
            Collection<Map.Entry<K, ImmutableCollection.Builder<V>>> mapEntries = this.builderMap.entrySet();
            if (this.keyComparator != null) {
                mapEntries = Ordering.from(this.keyComparator).onKeys().immutableSortedCopy(mapEntries);
            }
            return ImmutableListMultimap.fromMapBuilderEntries(mapEntries, this.valueComparator);
        }
    }

    private static final class EntryCollection<K, V>
    extends ImmutableCollection<Map.Entry<K, V>> {
        @Weak
        final ImmutableMultimap<K, V> multimap;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        EntryCollection(ImmutableMultimap<K, V> multimap) {
            this.multimap = multimap;
        }

        @Override
        public UnmodifiableIterator<Map.Entry<K, V>> iterator() {
            return this.multimap.entryIterator();
        }

        @Override
        boolean isPartialView() {
            return this.multimap.isPartialView();
        }

        @Override
        public int size() {
            return this.multimap.size();
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
        @J2ktIncompatible
        @GwtIncompatible
        Object writeReplace() {
            return super.writeReplace();
        }
    }

    private final class Keys
    extends ImmutableMultiset<K> {
        private Keys() {
        }

        @Override
        public boolean contains(@Nullable Object object) {
            return ImmutableMultimap.this.containsKey(object);
        }

        @Override
        public int count(@Nullable Object element) {
            Collection values = ImmutableMultimap.this.map.get(element);
            return values == null ? 0 : values.size();
        }

        @Override
        public ImmutableSet<K> elementSet() {
            return ImmutableMultimap.this.keySet();
        }

        @Override
        public int size() {
            return ImmutableMultimap.this.size();
        }

        @Override
        Multiset.Entry<K> getEntry(int index) {
            Map.Entry entry = (Map.Entry)((ImmutableCollection)((Object)ImmutableMultimap.this.map.entrySet())).asList().get(index);
            return Multisets.immutableEntry(entry.getKey(), ((Collection)entry.getValue()).size());
        }

        @Override
        boolean isPartialView() {
            return true;
        }

        @Override
        @GwtIncompatible
        @J2ktIncompatible
        Object writeReplace() {
            return new KeysSerializedForm(ImmutableMultimap.this);
        }

        @GwtIncompatible
        @J2ktIncompatible
        private void readObject(ObjectInputStream stream) throws InvalidObjectException {
            throw new InvalidObjectException("Use KeysSerializedForm");
        }
    }

    private static final class Values<K, V>
    extends ImmutableCollection<V> {
        @Weak
        private final transient ImmutableMultimap<K, V> multimap;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        Values(ImmutableMultimap<K, V> multimap) {
            this.multimap = multimap;
        }

        @Override
        public boolean contains(@Nullable Object object) {
            return this.multimap.containsValue(object);
        }

        @Override
        public UnmodifiableIterator<V> iterator() {
            return this.multimap.valueIterator();
        }

        @Override
        @GwtIncompatible
        int copyIntoArray(@Nullable Object[] dst, int offset) {
            for (ImmutableCollection valueCollection : this.multimap.map.values()) {
                offset = valueCollection.copyIntoArray(dst, offset);
            }
            return offset;
        }

        @Override
        public int size() {
            return this.multimap.size();
        }

        @Override
        boolean isPartialView() {
            return true;
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
    private static final class KeysSerializedForm
    implements Serializable {
        final ImmutableMultimap<?, ?> multimap;

        KeysSerializedForm(ImmutableMultimap<?, ?> multimap) {
            this.multimap = multimap;
        }

        Object readResolve() {
            return this.multimap.keys();
        }
    }

    @GwtIncompatible
    @J2ktIncompatible
    static final class FieldSettersHolder {
        static final Serialization.FieldSetter<? super ImmutableMultimap<?, ?>> MAP_FIELD_SETTER = Serialization.getFieldSetter(ImmutableMultimap.class, "map");
        static final Serialization.FieldSetter<? super ImmutableMultimap<?, ?>> SIZE_FIELD_SETTER = Serialization.getFieldSetter(ImmutableMultimap.class, "size");

        private FieldSettersHolder() {
        }
    }
}

