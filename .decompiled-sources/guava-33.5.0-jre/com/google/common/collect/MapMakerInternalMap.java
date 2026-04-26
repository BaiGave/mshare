/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Equivalence;
import com.google.common.base.Preconditions;
import com.google.common.collect.CollectPreconditions;
import com.google.common.collect.ForwardingConcurrentMap;
import com.google.common.collect.MapMaker;
import com.google.common.primitives.Ints;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.google.j2objc.annotations.Weak;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReentrantLock;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

@NullUnmarked
@J2ktIncompatible
@GwtIncompatible
final class MapMakerInternalMap<K, V, E extends InternalEntry<K, V, E>, S extends Segment<K, V, E, S>>
extends AbstractMap<K, V>
implements ConcurrentMap<K, V>,
Serializable {
    static final int MAXIMUM_CAPACITY = 0x40000000;
    static final int MAX_SEGMENTS = 65536;
    static final int CONTAINS_VALUE_RETRIES = 3;
    static final int DRAIN_THRESHOLD = 63;
    static final int DRAIN_MAX = 16;
    final transient int segmentMask;
    final transient int segmentShift;
    final transient Segment<K, V, E, S>[] segments;
    final int concurrencyLevel;
    final Equivalence<Object> keyEquivalence;
    final transient InternalEntryHelper<K, V, E, S> entryHelper;
    static final WeakValueReference<Object, Object, DummyInternalEntry> UNSET_WEAK_VALUE_REFERENCE = new WeakValueReference<Object, Object, DummyInternalEntry>(){

        @Override
        public @Nullable DummyInternalEntry getEntry() {
            return null;
        }

        @Override
        public void clear() {
        }

        @Override
        public @Nullable Object get() {
            return null;
        }

        @Override
        public WeakValueReference<Object, Object, DummyInternalEntry> copyFor(ReferenceQueue<Object> queue, DummyInternalEntry entry) {
            return this;
        }
    };
    @LazyInit
    transient @Nullable Set<K> keySet;
    @LazyInit
    transient @Nullable Collection<V> values;
    @LazyInit
    transient @Nullable Set<Map.Entry<K, V>> entrySet;
    private static final long serialVersionUID = 5L;

    private MapMakerInternalMap(MapMaker builder, InternalEntryHelper<K, V, E, S> entryHelper) {
        int segmentSize;
        int segmentCount;
        this.concurrencyLevel = Math.min(builder.getConcurrencyLevel(), 65536);
        this.keyEquivalence = builder.getKeyEquivalence();
        this.entryHelper = entryHelper;
        int initialCapacity = Math.min(builder.getInitialCapacity(), 0x40000000);
        int segmentShift = 0;
        for (segmentCount = 1; segmentCount < this.concurrencyLevel; segmentCount <<= 1) {
            ++segmentShift;
        }
        this.segmentShift = 32 - segmentShift;
        this.segmentMask = segmentCount - 1;
        this.segments = this.newSegmentArray(segmentCount);
        int segmentCapacity = initialCapacity / segmentCount;
        if (segmentCapacity * segmentCount < initialCapacity) {
            ++segmentCapacity;
        }
        for (segmentSize = 1; segmentSize < segmentCapacity; segmentSize <<= 1) {
        }
        for (int i = 0; i < this.segments.length; ++i) {
            this.segments[i] = this.createSegment(segmentSize);
        }
    }

    static <K, V> MapMakerInternalMap<K, V, ? extends InternalEntry<K, V, ?>, ?> create(MapMaker builder) {
        if (builder.getKeyStrength() == Strength.STRONG && builder.getValueStrength() == Strength.STRONG) {
            return new MapMakerInternalMap(builder, StrongKeyStrongValueEntry.Helper.instance());
        }
        if (builder.getKeyStrength() == Strength.STRONG && builder.getValueStrength() == Strength.WEAK) {
            return new MapMakerInternalMap(builder, StrongKeyWeakValueEntry.Helper.instance());
        }
        if (builder.getKeyStrength() == Strength.WEAK && builder.getValueStrength() == Strength.STRONG) {
            return new MapMakerInternalMap(builder, WeakKeyStrongValueEntry.Helper.instance());
        }
        if (builder.getKeyStrength() == Strength.WEAK && builder.getValueStrength() == Strength.WEAK) {
            return new MapMakerInternalMap(builder, WeakKeyWeakValueEntry.Helper.instance());
        }
        throw new AssertionError();
    }

    static <K> MapMakerInternalMap<K, MapMaker.Dummy, ? extends InternalEntry<K, MapMaker.Dummy, ?>, ?> createWithDummyValues(MapMaker builder) {
        if (builder.getKeyStrength() == Strength.STRONG && builder.getValueStrength() == Strength.STRONG) {
            return new MapMakerInternalMap(builder, StrongKeyDummyValueEntry.Helper.instance());
        }
        if (builder.getKeyStrength() == Strength.WEAK && builder.getValueStrength() == Strength.STRONG) {
            return new MapMakerInternalMap(builder, WeakKeyDummyValueEntry.Helper.instance());
        }
        if (builder.getValueStrength() == Strength.WEAK) {
            throw new IllegalArgumentException("Map cannot have both weak and dummy values");
        }
        throw new AssertionError();
    }

    static <K, V, E extends InternalEntry<K, V, E>> WeakValueReference<K, V, E> unsetWeakValueReference() {
        return UNSET_WEAK_VALUE_REFERENCE;
    }

    static int rehash(int h) {
        h += h << 15 ^ 0xFFFFCD7D;
        h ^= h >>> 10;
        h += h << 3;
        h ^= h >>> 6;
        h += (h << 2) + (h << 14);
        return h ^ h >>> 16;
    }

    @VisibleForTesting
    E copyEntry(E original, E newNext) {
        int hash = original.getHash();
        return this.segmentFor(hash).copyEntry(original, newNext);
    }

    int hash(Object key) {
        int h = this.keyEquivalence.hash(key);
        return MapMakerInternalMap.rehash(h);
    }

    void reclaimValue(WeakValueReference<K, V, E> valueReference) {
        E entry = valueReference.getEntry();
        int hash = entry.getHash();
        this.segmentFor(hash).reclaimValue(entry.getKey(), hash, valueReference);
    }

    void reclaimKey(E entry) {
        int hash = entry.getHash();
        this.segmentFor(hash).reclaimKey(entry, hash);
    }

    @VisibleForTesting
    boolean isLiveForTesting(InternalEntry<K, V, ?> entry) {
        return this.segmentFor(entry.getHash()).getLiveValueForTesting(entry) != null;
    }

    Segment<K, V, E, S> segmentFor(int hash) {
        return this.segments[hash >>> this.segmentShift & this.segmentMask];
    }

    Segment<K, V, E, S> createSegment(int initialCapacity) {
        return this.entryHelper.newSegment(this, initialCapacity);
    }

    @Nullable V getLiveValue(E entry) {
        if (entry.getKey() == null) {
            return null;
        }
        return entry.getValue();
    }

    final Segment<K, V, E, S>[] newSegmentArray(int ssize) {
        return new Segment[ssize];
    }

    @VisibleForTesting
    Strength keyStrength() {
        return this.entryHelper.keyStrength();
    }

    @VisibleForTesting
    Strength valueStrength() {
        return this.entryHelper.valueStrength();
    }

    @VisibleForTesting
    Equivalence<Object> valueEquivalence() {
        return this.entryHelper.valueStrength().defaultEquivalence();
    }

    @Override
    public boolean isEmpty() {
        int i;
        long sum = 0L;
        Segment<K, V, E, S>[] segments = this.segments;
        for (i = 0; i < segments.length; ++i) {
            if (segments[i].count != 0) {
                return false;
            }
            sum += (long)segments[i].modCount;
        }
        if (sum != 0L) {
            for (i = 0; i < segments.length; ++i) {
                if (segments[i].count != 0) {
                    return false;
                }
                sum -= (long)segments[i].modCount;
            }
            return sum == 0L;
        }
        return true;
    }

    @Override
    public int size() {
        Segment<K, V, E, S>[] segments = this.segments;
        long sum = 0L;
        for (int i = 0; i < segments.length; ++i) {
            sum += (long)segments[i].count;
        }
        return Ints.saturatedCast(sum);
    }

    @Override
    public @Nullable V get(@Nullable Object key) {
        if (key == null) {
            return null;
        }
        int hash = this.hash(key);
        return this.segmentFor(hash).get(key, hash);
    }

    @Nullable E getEntry(@Nullable Object key) {
        if (key == null) {
            return null;
        }
        int hash = this.hash(key);
        return this.segmentFor(hash).getEntry(key, hash);
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        if (key == null) {
            return false;
        }
        int hash = this.hash(key);
        return this.segmentFor(hash).containsKey(key, hash);
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        if (value == null) {
            return false;
        }
        Segment<K, V, E, S>[] segments = this.segments;
        long last = -1L;
        for (int i = 0; i < 3; ++i) {
            long sum = 0L;
            for (Segment<K, V, InternalEntry, S> segment : segments) {
                int unused = segment.count;
                AtomicReferenceArray table = segment.table;
                for (int j = 0; j < table.length(); ++j) {
                    for (InternalEntry e = (InternalEntry)table.get(j); e != null; e = e.getNext()) {
                        V v = segment.getLiveValue(e);
                        if (v == null || !this.valueEquivalence().equivalent(value, v)) continue;
                        return true;
                    }
                }
                sum += (long)segment.modCount;
            }
            if (sum == last) break;
            last = sum;
        }
        return false;
    }

    @Override
    @CanIgnoreReturnValue
    public @Nullable V put(K key, V value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(value);
        int hash = this.hash(key);
        return this.segmentFor(hash).put(key, hash, value, false);
    }

    @Override
    @CanIgnoreReturnValue
    public @Nullable V putIfAbsent(K key, V value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(value);
        int hash = this.hash(key);
        return this.segmentFor(hash).put(key, hash, value, true);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<K, V> e : m.entrySet()) {
            this.put(e.getKey(), e.getValue());
        }
    }

    @Override
    @CanIgnoreReturnValue
    public @Nullable V remove(@Nullable Object key) {
        if (key == null) {
            return null;
        }
        int hash = this.hash(key);
        return this.segmentFor(hash).remove(key, hash);
    }

    @Override
    @CanIgnoreReturnValue
    public boolean remove(@Nullable Object key, @Nullable Object value) {
        if (key == null || value == null) {
            return false;
        }
        int hash = this.hash(key);
        return this.segmentFor(hash).remove(key, hash, value);
    }

    @Override
    @CanIgnoreReturnValue
    public boolean replace(K key, @Nullable V oldValue, V newValue) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(newValue);
        if (oldValue == null) {
            return false;
        }
        int hash = this.hash(key);
        return this.segmentFor(hash).replace(key, hash, oldValue, newValue);
    }

    @Override
    @CanIgnoreReturnValue
    public @Nullable V replace(K key, V value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(value);
        int hash = this.hash(key);
        return this.segmentFor(hash).replace(key, hash, value);
    }

    @Override
    public void clear() {
        for (Segment<K, V, E, S> segment : this.segments) {
            segment.clear();
        }
    }

    @Override
    public Set<K> keySet() {
        KeySet ks = this.keySet;
        return ks != null ? ks : (this.keySet = new KeySet());
    }

    @Override
    public Collection<V> values() {
        Values vs = this.values;
        return vs != null ? vs : (this.values = new Values());
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        EntrySet es = this.entrySet;
        return es != null ? es : (this.entrySet = new EntrySet());
    }

    Object writeReplace() {
        return new SerializationProxy(this.entryHelper.keyStrength(), this.entryHelper.valueStrength(), this.keyEquivalence, this.entryHelper.valueStrength().defaultEquivalence(), this.concurrencyLevel, this);
    }

    @J2ktIncompatible
    private void readObject(ObjectInputStream in) throws InvalidObjectException {
        throw new InvalidObjectException("Use SerializationProxy");
    }

    static interface InternalEntryHelper<K, V, E extends InternalEntry<K, V, E>, S extends Segment<K, V, E, S>> {
        public Strength keyStrength();

        public Strength valueStrength();

        public S newSegment(MapMakerInternalMap<K, V, E, S> var1, int var2);

        public E newEntry(S var1, K var2, int var3, @Nullable E var4);

        public E copy(S var1, E var2, @Nullable E var3);

        public void setValue(S var1, E var2, V var3);
    }

    static abstract class Segment<K, V, E extends InternalEntry<K, V, E>, S extends Segment<K, V, E, S>>
    extends ReentrantLock {
        @Weak
        final MapMakerInternalMap<K, V, E, S> map;
        volatile int count;
        int modCount;
        int threshold;
        volatile @Nullable AtomicReferenceArray<E> table;
        final AtomicInteger readCount = new AtomicInteger();

        Segment(MapMakerInternalMap<K, V, E, S> map, int initialCapacity) {
            this.map = map;
            this.initTable(this.newEntryArray(initialCapacity));
        }

        abstract S self();

        @GuardedBy(value="this")
        void maybeDrainReferenceQueues() {
        }

        void maybeClearReferenceQueues() {
        }

        void setValue(E entry, V value) {
            this.map.entryHelper.setValue(this.self(), entry, value);
        }

        @Nullable E copyEntry(E original, E newNext) {
            return this.map.entryHelper.copy(this.self(), original, newNext);
        }

        AtomicReferenceArray<E> newEntryArray(int size) {
            return new AtomicReferenceArray(size);
        }

        void initTable(AtomicReferenceArray<E> newTable) {
            this.threshold = newTable.length() * 3 / 4;
            this.table = newTable;
        }

        abstract E castForTesting(InternalEntry<K, V, ?> var1);

        ReferenceQueue<K> getKeyReferenceQueueForTesting() {
            throw new AssertionError();
        }

        ReferenceQueue<V> getValueReferenceQueueForTesting() {
            throw new AssertionError();
        }

        WeakValueReference<K, V, E> getWeakValueReferenceForTesting(InternalEntry<K, V, ?> entry) {
            throw new AssertionError();
        }

        WeakValueReference<K, V, E> newWeakValueReferenceForTesting(InternalEntry<K, V, ?> entry, V value) {
            throw new AssertionError();
        }

        void setWeakValueReferenceForTesting(InternalEntry<K, V, ?> entry, WeakValueReference<K, V, ? extends InternalEntry<K, V, ?>> valueReference) {
            throw new AssertionError();
        }

        void setTableEntryForTesting(int i, InternalEntry<K, V, ?> entry) {
            this.table.set(i, this.castForTesting(entry));
        }

        E copyForTesting(InternalEntry<K, V, ?> entry, @Nullable InternalEntry<K, V, ?> newNext) {
            return this.map.entryHelper.copy(this.self(), this.castForTesting(entry), this.castForTesting(newNext));
        }

        void setValueForTesting(InternalEntry<K, V, ?> entry, V value) {
            this.map.entryHelper.setValue(this.self(), this.castForTesting(entry), value);
        }

        E newEntryForTesting(K key, int hash, @Nullable InternalEntry<K, V, ?> next) {
            return this.map.entryHelper.newEntry(this.self(), key, hash, this.castForTesting(next));
        }

        @CanIgnoreReturnValue
        boolean removeTableEntryForTesting(InternalEntry<K, V, ?> entry) {
            return this.removeEntryForTesting(this.castForTesting(entry));
        }

        @Nullable E removeFromChainForTesting(InternalEntry<K, V, ?> first, InternalEntry<K, V, ?> entry) {
            return this.removeFromChain(this.castForTesting(first), this.castForTesting(entry));
        }

        @Nullable V getLiveValueForTesting(InternalEntry<K, V, ?> entry) {
            return this.getLiveValue(this.castForTesting(entry));
        }

        void tryDrainReferenceQueues() {
            if (this.tryLock()) {
                try {
                    this.maybeDrainReferenceQueues();
                }
                finally {
                    this.unlock();
                }
            }
        }

        @GuardedBy(value="this")
        void drainKeyReferenceQueue(ReferenceQueue<K> keyReferenceQueue) {
            Reference<K> ref;
            int i = 0;
            while ((ref = keyReferenceQueue.poll()) != null) {
                InternalEntry entry = (InternalEntry)((Object)ref);
                this.map.reclaimKey(entry);
                if (++i != 16) continue;
                break;
            }
        }

        @GuardedBy(value="this")
        void drainValueReferenceQueue(ReferenceQueue<V> valueReferenceQueue) {
            Reference<V> ref;
            int i = 0;
            while ((ref = valueReferenceQueue.poll()) != null) {
                WeakValueReference valueReference = (WeakValueReference)((Object)ref);
                this.map.reclaimValue(valueReference);
                if (++i != 16) continue;
                break;
            }
        }

        <T> void clearReferenceQueue(ReferenceQueue<T> referenceQueue) {
            while (referenceQueue.poll() != null) {
            }
        }

        @Nullable E getFirst(int hash) {
            AtomicReferenceArray<E> table = this.table;
            return (E)((InternalEntry)table.get(hash & table.length() - 1));
        }

        @Nullable E getEntry(Object key, int hash) {
            if (this.count != 0) {
                for (E e = this.getFirst(hash); e != null; e = e.getNext()) {
                    if (e.getHash() != hash) continue;
                    Object entryKey = e.getKey();
                    if (entryKey == null) {
                        this.tryDrainReferenceQueues();
                        continue;
                    }
                    if (!this.map.keyEquivalence.equivalent(key, entryKey)) continue;
                    return e;
                }
            }
            return null;
        }

        @Nullable E getLiveEntry(Object key, int hash) {
            return this.getEntry(key, hash);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Nullable V get(Object key, int hash) {
            try {
                E e = this.getLiveEntry(key, hash);
                if (e == null) {
                    V v = null;
                    return v;
                }
                Object value = e.getValue();
                if (value == null) {
                    this.tryDrainReferenceQueues();
                }
                Object v = value;
                return v;
            }
            finally {
                this.postReadCleanup();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        boolean containsKey(Object key, int hash) {
            try {
                if (this.count != 0) {
                    E e = this.getLiveEntry(key, hash);
                    boolean bl = e != null && e.getValue() != null;
                    return bl;
                }
                boolean bl = false;
                return bl;
            }
            finally {
                this.postReadCleanup();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @VisibleForTesting
        boolean containsValue(Object value) {
            try {
                if (this.count != 0) {
                    AtomicReferenceArray<E> table = this.table;
                    int length = table.length();
                    for (int i = 0; i < length; ++i) {
                        for (InternalEntry e = (InternalEntry)table.get(i); e != null; e = e.getNext()) {
                            V entryValue = this.getLiveValue(e);
                            if (entryValue == null || !this.map.valueEquivalence().equivalent(value, entryValue)) continue;
                            boolean bl = true;
                            return bl;
                        }
                    }
                }
                boolean bl = false;
                return bl;
            }
            finally {
                this.postReadCleanup();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Nullable V put(K key, int hash, V value, boolean onlyIfAbsent) {
            this.lock();
            try {
                this.preWriteCleanup();
                int newCount = this.count + 1;
                if (newCount > this.threshold) {
                    this.expand();
                    newCount = this.count + 1;
                }
                AtomicReferenceArray<InternalEntry> table = this.table;
                int index = hash & table.length() - 1;
                InternalEntry first = (InternalEntry)table.get(index);
                for (InternalEntry e = first; e != null; e = e.getNext()) {
                    Object entryKey = e.getKey();
                    if (e.getHash() != hash || entryKey == null || !this.map.keyEquivalence.equivalent(key, entryKey)) continue;
                    Object entryValue = e.getValue();
                    if (entryValue == null) {
                        ++this.modCount;
                        this.setValue(e, value);
                        this.count = newCount = this.count;
                        V v = null;
                        return v;
                    }
                    if (onlyIfAbsent) {
                        Object v = entryValue;
                        return v;
                    }
                    ++this.modCount;
                    this.setValue(e, value);
                    Object v = entryValue;
                    return v;
                }
                ++this.modCount;
                InternalEntry newEntry = this.map.entryHelper.newEntry(this.self(), key, hash, first);
                this.setValue(newEntry, value);
                table.set(index, newEntry);
                this.count = newCount;
                V v = null;
                return v;
            }
            finally {
                this.unlock();
            }
        }

        @GuardedBy(value="this")
        void expand() {
            AtomicReferenceArray<E> oldTable = this.table;
            int oldCapacity = oldTable.length();
            if (oldCapacity >= 0x40000000) {
                return;
            }
            int newCount = this.count;
            AtomicReferenceArray<InternalEntry> newTable = this.newEntryArray(oldCapacity << 1);
            this.threshold = newTable.length() * 3 / 4;
            int newMask = newTable.length() - 1;
            for (int oldIndex = 0; oldIndex < oldCapacity; ++oldIndex) {
                int newIndex;
                Object e;
                InternalEntry head = (InternalEntry)oldTable.get(oldIndex);
                if (head == null) continue;
                Object next = head.getNext();
                int headIndex = head.getHash() & newMask;
                if (next == null) {
                    newTable.set(headIndex, head);
                    continue;
                }
                Object tail = head;
                int tailIndex = headIndex;
                for (e = next; e != null; e = e.getNext()) {
                    newIndex = e.getHash() & newMask;
                    if (newIndex == tailIndex) continue;
                    tailIndex = newIndex;
                    tail = e;
                }
                newTable.set(tailIndex, (InternalEntry)tail);
                for (e = head; e != tail; e = e.getNext()) {
                    newIndex = e.getHash() & newMask;
                    InternalEntry newNext = (InternalEntry)newTable.get(newIndex);
                    InternalEntry newFirst = this.copyEntry(e, newNext);
                    if (newFirst != null) {
                        newTable.set(newIndex, newFirst);
                        continue;
                    }
                    --newCount;
                }
            }
            this.table = newTable;
            this.count = newCount;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        boolean replace(K key, int hash, V oldValue, V newValue) {
            this.lock();
            try {
                this.preWriteCleanup();
                AtomicReferenceArray table = this.table;
                int index = hash & table.length() - 1;
                InternalEntry first = (InternalEntry)table.get(index);
                for (InternalEntry e = first; e != null; e = e.getNext()) {
                    Object entryKey = e.getKey();
                    if (e.getHash() != hash || entryKey == null || !this.map.keyEquivalence.equivalent(key, entryKey)) continue;
                    Object entryValue = e.getValue();
                    if (entryValue == null) {
                        if (Segment.isCollected(e)) {
                            int newCount = this.count - 1;
                            ++this.modCount;
                            InternalEntry newFirst = this.removeFromChain(first, e);
                            newCount = this.count - 1;
                            table.set(index, newFirst);
                            this.count = newCount;
                        }
                        boolean bl = false;
                        return bl;
                    }
                    if (this.map.valueEquivalence().equivalent(oldValue, entryValue)) {
                        ++this.modCount;
                        this.setValue(e, newValue);
                        boolean bl = true;
                        return bl;
                    }
                    boolean bl = false;
                    return bl;
                }
                boolean bl = false;
                return bl;
            }
            finally {
                this.unlock();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Nullable V replace(K key, int hash, V newValue) {
            this.lock();
            try {
                this.preWriteCleanup();
                AtomicReferenceArray table = this.table;
                int index = hash & table.length() - 1;
                InternalEntry first = (InternalEntry)table.get(index);
                for (InternalEntry e = first; e != null; e = e.getNext()) {
                    Object entryKey = e.getKey();
                    if (e.getHash() != hash || entryKey == null || !this.map.keyEquivalence.equivalent(key, entryKey)) continue;
                    Object entryValue = e.getValue();
                    if (entryValue == null) {
                        if (Segment.isCollected(e)) {
                            int newCount = this.count - 1;
                            ++this.modCount;
                            InternalEntry newFirst = this.removeFromChain(first, e);
                            newCount = this.count - 1;
                            table.set(index, newFirst);
                            this.count = newCount;
                        }
                        V v = null;
                        return v;
                    }
                    ++this.modCount;
                    this.setValue(e, newValue);
                    Object v = entryValue;
                    return v;
                }
                V v = null;
                return v;
            }
            finally {
                this.unlock();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @CanIgnoreReturnValue
        @Nullable V remove(Object key, int hash) {
            this.lock();
            try {
                this.preWriteCleanup();
                int newCount = this.count - 1;
                AtomicReferenceArray table = this.table;
                int index = hash & table.length() - 1;
                InternalEntry first = (InternalEntry)table.get(index);
                for (InternalEntry e = first; e != null; e = e.getNext()) {
                    Object entryKey = e.getKey();
                    if (e.getHash() != hash || entryKey == null || !this.map.keyEquivalence.equivalent(key, entryKey)) continue;
                    Object entryValue = e.getValue();
                    if (entryValue == null && !Segment.isCollected(e)) {
                        V v = null;
                        return v;
                    }
                    ++this.modCount;
                    InternalEntry newFirst = this.removeFromChain(first, e);
                    newCount = this.count - 1;
                    table.set(index, newFirst);
                    this.count = newCount;
                    Object v = entryValue;
                    return v;
                }
                V v = null;
                return v;
            }
            finally {
                this.unlock();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        boolean remove(Object key, int hash, Object value) {
            this.lock();
            try {
                this.preWriteCleanup();
                int newCount = this.count - 1;
                AtomicReferenceArray table = this.table;
                int index = hash & table.length() - 1;
                InternalEntry first = (InternalEntry)table.get(index);
                for (InternalEntry e = first; e != null; e = e.getNext()) {
                    Object entryKey = e.getKey();
                    if (e.getHash() != hash || entryKey == null || !this.map.keyEquivalence.equivalent(key, entryKey)) continue;
                    Object entryValue = e.getValue();
                    boolean explicitRemoval = false;
                    if (this.map.valueEquivalence().equivalent(value, entryValue)) {
                        explicitRemoval = true;
                    } else if (!Segment.isCollected(e)) {
                        boolean bl = false;
                        return bl;
                    }
                    ++this.modCount;
                    InternalEntry newFirst = this.removeFromChain(first, e);
                    newCount = this.count - 1;
                    table.set(index, newFirst);
                    this.count = newCount;
                    boolean bl = explicitRemoval;
                    return bl;
                }
                boolean bl = false;
                return bl;
            }
            finally {
                this.unlock();
            }
        }

        void clear() {
            if (this.count != 0) {
                this.lock();
                try {
                    AtomicReferenceArray<E> table = this.table;
                    for (int i = 0; i < table.length(); ++i) {
                        table.set(i, null);
                    }
                    this.maybeClearReferenceQueues();
                    this.readCount.set(0);
                    ++this.modCount;
                    this.count = 0;
                }
                finally {
                    this.unlock();
                }
            }
        }

        @GuardedBy(value="this")
        @Nullable E removeFromChain(E first, E entry) {
            int newCount = this.count;
            Object newFirst = entry.getNext();
            for (E e = first; e != entry; e = e.getNext()) {
                E next = this.copyEntry(e, newFirst);
                if (next != null) {
                    newFirst = next;
                    continue;
                }
                --newCount;
            }
            this.count = newCount;
            return newFirst;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @CanIgnoreReturnValue
        boolean reclaimKey(E entry, int hash) {
            this.lock();
            try {
                InternalEntry first;
                int newCount = this.count - 1;
                AtomicReferenceArray<InternalEntry> table = this.table;
                int index = hash & table.length() - 1;
                for (InternalEntry e = first = (InternalEntry)table.get(index); e != null; e = e.getNext()) {
                    if (e != entry) continue;
                    ++this.modCount;
                    InternalEntry newFirst = this.removeFromChain(first, e);
                    newCount = this.count - 1;
                    table.set(index, newFirst);
                    this.count = newCount;
                    boolean bl = true;
                    return bl;
                }
                boolean bl = false;
                return bl;
            }
            finally {
                this.unlock();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @CanIgnoreReturnValue
        boolean reclaimValue(K key, int hash, WeakValueReference<K, V, E> valueReference) {
            this.lock();
            try {
                InternalEntry first;
                int newCount = this.count - 1;
                AtomicReferenceArray<InternalEntry> table = this.table;
                int index = hash & table.length() - 1;
                for (InternalEntry e = first = (InternalEntry)table.get(index); e != null; e = e.getNext()) {
                    Object entryKey = e.getKey();
                    if (e.getHash() != hash || entryKey == null || !this.map.keyEquivalence.equivalent(key, entryKey)) continue;
                    WeakValueReference v = ((WeakValueEntry)e).getValueReference();
                    if (v == valueReference) {
                        ++this.modCount;
                        InternalEntry newFirst = this.removeFromChain(first, e);
                        newCount = this.count - 1;
                        table.set(index, newFirst);
                        this.count = newCount;
                        boolean bl = true;
                        return bl;
                    }
                    boolean bl = false;
                    return bl;
                }
                boolean bl = false;
                return bl;
            }
            finally {
                this.unlock();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @CanIgnoreReturnValue
        boolean clearValueForTesting(K key, int hash, WeakValueReference<K, V, ? extends InternalEntry<K, V, ?>> valueReference) {
            this.lock();
            try {
                InternalEntry first;
                AtomicReferenceArray<InternalEntry> table = this.table;
                int index = hash & table.length() - 1;
                for (InternalEntry e = first = (InternalEntry)table.get(index); e != null; e = e.getNext()) {
                    Object entryKey = e.getKey();
                    if (e.getHash() != hash || entryKey == null || !this.map.keyEquivalence.equivalent(key, entryKey)) continue;
                    WeakValueReference v = ((WeakValueEntry)e).getValueReference();
                    if (v == valueReference) {
                        InternalEntry newFirst = this.removeFromChain(first, e);
                        table.set(index, newFirst);
                        boolean bl = true;
                        return bl;
                    }
                    boolean bl = false;
                    return bl;
                }
                boolean bl = false;
                return bl;
            }
            finally {
                this.unlock();
            }
        }

        @GuardedBy(value="this")
        boolean removeEntryForTesting(E entry) {
            InternalEntry first;
            int hash = entry.getHash();
            int newCount = this.count - 1;
            AtomicReferenceArray<InternalEntry> table = this.table;
            int index = hash & table.length() - 1;
            for (InternalEntry e = first = (InternalEntry)table.get(index); e != null; e = e.getNext()) {
                if (e != entry) continue;
                ++this.modCount;
                InternalEntry newFirst = this.removeFromChain(first, e);
                newCount = this.count - 1;
                table.set(index, newFirst);
                this.count = newCount;
                return true;
            }
            return false;
        }

        static <K, V, E extends InternalEntry<K, V, E>> boolean isCollected(E entry) {
            return entry.getValue() == null;
        }

        @Nullable V getLiveValue(E entry) {
            if (entry.getKey() == null) {
                this.tryDrainReferenceQueues();
                return null;
            }
            Object value = entry.getValue();
            if (value == null) {
                this.tryDrainReferenceQueues();
                return null;
            }
            return value;
        }

        void postReadCleanup() {
            if ((this.readCount.incrementAndGet() & 0x3F) == 0) {
                this.runCleanup();
            }
        }

        @GuardedBy(value="this")
        void preWriteCleanup() {
            this.runLockedCleanup();
        }

        void runCleanup() {
            this.runLockedCleanup();
        }

        void runLockedCleanup() {
            if (this.tryLock()) {
                try {
                    this.maybeDrainReferenceQueues();
                    this.readCount.set(0);
                }
                finally {
                    this.unlock();
                }
            }
        }
    }

    static enum Strength {
        STRONG{

            @Override
            Equivalence<Object> defaultEquivalence() {
                return Equivalence.equals();
            }
        }
        ,
        WEAK{

            @Override
            Equivalence<Object> defaultEquivalence() {
                return Equivalence.identity();
            }
        };


        abstract Equivalence<Object> defaultEquivalence();
    }

    static class StrongKeyStrongValueEntry<K, V>
    extends AbstractStrongKeyEntry<K, V, StrongKeyStrongValueEntry<K, V>>
    implements StrongValueEntry<K, V, StrongKeyStrongValueEntry<K, V>> {
        private volatile @Nullable V value = null;

        private StrongKeyStrongValueEntry(K key, int hash) {
            super(key, hash);
        }

        @Override
        public final @Nullable V getValue() {
            return this.value;
        }

        static final class Helper<K, V>
        implements InternalEntryHelper<K, V, StrongKeyStrongValueEntry<K, V>, StrongKeyStrongValueSegment<K, V>> {
            private static final Helper<?, ?> INSTANCE = new Helper();

            Helper() {
            }

            static <K, V> Helper<K, V> instance() {
                return INSTANCE;
            }

            @Override
            public Strength keyStrength() {
                return Strength.STRONG;
            }

            @Override
            public Strength valueStrength() {
                return Strength.STRONG;
            }

            @Override
            public StrongKeyStrongValueSegment<K, V> newSegment(MapMakerInternalMap<K, V, StrongKeyStrongValueEntry<K, V>, StrongKeyStrongValueSegment<K, V>> map, int initialCapacity) {
                return new StrongKeyStrongValueSegment<K, V>(map, initialCapacity);
            }

            @Override
            public StrongKeyStrongValueEntry<K, V> copy(StrongKeyStrongValueSegment<K, V> segment, StrongKeyStrongValueEntry<K, V> entry, @Nullable StrongKeyStrongValueEntry<K, V> newNext) {
                StrongKeyStrongValueEntry<Object, V> newEntry = this.newEntry(segment, entry.key, entry.hash, newNext);
                ((StrongKeyStrongValueEntry)newEntry).value = ((StrongKeyStrongValueEntry)entry).value;
                return newEntry;
            }

            @Override
            public void setValue(StrongKeyStrongValueSegment<K, V> segment, StrongKeyStrongValueEntry<K, V> entry, V value) {
                ((StrongKeyStrongValueEntry)entry).value = value;
            }

            @Override
            public StrongKeyStrongValueEntry<K, V> newEntry(StrongKeyStrongValueSegment<K, V> segment, K key, int hash, @Nullable StrongKeyStrongValueEntry<K, V> next) {
                return next == null ? new StrongKeyStrongValueEntry(key, hash) : new LinkedStrongKeyStrongValueEntry<K, V>(key, hash, next);
            }
        }

        private static final class LinkedStrongKeyStrongValueEntry<K, V>
        extends StrongKeyStrongValueEntry<K, V> {
            private final StrongKeyStrongValueEntry<K, V> next;

            LinkedStrongKeyStrongValueEntry(K key, int hash, StrongKeyStrongValueEntry<K, V> next) {
                super(key, hash);
                this.next = next;
            }

            @Override
            public StrongKeyStrongValueEntry<K, V> getNext() {
                return this.next;
            }
        }
    }

    static class StrongKeyWeakValueEntry<K, V>
    extends AbstractStrongKeyEntry<K, V, StrongKeyWeakValueEntry<K, V>>
    implements WeakValueEntry<K, V, StrongKeyWeakValueEntry<K, V>> {
        private volatile WeakValueReference<K, V, StrongKeyWeakValueEntry<K, V>> valueReference = MapMakerInternalMap.unsetWeakValueReference();

        private StrongKeyWeakValueEntry(K key, int hash) {
            super(key, hash);
        }

        @Override
        public final @Nullable V getValue() {
            return this.valueReference.get();
        }

        @Override
        public final WeakValueReference<K, V, StrongKeyWeakValueEntry<K, V>> getValueReference() {
            return this.valueReference;
        }

        static final class Helper<K, V>
        implements InternalEntryHelper<K, V, StrongKeyWeakValueEntry<K, V>, StrongKeyWeakValueSegment<K, V>> {
            private static final Helper<?, ?> INSTANCE = new Helper();

            Helper() {
            }

            static <K, V> Helper<K, V> instance() {
                return INSTANCE;
            }

            @Override
            public Strength keyStrength() {
                return Strength.STRONG;
            }

            @Override
            public Strength valueStrength() {
                return Strength.WEAK;
            }

            @Override
            public StrongKeyWeakValueSegment<K, V> newSegment(MapMakerInternalMap<K, V, StrongKeyWeakValueEntry<K, V>, StrongKeyWeakValueSegment<K, V>> map, int initialCapacity) {
                return new StrongKeyWeakValueSegment<K, V>(map, initialCapacity);
            }

            @Override
            public @Nullable StrongKeyWeakValueEntry<K, V> copy(StrongKeyWeakValueSegment<K, V> segment, StrongKeyWeakValueEntry<K, V> entry, @Nullable StrongKeyWeakValueEntry<K, V> newNext) {
                if (Segment.isCollected(entry)) {
                    return null;
                }
                StrongKeyWeakValueEntry<Object, V> newEntry = this.newEntry(segment, entry.key, entry.hash, newNext);
                ((StrongKeyWeakValueEntry)newEntry).valueReference = ((StrongKeyWeakValueEntry)entry).valueReference.copyFor(((StrongKeyWeakValueSegment)segment).queueForValues, newEntry);
                return newEntry;
            }

            @Override
            public void setValue(StrongKeyWeakValueSegment<K, V> segment, StrongKeyWeakValueEntry<K, V> entry, V value) {
                WeakValueReference previous = ((StrongKeyWeakValueEntry)entry).valueReference;
                ((StrongKeyWeakValueEntry)entry).valueReference = new WeakValueReferenceImpl(((StrongKeyWeakValueSegment)segment).queueForValues, value, entry);
                previous.clear();
            }

            @Override
            public StrongKeyWeakValueEntry<K, V> newEntry(StrongKeyWeakValueSegment<K, V> segment, K key, int hash, @Nullable StrongKeyWeakValueEntry<K, V> next) {
                return next == null ? new StrongKeyWeakValueEntry(key, hash) : new LinkedStrongKeyWeakValueEntry<K, V>(key, hash, next);
            }
        }

        private static final class LinkedStrongKeyWeakValueEntry<K, V>
        extends StrongKeyWeakValueEntry<K, V> {
            private final StrongKeyWeakValueEntry<K, V> next;

            LinkedStrongKeyWeakValueEntry(K key, int hash, StrongKeyWeakValueEntry<K, V> next) {
                super(key, hash);
                this.next = next;
            }

            @Override
            public StrongKeyWeakValueEntry<K, V> getNext() {
                return this.next;
            }
        }
    }

    static class WeakKeyStrongValueEntry<K, V>
    extends AbstractWeakKeyEntry<K, V, WeakKeyStrongValueEntry<K, V>>
    implements StrongValueEntry<K, V, WeakKeyStrongValueEntry<K, V>> {
        private volatile @Nullable V value = null;

        private WeakKeyStrongValueEntry(ReferenceQueue<K> queue, K key, int hash) {
            super(queue, key, hash);
        }

        @Override
        public final @Nullable V getValue() {
            return this.value;
        }

        static final class Helper<K, V>
        implements InternalEntryHelper<K, V, WeakKeyStrongValueEntry<K, V>, WeakKeyStrongValueSegment<K, V>> {
            private static final Helper<?, ?> INSTANCE = new Helper();

            Helper() {
            }

            static <K, V> Helper<K, V> instance() {
                return INSTANCE;
            }

            @Override
            public Strength keyStrength() {
                return Strength.WEAK;
            }

            @Override
            public Strength valueStrength() {
                return Strength.STRONG;
            }

            @Override
            public WeakKeyStrongValueSegment<K, V> newSegment(MapMakerInternalMap<K, V, WeakKeyStrongValueEntry<K, V>, WeakKeyStrongValueSegment<K, V>> map, int initialCapacity) {
                return new WeakKeyStrongValueSegment<K, V>(map, initialCapacity);
            }

            @Override
            public @Nullable WeakKeyStrongValueEntry<K, V> copy(WeakKeyStrongValueSegment<K, V> segment, WeakKeyStrongValueEntry<K, V> entry, @Nullable WeakKeyStrongValueEntry<K, V> newNext) {
                Object key = entry.getKey();
                if (key == null) {
                    return null;
                }
                WeakKeyStrongValueEntry<K, V> newEntry = this.newEntry(segment, key, entry.hash, newNext);
                ((WeakKeyStrongValueEntry)newEntry).value = ((WeakKeyStrongValueEntry)entry).value;
                return newEntry;
            }

            @Override
            public void setValue(WeakKeyStrongValueSegment<K, V> segment, WeakKeyStrongValueEntry<K, V> entry, V value) {
                ((WeakKeyStrongValueEntry)entry).value = value;
            }

            @Override
            public WeakKeyStrongValueEntry<K, V> newEntry(WeakKeyStrongValueSegment<K, V> segment, K key, int hash, @Nullable WeakKeyStrongValueEntry<K, V> next) {
                return next == null ? new WeakKeyStrongValueEntry(((WeakKeyStrongValueSegment)segment).queueForKeys, key, hash) : new LinkedWeakKeyStrongValueEntry(((WeakKeyStrongValueSegment)segment).queueForKeys, key, hash, next);
            }
        }

        private static final class LinkedWeakKeyStrongValueEntry<K, V>
        extends WeakKeyStrongValueEntry<K, V> {
            private final WeakKeyStrongValueEntry<K, V> next;

            private LinkedWeakKeyStrongValueEntry(ReferenceQueue<K> queue, K key, int hash, WeakKeyStrongValueEntry<K, V> next) {
                super(queue, key, hash);
                this.next = next;
            }

            @Override
            public WeakKeyStrongValueEntry<K, V> getNext() {
                return this.next;
            }
        }
    }

    private static class WeakKeyWeakValueEntry<K, V>
    extends AbstractWeakKeyEntry<K, V, WeakKeyWeakValueEntry<K, V>>
    implements WeakValueEntry<K, V, WeakKeyWeakValueEntry<K, V>> {
        private volatile WeakValueReference<K, V, WeakKeyWeakValueEntry<K, V>> valueReference = MapMakerInternalMap.unsetWeakValueReference();

        WeakKeyWeakValueEntry(ReferenceQueue<K> queue, K key, int hash) {
            super(queue, key, hash);
        }

        @Override
        public final V getValue() {
            return this.valueReference.get();
        }

        @Override
        public final WeakValueReference<K, V, WeakKeyWeakValueEntry<K, V>> getValueReference() {
            return this.valueReference;
        }

        static final class Helper<K, V>
        implements InternalEntryHelper<K, V, WeakKeyWeakValueEntry<K, V>, WeakKeyWeakValueSegment<K, V>> {
            private static final Helper<?, ?> INSTANCE = new Helper();

            Helper() {
            }

            static <K, V> Helper<K, V> instance() {
                return INSTANCE;
            }

            @Override
            public Strength keyStrength() {
                return Strength.WEAK;
            }

            @Override
            public Strength valueStrength() {
                return Strength.WEAK;
            }

            @Override
            public WeakKeyWeakValueSegment<K, V> newSegment(MapMakerInternalMap<K, V, WeakKeyWeakValueEntry<K, V>, WeakKeyWeakValueSegment<K, V>> map, int initialCapacity) {
                return new WeakKeyWeakValueSegment<K, V>(map, initialCapacity);
            }

            @Override
            public @Nullable WeakKeyWeakValueEntry<K, V> copy(WeakKeyWeakValueSegment<K, V> segment, WeakKeyWeakValueEntry<K, V> entry, @Nullable WeakKeyWeakValueEntry<K, V> newNext) {
                Object key = entry.getKey();
                if (key == null) {
                    return null;
                }
                if (Segment.isCollected(entry)) {
                    return null;
                }
                WeakKeyWeakValueEntry<K, V> newEntry = this.newEntry(segment, key, entry.hash, newNext);
                ((WeakKeyWeakValueEntry)newEntry).valueReference = ((WeakKeyWeakValueEntry)entry).valueReference.copyFor(((WeakKeyWeakValueSegment)segment).queueForValues, newEntry);
                return newEntry;
            }

            @Override
            public void setValue(WeakKeyWeakValueSegment<K, V> segment, WeakKeyWeakValueEntry<K, V> entry, V value) {
                WeakValueReference previous = ((WeakKeyWeakValueEntry)entry).valueReference;
                ((WeakKeyWeakValueEntry)entry).valueReference = new WeakValueReferenceImpl(((WeakKeyWeakValueSegment)segment).queueForValues, value, entry);
                previous.clear();
            }

            @Override
            public WeakKeyWeakValueEntry<K, V> newEntry(WeakKeyWeakValueSegment<K, V> segment, K key, int hash, @Nullable WeakKeyWeakValueEntry<K, V> next) {
                return next == null ? new WeakKeyWeakValueEntry(((WeakKeyWeakValueSegment)segment).queueForKeys, key, hash) : new LinkedWeakKeyWeakValueEntry<K, V>(((WeakKeyWeakValueSegment)segment).queueForKeys, key, hash, next);
            }
        }

        private static final class LinkedWeakKeyWeakValueEntry<K, V>
        extends WeakKeyWeakValueEntry<K, V> {
            private final WeakKeyWeakValueEntry<K, V> next;

            LinkedWeakKeyWeakValueEntry(ReferenceQueue<K> queue, K key, int hash, WeakKeyWeakValueEntry<K, V> next) {
                super(queue, key, hash);
                this.next = next;
            }

            @Override
            public WeakKeyWeakValueEntry<K, V> getNext() {
                return this.next;
            }
        }
    }

    private static class StrongKeyDummyValueEntry<K>
    extends AbstractStrongKeyEntry<K, MapMaker.Dummy, StrongKeyDummyValueEntry<K>>
    implements StrongValueEntry<K, MapMaker.Dummy, StrongKeyDummyValueEntry<K>> {
        private StrongKeyDummyValueEntry(K key, int hash) {
            super(key, hash);
        }

        @Override
        public final MapMaker.Dummy getValue() {
            return MapMaker.Dummy.VALUE;
        }

        static final class Helper<K>
        implements InternalEntryHelper<K, MapMaker.Dummy, StrongKeyDummyValueEntry<K>, StrongKeyDummyValueSegment<K>> {
            private static final Helper<?> INSTANCE = new Helper();

            Helper() {
            }

            static <K> Helper<K> instance() {
                return INSTANCE;
            }

            @Override
            public Strength keyStrength() {
                return Strength.STRONG;
            }

            @Override
            public Strength valueStrength() {
                return Strength.STRONG;
            }

            @Override
            public StrongKeyDummyValueSegment<K> newSegment(MapMakerInternalMap<K, MapMaker.Dummy, StrongKeyDummyValueEntry<K>, StrongKeyDummyValueSegment<K>> map, int initialCapacity) {
                return new StrongKeyDummyValueSegment<K>(map, initialCapacity);
            }

            @Override
            public StrongKeyDummyValueEntry<K> copy(StrongKeyDummyValueSegment<K> segment, StrongKeyDummyValueEntry<K> entry, @Nullable StrongKeyDummyValueEntry<K> newNext) {
                return this.newEntry(segment, entry.key, entry.hash, newNext);
            }

            @Override
            public void setValue(StrongKeyDummyValueSegment<K> segment, StrongKeyDummyValueEntry<K> entry, MapMaker.Dummy value) {
            }

            @Override
            public StrongKeyDummyValueEntry<K> newEntry(StrongKeyDummyValueSegment<K> segment, K key, int hash, @Nullable StrongKeyDummyValueEntry<K> next) {
                return next == null ? new StrongKeyDummyValueEntry(key, hash) : new LinkedStrongKeyDummyValueEntry<K>(key, hash, next);
            }
        }

        private static final class LinkedStrongKeyDummyValueEntry<K>
        extends StrongKeyDummyValueEntry<K> {
            private final StrongKeyDummyValueEntry<K> next;

            LinkedStrongKeyDummyValueEntry(K key, int hash, StrongKeyDummyValueEntry<K> next) {
                super(key, hash);
                this.next = next;
            }

            @Override
            public StrongKeyDummyValueEntry<K> getNext() {
                return this.next;
            }
        }
    }

    private static class WeakKeyDummyValueEntry<K>
    extends AbstractWeakKeyEntry<K, MapMaker.Dummy, WeakKeyDummyValueEntry<K>>
    implements StrongValueEntry<K, MapMaker.Dummy, WeakKeyDummyValueEntry<K>> {
        private WeakKeyDummyValueEntry(ReferenceQueue<K> queue, K key, int hash) {
            super(queue, key, hash);
        }

        @Override
        public final MapMaker.Dummy getValue() {
            return MapMaker.Dummy.VALUE;
        }

        static final class Helper<K>
        implements InternalEntryHelper<K, MapMaker.Dummy, WeakKeyDummyValueEntry<K>, WeakKeyDummyValueSegment<K>> {
            private static final Helper<?> INSTANCE = new Helper();

            Helper() {
            }

            static <K> Helper<K> instance() {
                return INSTANCE;
            }

            @Override
            public Strength keyStrength() {
                return Strength.WEAK;
            }

            @Override
            public Strength valueStrength() {
                return Strength.STRONG;
            }

            @Override
            public WeakKeyDummyValueSegment<K> newSegment(MapMakerInternalMap<K, MapMaker.Dummy, WeakKeyDummyValueEntry<K>, WeakKeyDummyValueSegment<K>> map, int initialCapacity) {
                return new WeakKeyDummyValueSegment<K>(map, initialCapacity);
            }

            @Override
            public @Nullable WeakKeyDummyValueEntry<K> copy(WeakKeyDummyValueSegment<K> segment, WeakKeyDummyValueEntry<K> entry, @Nullable WeakKeyDummyValueEntry<K> newNext) {
                Object key = entry.getKey();
                if (key == null) {
                    return null;
                }
                return this.newEntry(segment, key, entry.hash, newNext);
            }

            @Override
            public void setValue(WeakKeyDummyValueSegment<K> segment, WeakKeyDummyValueEntry<K> entry, MapMaker.Dummy value) {
            }

            @Override
            public WeakKeyDummyValueEntry<K> newEntry(WeakKeyDummyValueSegment<K> segment, K key, int hash, @Nullable WeakKeyDummyValueEntry<K> next) {
                return next == null ? new WeakKeyDummyValueEntry(((WeakKeyDummyValueSegment)segment).queueForKeys, key, hash) : new LinkedWeakKeyDummyValueEntry(((WeakKeyDummyValueSegment)segment).queueForKeys, key, hash, next);
            }
        }

        private static final class LinkedWeakKeyDummyValueEntry<K>
        extends WeakKeyDummyValueEntry<K> {
            private final WeakKeyDummyValueEntry<K> next;

            private LinkedWeakKeyDummyValueEntry(ReferenceQueue<K> queue, K key, int hash, WeakKeyDummyValueEntry<K> next) {
                super(queue, key, hash);
                this.next = next;
            }

            @Override
            public WeakKeyDummyValueEntry<K> getNext() {
                return this.next;
            }
        }
    }

    static interface WeakValueReference<K, V, E extends InternalEntry<K, V, E>> {
        public @Nullable V get();

        public E getEntry();

        public void clear();

        public WeakValueReference<K, V, E> copyFor(ReferenceQueue<V> var1, E var2);
    }

    static interface InternalEntry<K, V, E extends InternalEntry<K, V, E>> {
        public E getNext();

        public int getHash();

        public K getKey();

        public V getValue();
    }

    final class KeySet
    extends AbstractSet<K> {
        KeySet() {
        }

        @Override
        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        @Override
        public int size() {
            return MapMakerInternalMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return MapMakerInternalMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return MapMakerInternalMap.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            return MapMakerInternalMap.this.remove(o) != null;
        }

        @Override
        public void clear() {
            MapMakerInternalMap.this.clear();
        }
    }

    final class Values
    extends AbstractCollection<V> {
        Values() {
        }

        @Override
        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        @Override
        public int size() {
            return MapMakerInternalMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return MapMakerInternalMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return MapMakerInternalMap.this.containsValue(o);
        }

        @Override
        public void clear() {
            MapMakerInternalMap.this.clear();
        }
    }

    final class EntrySet
    extends AbstractSet<Map.Entry<K, V>> {
        EntrySet() {
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            Object key = e.getKey();
            if (key == null) {
                return false;
            }
            Object v = MapMakerInternalMap.this.get(key);
            return v != null && MapMakerInternalMap.this.valueEquivalence().equivalent(e.getValue(), v);
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            Object key = e.getKey();
            return key != null && MapMakerInternalMap.this.remove(key, e.getValue());
        }

        @Override
        public int size() {
            return MapMakerInternalMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return MapMakerInternalMap.this.isEmpty();
        }

        @Override
        public void clear() {
            MapMakerInternalMap.this.clear();
        }
    }

    private static final class SerializationProxy<K, V>
    extends AbstractSerializationProxy<K, V> {
        private static final long serialVersionUID = 3L;

        SerializationProxy(Strength keyStrength, Strength valueStrength, Equivalence<Object> keyEquivalence, Equivalence<Object> valueEquivalence, int concurrencyLevel, ConcurrentMap<K, V> delegate) {
            super(keyStrength, valueStrength, keyEquivalence, valueEquivalence, concurrencyLevel, delegate);
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            this.writeMapTo(out);
        }

        @J2ktIncompatible
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            MapMaker mapMaker = this.readMapMaker(in);
            this.delegate = mapMaker.makeMap();
            this.readEntries(in);
        }

        private Object readResolve() {
            return this.delegate;
        }
    }

    static abstract class AbstractSerializationProxy<K, V>
    extends ForwardingConcurrentMap<K, V>
    implements Serializable {
        private static final long serialVersionUID = 3L;
        final Strength keyStrength;
        final Strength valueStrength;
        final Equivalence<Object> keyEquivalence;
        final Equivalence<Object> valueEquivalence;
        final int concurrencyLevel;
        transient ConcurrentMap<K, V> delegate;

        AbstractSerializationProxy(Strength keyStrength, Strength valueStrength, Equivalence<Object> keyEquivalence, Equivalence<Object> valueEquivalence, int concurrencyLevel, ConcurrentMap<K, V> delegate) {
            this.keyStrength = keyStrength;
            this.valueStrength = valueStrength;
            this.keyEquivalence = keyEquivalence;
            this.valueEquivalence = valueEquivalence;
            this.concurrencyLevel = concurrencyLevel;
            this.delegate = delegate;
        }

        @Override
        protected ConcurrentMap<K, V> delegate() {
            return this.delegate;
        }

        void writeMapTo(ObjectOutputStream out) throws IOException {
            out.writeInt(this.delegate.size());
            for (Map.Entry entry : this.delegate.entrySet()) {
                out.writeObject(entry.getKey());
                out.writeObject(entry.getValue());
            }
            out.writeObject(null);
        }

        @J2ktIncompatible
        MapMaker readMapMaker(ObjectInputStream in) throws IOException {
            int size = in.readInt();
            return new MapMaker().initialCapacity(size).setKeyStrength(this.keyStrength).setValueStrength(this.valueStrength).keyEquivalence(this.keyEquivalence).concurrencyLevel(this.concurrencyLevel);
        }

        @J2ktIncompatible
        void readEntries(ObjectInputStream in) throws IOException, ClassNotFoundException {
            Object key;
            while ((key = in.readObject()) != null) {
                Object value = in.readObject();
                this.delegate.put(key, value);
            }
        }
    }

    final class EntryIterator
    extends HashIterator<Map.Entry<K, V>> {
        EntryIterator() {
        }

        @Override
        public Map.Entry<K, V> next() {
            return this.nextEntry();
        }
    }

    final class WriteThroughEntry
    extends AbstractMap.SimpleEntry<K, V> {
        WriteThroughEntry(K key, V value) {
            super(key, value);
        }

        @Override
        public V setValue(V newValue) {
            MapMakerInternalMap.this.put(this.getKey(), newValue);
            return super.setValue(newValue);
        }
    }

    final class ValueIterator
    extends HashIterator<V> {
        ValueIterator() {
        }

        @Override
        public V next() {
            return this.nextEntry().getValue();
        }
    }

    final class KeyIterator
    extends HashIterator<K> {
        KeyIterator() {
        }

        @Override
        public K next() {
            return this.nextEntry().getKey();
        }
    }

    abstract class HashIterator<T>
    implements Iterator<T> {
        int nextSegmentIndex;
        int nextTableIndex;
        @Nullable Segment<K, V, E, S> currentSegment;
        @Nullable AtomicReferenceArray<E> currentTable;
        @Nullable E nextEntry;
        @Nullable WriteThroughEntry nextExternal;
        @Nullable WriteThroughEntry lastReturned;

        HashIterator() {
            this.nextSegmentIndex = MapMakerInternalMap.this.segments.length - 1;
            this.nextTableIndex = -1;
            this.advance();
        }

        @Override
        public abstract T next();

        final void advance() {
            this.nextExternal = null;
            if (this.nextInChain()) {
                return;
            }
            if (this.nextInTable()) {
                return;
            }
            while (this.nextSegmentIndex >= 0) {
                this.currentSegment = MapMakerInternalMap.this.segments[this.nextSegmentIndex--];
                if (this.currentSegment.count == 0) continue;
                this.currentTable = this.currentSegment.table;
                this.nextTableIndex = this.currentTable.length() - 1;
                if (!this.nextInTable()) continue;
                return;
            }
        }

        boolean nextInChain() {
            if (this.nextEntry != null) {
                this.nextEntry = this.nextEntry.getNext();
                while (this.nextEntry != null) {
                    if (this.advanceTo(this.nextEntry)) {
                        return true;
                    }
                    this.nextEntry = this.nextEntry.getNext();
                }
            }
            return false;
        }

        boolean nextInTable() {
            while (this.nextTableIndex >= 0) {
                if ((this.nextEntry = (InternalEntry)this.currentTable.get(this.nextTableIndex--)) == null || !this.advanceTo(this.nextEntry) && !this.nextInChain()) continue;
                return true;
            }
            return false;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        boolean advanceTo(E entry) {
            try {
                Object key = entry.getKey();
                Object value = MapMakerInternalMap.this.getLiveValue(entry);
                if (value != null) {
                    this.nextExternal = new WriteThroughEntry(key, value);
                    boolean bl = true;
                    return bl;
                }
                boolean bl = false;
                return bl;
            }
            finally {
                this.currentSegment.postReadCleanup();
            }
        }

        @Override
        public boolean hasNext() {
            return this.nextExternal != null;
        }

        WriteThroughEntry nextEntry() {
            if (this.nextExternal == null) {
                throw new NoSuchElementException();
            }
            this.lastReturned = this.nextExternal;
            this.advance();
            return this.lastReturned;
        }

        @Override
        public void remove() {
            CollectPreconditions.checkRemove(this.lastReturned != null);
            MapMakerInternalMap.this.remove(this.lastReturned.getKey());
            this.lastReturned = null;
        }
    }

    static final class CleanupMapTask
    implements Runnable {
        final WeakReference<MapMakerInternalMap<?, ?, ?, ?>> mapReference;

        public CleanupMapTask(MapMakerInternalMap<?, ?, ?, ?> map) {
            this.mapReference = new WeakReference(map);
        }

        @Override
        public void run() {
            MapMakerInternalMap map = (MapMakerInternalMap)this.mapReference.get();
            if (map == null) {
                throw new CancellationException();
            }
            for (Segment segment : map.segments) {
                segment.runCleanup();
            }
        }
    }

    static final class WeakKeyDummyValueSegment<K>
    extends Segment<K, MapMaker.Dummy, WeakKeyDummyValueEntry<K>, WeakKeyDummyValueSegment<K>> {
        private final ReferenceQueue<K> queueForKeys = new ReferenceQueue();

        WeakKeyDummyValueSegment(MapMakerInternalMap<K, MapMaker.Dummy, WeakKeyDummyValueEntry<K>, WeakKeyDummyValueSegment<K>> map, int initialCapacity) {
            super(map, initialCapacity);
        }

        @Override
        WeakKeyDummyValueSegment<K> self() {
            return this;
        }

        @Override
        ReferenceQueue<K> getKeyReferenceQueueForTesting() {
            return this.queueForKeys;
        }

        @Override
        public WeakKeyDummyValueEntry<K> castForTesting(InternalEntry<K, MapMaker.Dummy, ?> entry) {
            return (WeakKeyDummyValueEntry)entry;
        }

        @Override
        void maybeDrainReferenceQueues() {
            this.drainKeyReferenceQueue(this.queueForKeys);
        }

        @Override
        void maybeClearReferenceQueues() {
            this.clearReferenceQueue(this.queueForKeys);
        }
    }

    static final class WeakKeyWeakValueSegment<K, V>
    extends Segment<K, V, WeakKeyWeakValueEntry<K, V>, WeakKeyWeakValueSegment<K, V>> {
        private final ReferenceQueue<K> queueForKeys = new ReferenceQueue();
        private final ReferenceQueue<V> queueForValues = new ReferenceQueue();

        WeakKeyWeakValueSegment(MapMakerInternalMap<K, V, WeakKeyWeakValueEntry<K, V>, WeakKeyWeakValueSegment<K, V>> map, int initialCapacity) {
            super(map, initialCapacity);
        }

        @Override
        WeakKeyWeakValueSegment<K, V> self() {
            return this;
        }

        @Override
        ReferenceQueue<K> getKeyReferenceQueueForTesting() {
            return this.queueForKeys;
        }

        @Override
        ReferenceQueue<V> getValueReferenceQueueForTesting() {
            return this.queueForValues;
        }

        @Override
        public @Nullable WeakKeyWeakValueEntry<K, V> castForTesting(@Nullable InternalEntry<K, V, ?> entry) {
            return (WeakKeyWeakValueEntry)entry;
        }

        @Override
        public WeakValueReference<K, V, WeakKeyWeakValueEntry<K, V>> getWeakValueReferenceForTesting(InternalEntry<K, V, ?> e) {
            return ((WeakKeyWeakValueEntry)this.castForTesting((InternalEntry)e)).getValueReference();
        }

        @Override
        public WeakValueReference<K, V, WeakKeyWeakValueEntry<K, V>> newWeakValueReferenceForTesting(InternalEntry<K, V, ?> e, V value) {
            return new WeakValueReferenceImpl(this.queueForValues, value, this.castForTesting((InternalEntry)e));
        }

        @Override
        public void setWeakValueReferenceForTesting(InternalEntry<K, V, ?> e, WeakValueReference<K, V, ? extends InternalEntry<K, V, ?>> valueReference) {
            InternalEntry entry = this.castForTesting((InternalEntry)e);
            WeakValueReference<K, V, ? extends InternalEntry<K, V, ?>> newValueReference = valueReference;
            WeakValueReference previous = ((WeakKeyWeakValueEntry)entry).valueReference;
            ((WeakKeyWeakValueEntry)entry).valueReference = newValueReference;
            previous.clear();
        }

        @Override
        void maybeDrainReferenceQueues() {
            this.drainKeyReferenceQueue(this.queueForKeys);
            this.drainValueReferenceQueue(this.queueForValues);
        }

        @Override
        void maybeClearReferenceQueues() {
            this.clearReferenceQueue(this.queueForKeys);
        }
    }

    static final class WeakKeyStrongValueSegment<K, V>
    extends Segment<K, V, WeakKeyStrongValueEntry<K, V>, WeakKeyStrongValueSegment<K, V>> {
        private final ReferenceQueue<K> queueForKeys = new ReferenceQueue();

        WeakKeyStrongValueSegment(MapMakerInternalMap<K, V, WeakKeyStrongValueEntry<K, V>, WeakKeyStrongValueSegment<K, V>> map, int initialCapacity) {
            super(map, initialCapacity);
        }

        @Override
        WeakKeyStrongValueSegment<K, V> self() {
            return this;
        }

        @Override
        ReferenceQueue<K> getKeyReferenceQueueForTesting() {
            return this.queueForKeys;
        }

        @Override
        public WeakKeyStrongValueEntry<K, V> castForTesting(InternalEntry<K, V, ?> entry) {
            return (WeakKeyStrongValueEntry)entry;
        }

        @Override
        void maybeDrainReferenceQueues() {
            this.drainKeyReferenceQueue(this.queueForKeys);
        }

        @Override
        void maybeClearReferenceQueues() {
            this.clearReferenceQueue(this.queueForKeys);
        }
    }

    static final class StrongKeyDummyValueSegment<K>
    extends Segment<K, MapMaker.Dummy, StrongKeyDummyValueEntry<K>, StrongKeyDummyValueSegment<K>> {
        StrongKeyDummyValueSegment(MapMakerInternalMap<K, MapMaker.Dummy, StrongKeyDummyValueEntry<K>, StrongKeyDummyValueSegment<K>> map, int initialCapacity) {
            super(map, initialCapacity);
        }

        @Override
        StrongKeyDummyValueSegment<K> self() {
            return this;
        }

        @Override
        public StrongKeyDummyValueEntry<K> castForTesting(InternalEntry<K, MapMaker.Dummy, ?> entry) {
            return (StrongKeyDummyValueEntry)entry;
        }
    }

    static final class StrongKeyWeakValueSegment<K, V>
    extends Segment<K, V, StrongKeyWeakValueEntry<K, V>, StrongKeyWeakValueSegment<K, V>> {
        private final ReferenceQueue<V> queueForValues = new ReferenceQueue();

        StrongKeyWeakValueSegment(MapMakerInternalMap<K, V, StrongKeyWeakValueEntry<K, V>, StrongKeyWeakValueSegment<K, V>> map, int initialCapacity) {
            super(map, initialCapacity);
        }

        @Override
        StrongKeyWeakValueSegment<K, V> self() {
            return this;
        }

        @Override
        ReferenceQueue<V> getValueReferenceQueueForTesting() {
            return this.queueForValues;
        }

        @Override
        public @Nullable StrongKeyWeakValueEntry<K, V> castForTesting(@Nullable InternalEntry<K, V, ?> entry) {
            return (StrongKeyWeakValueEntry)entry;
        }

        @Override
        public WeakValueReference<K, V, StrongKeyWeakValueEntry<K, V>> getWeakValueReferenceForTesting(InternalEntry<K, V, ?> e) {
            return ((StrongKeyWeakValueEntry)this.castForTesting((InternalEntry)e)).getValueReference();
        }

        @Override
        public WeakValueReference<K, V, StrongKeyWeakValueEntry<K, V>> newWeakValueReferenceForTesting(InternalEntry<K, V, ?> e, V value) {
            return new WeakValueReferenceImpl(this.queueForValues, value, this.castForTesting((InternalEntry)e));
        }

        @Override
        public void setWeakValueReferenceForTesting(InternalEntry<K, V, ?> e, WeakValueReference<K, V, ? extends InternalEntry<K, V, ?>> valueReference) {
            InternalEntry entry = this.castForTesting((InternalEntry)e);
            WeakValueReference<K, V, ? extends InternalEntry<K, V, ?>> newValueReference = valueReference;
            WeakValueReference previous = ((StrongKeyWeakValueEntry)entry).valueReference;
            ((StrongKeyWeakValueEntry)entry).valueReference = newValueReference;
            previous.clear();
        }

        @Override
        void maybeDrainReferenceQueues() {
            this.drainValueReferenceQueue(this.queueForValues);
        }

        @Override
        void maybeClearReferenceQueues() {
            this.clearReferenceQueue(this.queueForValues);
        }
    }

    static final class StrongKeyStrongValueSegment<K, V>
    extends Segment<K, V, StrongKeyStrongValueEntry<K, V>, StrongKeyStrongValueSegment<K, V>> {
        StrongKeyStrongValueSegment(MapMakerInternalMap<K, V, StrongKeyStrongValueEntry<K, V>, StrongKeyStrongValueSegment<K, V>> map, int initialCapacity) {
            super(map, initialCapacity);
        }

        @Override
        StrongKeyStrongValueSegment<K, V> self() {
            return this;
        }

        @Override
        public @Nullable StrongKeyStrongValueEntry<K, V> castForTesting(@Nullable InternalEntry<K, V, ?> entry) {
            return (StrongKeyStrongValueEntry)entry;
        }
    }

    static final class WeakValueReferenceImpl<K, V, E extends InternalEntry<K, V, E>>
    extends WeakReference<V>
    implements WeakValueReference<K, V, E> {
        @Weak
        final E entry;

        WeakValueReferenceImpl(ReferenceQueue<V> queue, V referent, E entry) {
            super(referent, queue);
            this.entry = entry;
        }

        @Override
        public E getEntry() {
            return this.entry;
        }

        @Override
        public WeakValueReference<K, V, E> copyFor(ReferenceQueue<V> queue, E entry) {
            return new WeakValueReferenceImpl<K, V, E>(queue, this.get(), entry);
        }
    }

    static final class DummyInternalEntry
    implements InternalEntry<Object, Object, DummyInternalEntry> {
        private DummyInternalEntry() {
            throw new AssertionError();
        }

        @Override
        public DummyInternalEntry getNext() {
            throw new AssertionError();
        }

        @Override
        public int getHash() {
            throw new AssertionError();
        }

        @Override
        public Object getKey() {
            throw new AssertionError();
        }

        @Override
        public Object getValue() {
            throw new AssertionError();
        }
    }

    static abstract class AbstractWeakKeyEntry<K, V, E extends InternalEntry<K, V, E>>
    extends WeakReference<K>
    implements InternalEntry<K, V, E> {
        final int hash;

        AbstractWeakKeyEntry(ReferenceQueue<K> queue, K key, int hash) {
            super(key, queue);
            this.hash = hash;
        }

        @Override
        public final K getKey() {
            return (K)this.get();
        }

        @Override
        public final int getHash() {
            return this.hash;
        }

        @Override
        public @Nullable E getNext() {
            return null;
        }
    }

    static interface WeakValueEntry<K, V, E extends InternalEntry<K, V, E>>
    extends InternalEntry<K, V, E> {
        public WeakValueReference<K, V, E> getValueReference();
    }

    static interface StrongValueEntry<K, V, E extends InternalEntry<K, V, E>>
    extends InternalEntry<K, V, E> {
    }

    static abstract class AbstractStrongKeyEntry<K, V, E extends InternalEntry<K, V, E>>
    implements InternalEntry<K, V, E> {
        final K key;
        final int hash;

        AbstractStrongKeyEntry(K key, int hash) {
            this.key = key;
            this.hash = hash;
        }

        @Override
        public final K getKey() {
            return this.key;
        }

        @Override
        public final int getHash() {
            return this.hash;
        }

        @Override
        public @Nullable E getNext() {
            return null;
        }
    }
}

