/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractSetMultimap;
import com.google.common.collect.CollectPreconditions;
import com.google.common.collect.CollectSpliterators;
import com.google.common.collect.Hashing;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.ParametricNullness;
import com.google.common.collect.Platform;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class LinkedHashMultimap<K, V>
extends AbstractSetMultimap<K, V> {
    private static final int DEFAULT_KEY_CAPACITY = 16;
    private static final int DEFAULT_VALUE_SET_CAPACITY = 2;
    @VisibleForTesting
    static final double VALUE_SET_LOAD_FACTOR = 1.0;
    @VisibleForTesting
    transient int valueSetCapacity;
    private transient MultimapIterationChain<K, V> multimapIterationChain = new MultimapIterationChain();
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = 1L;

    public static <K, V> LinkedHashMultimap<K, V> create() {
        return new LinkedHashMultimap<K, V>(16, 2);
    }

    public static <K, V> LinkedHashMultimap<K, V> create(int expectedKeys, int expectedValuesPerKey) {
        return new LinkedHashMultimap<K, V>(Maps.capacity(expectedKeys), Maps.capacity(expectedValuesPerKey));
    }

    public static <K, V> LinkedHashMultimap<K, V> create(Multimap<? extends K, ? extends V> multimap) {
        LinkedHashMultimap<K, V> result = LinkedHashMultimap.create(multimap.keySet().size(), 2);
        result.putAll((Multimap)multimap);
        return result;
    }

    private LinkedHashMultimap(int keyCapacity, int valueSetCapacity) {
        super(Platform.newLinkedHashMapWithExpectedSize(keyCapacity));
        CollectPreconditions.checkNonnegative(valueSetCapacity, "expectedValuesPerKey");
        this.valueSetCapacity = valueSetCapacity;
    }

    @Override
    Set<V> createCollection() {
        return Platform.newLinkedHashSetWithExpectedSize(this.valueSetCapacity);
    }

    @Override
    Collection<V> createCollection(@ParametricNullness K key) {
        return new ValueSet(key, this.valueSetCapacity);
    }

    @Override
    @CanIgnoreReturnValue
    public Set<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values) {
        return super.replaceValues((Object)key, (Iterable)values);
    }

    @Override
    public Set<Map.Entry<K, V>> entries() {
        return super.entries();
    }

    @Override
    public Set<K> keySet() {
        return super.keySet();
    }

    @Override
    public Collection<V> values() {
        return super.values();
    }

    @Override
    Iterator<Map.Entry<K, V>> entryIterator() {
        return new Iterator<Map.Entry<K, V>>(){
            @Nullable ValueEntry<K, V> nextEntry;
            @Nullable ValueEntry<K, V> toRemove;
            {
                this.nextEntry = ((LinkedHashMultimap)LinkedHashMultimap.this).multimapIterationChain.firstEntry;
            }

            @Override
            public boolean hasNext() {
                return this.nextEntry != null;
            }

            @Override
            public Map.Entry<K, V> next() {
                ValueEntry entry = this.nextEntry;
                if (entry == null) {
                    throw new NoSuchElementException();
                }
                this.toRemove = entry;
                this.nextEntry = entry.successorInMultimap;
                return entry;
            }

            @Override
            public void remove() {
                Preconditions.checkState(this.toRemove != null, "no calls to next() since the last call to remove()");
                LinkedHashMultimap.this.remove(this.toRemove.getKey(), this.toRemove.getValue());
                this.toRemove = null;
            }
        };
    }

    @Override
    Spliterator<Map.Entry<K, V>> entrySpliterator() {
        return Spliterators.spliterator(this.entries(), 17);
    }

    @Override
    Iterator<V> valueIterator() {
        return Maps.valueIterator(this.entryIterator());
    }

    @Override
    Spliterator<V> valueSpliterator() {
        return CollectSpliterators.map(this.entrySpliterator(), Map.Entry::getValue);
    }

    @GwtIncompatible
    @J2ktIncompatible
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(this.keySet().size());
        for (K key : this.keySet()) {
            stream.writeObject(key);
        }
        stream.writeInt(this.size());
        for (Map.Entry entry : this.entries()) {
            stream.writeObject(entry.getKey());
            stream.writeObject(entry.getValue());
        }
    }

    @GwtIncompatible
    @J2ktIncompatible
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.multimapIterationChain = new MultimapIterationChain();
        this.valueSetCapacity = 2;
        int distinctKeys = stream.readInt();
        Map<Object, Collection<V>> map = Platform.newLinkedHashMapWithExpectedSize(12);
        for (int i = 0; i < distinctKeys; ++i) {
            Object key = stream.readObject();
            map.put(key, this.createCollection(key));
        }
        int entries = stream.readInt();
        for (int i = 0; i < entries; ++i) {
            Object key = stream.readObject();
            Object value = stream.readObject();
            Objects.requireNonNull((Collection)map.get(key)).add(value);
        }
        this.setMap(map);
    }

    private static final class MultimapIterationChain<K, V> {
        @Nullable ValueEntry<K, V> firstEntry;
        @Nullable ValueEntry<K, V> lastEntry;

        private MultimapIterationChain() {
        }

        void succeeds(@Nullable ValueEntry<K, V> pred, @Nullable ValueEntry<K, V> succ) {
            if (pred == null) {
                this.firstEntry = succ;
            } else {
                ((ValueEntry)pred).successorInMultimap = (ValueEntry)succ;
            }
            if (succ == null) {
                this.lastEntry = pred;
            } else {
                ((ValueEntry)succ).predecessorInMultimap = (ValueEntry)pred;
            }
        }

        void delete(ValueEntry<K, V> entry) {
            this.succeeds(((ValueEntry)entry).predecessorInMultimap, ((ValueEntry)entry).successorInMultimap);
        }

        void append(ValueEntry<K, V> newEntry) {
            this.succeeds(this.lastEntry, newEntry);
            this.lastEntry = newEntry;
        }
    }

    @VisibleForTesting
    final class ValueSet
    extends Sets.ImprovedAbstractSet<V> {
        @ParametricNullness
        private final K key;
        @VisibleForTesting
        @Nullable ValueEntry<K, V>[] hashTable;
        private int size = 0;
        private int modCount = 0;
        private @Nullable ValueEntry<K, V> firstEntry;
        private @Nullable ValueEntry<K, V> lastEntry;

        ValueSet(K key, int expectedValues) {
            this.key = key;
            int tableSize = Hashing.closedTableSize(expectedValues, 1.0);
            @Nullable ValueEntry[] hashTable = new ValueEntry[tableSize];
            this.hashTable = hashTable;
        }

        private void succeedsInValueSet(@Nullable ValueEntry<K, V> pred, @Nullable ValueEntry<K, V> succ) {
            if (pred == null) {
                this.firstEntry = succ;
            } else {
                pred.successorInValueSet = succ;
            }
            if (succ == null) {
                this.lastEntry = pred;
            } else {
                succ.predecessorInValueSet = pred;
            }
        }

        private void deleteFromValueSet(ValueEntry<K, V> entry) {
            this.succeedsInValueSet(entry.predecessorInValueSet, entry.successorInValueSet);
        }

        private void appendToValueSet(ValueEntry<K, V> newEntry) {
            this.succeedsInValueSet(this.lastEntry, newEntry);
            this.lastEntry = newEntry;
        }

        private int mask() {
            return this.hashTable.length - 1;
        }

        @Override
        public Iterator<V> iterator() {
            return new Iterator<V>(){
                @Nullable ValueEntry<K, V> nextEntry;
                @Nullable ValueEntry<K, V> toRemove;
                int expectedModCount;
                {
                    this.nextEntry = ValueSet.this.firstEntry;
                    this.expectedModCount = ValueSet.this.modCount;
                }

                private void checkForComodification() {
                    if (ValueSet.this.modCount != this.expectedModCount) {
                        throw new ConcurrentModificationException();
                    }
                }

                @Override
                public boolean hasNext() {
                    this.checkForComodification();
                    return this.nextEntry != null;
                }

                @Override
                @ParametricNullness
                public V next() {
                    this.checkForComodification();
                    ValueEntry entry = this.nextEntry;
                    if (entry == null) {
                        throw new NoSuchElementException();
                    }
                    Object result = entry.getValue();
                    this.toRemove = entry;
                    this.nextEntry = entry.successorInValueSet;
                    return result;
                }

                @Override
                public void remove() {
                    this.checkForComodification();
                    Preconditions.checkState(this.toRemove != null, "no calls to next() since the last call to remove()");
                    ValueSet.this.remove(this.toRemove.getValue());
                    this.expectedModCount = ValueSet.this.modCount;
                    this.toRemove = null;
                }
            };
        }

        @Override
        public void forEach(Consumer<? super V> action) {
            Preconditions.checkNotNull(action);
            ValueEntry entry = this.firstEntry;
            while (entry != null) {
                action.accept(entry.getValue());
                entry = entry.successorInValueSet;
            }
        }

        @Override
        public int size() {
            return this.size;
        }

        @Override
        public boolean contains(@Nullable Object o) {
            int smearedHash = Hashing.smearedHash(o);
            ValueEntry entry = this.hashTable[smearedHash & this.mask()];
            while (entry != null) {
                if (entry.matchesValue(o, smearedHash)) {
                    return true;
                }
                entry = entry.nextInValueBucket;
            }
            return false;
        }

        @Override
        public boolean add(@ParametricNullness V value) {
            ValueEntry rowHead;
            int smearedHash = Hashing.smearedHash(value);
            int bucket = smearedHash & this.mask();
            ValueEntry entry = rowHead = this.hashTable[bucket];
            while (entry != null) {
                if (entry.matchesValue(value, smearedHash)) {
                    return false;
                }
                entry = entry.nextInValueBucket;
            }
            ValueEntry newEntry = new ValueEntry(this.key, value, smearedHash, rowHead);
            this.appendToValueSet(newEntry);
            LinkedHashMultimap.this.multimapIterationChain.append(newEntry);
            this.hashTable[bucket] = newEntry;
            ++this.size;
            ++this.modCount;
            this.rehashIfNecessary();
            return true;
        }

        private void rehashIfNecessary() {
            if (Hashing.needsResizing(this.size, this.hashTable.length, 1.0)) {
                ValueEntry[] hashTable = new ValueEntry[this.hashTable.length * 2];
                this.hashTable = hashTable;
                int mask = hashTable.length - 1;
                ValueEntry entry = this.firstEntry;
                while (entry != null) {
                    int bucket = entry.smearedValueHash & mask;
                    entry.nextInValueBucket = hashTable[bucket];
                    hashTable[bucket] = entry;
                    entry = entry.successorInValueSet;
                }
            }
        }

        @Override
        @CanIgnoreReturnValue
        public boolean remove(@Nullable Object o) {
            int smearedHash = Hashing.smearedHash(o);
            int bucket = smearedHash & this.mask();
            ValueEntry prev = null;
            ValueEntry entry = this.hashTable[bucket];
            while (entry != null) {
                if (entry.matchesValue(o, smearedHash)) {
                    if (prev == null) {
                        this.hashTable[bucket] = entry.nextInValueBucket;
                    } else {
                        prev.nextInValueBucket = entry.nextInValueBucket;
                    }
                    this.deleteFromValueSet(entry);
                    LinkedHashMultimap.this.multimapIterationChain.delete(entry);
                    --this.size;
                    ++this.modCount;
                    return true;
                }
                prev = entry;
                entry = entry.nextInValueBucket;
            }
            return false;
        }

        @Override
        public void clear() {
            Arrays.fill(this.hashTable, null);
            this.size = 0;
            ValueEntry entry = this.firstEntry;
            while (entry != null) {
                LinkedHashMultimap.this.multimapIterationChain.delete(entry);
                entry = entry.successorInValueSet;
            }
            this.firstEntry = null;
            this.lastEntry = null;
            ++this.modCount;
        }
    }

    @VisibleForTesting
    static final class ValueEntry<K, V>
    extends AbstractMap.SimpleImmutableEntry<K, V> {
        final int smearedValueHash;
        @Nullable ValueEntry<K, V> nextInValueBucket;
        private @Nullable ValueEntry<K, V> predecessorInValueSet;
        private @Nullable ValueEntry<K, V> successorInValueSet;
        private @Nullable ValueEntry<K, V> predecessorInMultimap;
        private @Nullable ValueEntry<K, V> successorInMultimap;

        ValueEntry(@ParametricNullness K key, @ParametricNullness V value, int smearedValueHash, @Nullable ValueEntry<K, V> nextInValueBucket) {
            super(key, value);
            this.smearedValueHash = smearedValueHash;
            this.nextInValueBucket = nextInValueBucket;
        }

        boolean matchesValue(@Nullable Object v, int smearedVHash) {
            return this.smearedValueHash == smearedVHash && Objects.equals(this.getValue(), v);
        }
    }
}

