/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.CollectPreconditions;
import com.google.common.collect.Hashing;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMapEntry;
import com.google.common.collect.ImmutableMapEntrySet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.IndexedImmutableSet;
import com.google.common.collect.JdkBackedImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class RegularImmutableMap<K, V>
extends ImmutableMap<K, V> {
    static final ImmutableMap<Object, Object> EMPTY = new RegularImmutableMap(ImmutableMap.EMPTY_ENTRY_ARRAY, null, 0);
    @VisibleForTesting
    static final double MAX_LOAD_FACTOR = 1.2;
    @VisibleForTesting
    static final double HASH_FLOODING_FPP = 0.001;
    static final int MAX_HASH_BUCKET_LENGTH = 8;
    @VisibleForTesting
    final transient Map.Entry<K, V>[] entries;
    private final transient @Nullable ImmutableMapEntry<K, V> @Nullable [] table;
    private final transient int mask;
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = 0L;

    static <K, V> ImmutableMap<K, V> fromEntries(Map.Entry<K, V> ... entries) {
        return RegularImmutableMap.fromEntryArray(entries.length, entries, true);
    }

    static <K, V> ImmutableMap<K, V> fromEntryArray(int n,  @Nullable Map.Entry<K, V>[] entryArray, boolean throwIfDuplicateKeys) {
        Preconditions.checkPositionIndex(n, entryArray.length);
        if (n == 0) {
            ImmutableMap<Object, Object> empty = EMPTY;
            return empty;
        }
        try {
            return RegularImmutableMap.fromEntryArrayCheckingBucketOverflow(n, entryArray, throwIfDuplicateKeys);
        }
        catch (BucketOverflowException e) {
            return JdkBackedImmutableMap.create(n, entryArray, throwIfDuplicateKeys);
        }
    }

    private static <K, V> ImmutableMap<K, V> fromEntryArrayCheckingBucketOverflow(int n,  @Nullable Map.Entry<K, V>[] entryArray, boolean throwIfDuplicateKeys) throws BucketOverflowException {
        int newTableSize;
        Map.Entry<K, V>[] entries = n == entryArray.length ? entryArray : ImmutableMapEntry.createEntryArray(n);
        int tableSize = Hashing.closedTableSize(n, 1.2);
        @Nullable ImmutableMapEntry<K, V>[] table = ImmutableMapEntry.createEntryArray(tableSize);
        int mask = tableSize - 1;
        IdentityHashMap<ImmutableMapEntry.NonTerminalImmutableMapEntry<K, V>, Boolean> duplicates = null;
        int dupCount = 0;
        for (int entryIndex = n - 1; entryIndex >= 0; --entryIndex) {
            Map.Entry<K, V> entry = Objects.requireNonNull(entryArray[entryIndex]);
            K key = entry.getKey();
            V value = entry.getValue();
            CollectPreconditions.checkEntryNotNull(key, value);
            int tableIndex = Hashing.smear(key.hashCode()) & mask;
            ImmutableMapEntry keyBucketHead = table[tableIndex];
            ImmutableMapEntry effectiveEntry = RegularImmutableMap.checkNoConflictInKeyBucket(key, value, keyBucketHead, throwIfDuplicateKeys);
            if (effectiveEntry == null) {
                effectiveEntry = keyBucketHead == null ? RegularImmutableMap.makeImmutable(entry, key, value) : new ImmutableMapEntry.NonTerminalImmutableMapEntry<K, V>(key, value, keyBucketHead);
                table[tableIndex] = effectiveEntry;
            } else {
                if (duplicates == null) {
                    duplicates = new IdentityHashMap<ImmutableMapEntry.NonTerminalImmutableMapEntry<K, V>, Boolean>();
                }
                duplicates.put((ImmutableMapEntry.NonTerminalImmutableMapEntry<K, V>)effectiveEntry, true);
                ++dupCount;
                if (entries == entryArray) {
                    Map.Entry<K, V>[] originalEntries = entries;
                    entries = (Map.Entry[])originalEntries.clone();
                }
            }
            entries[entryIndex] = effectiveEntry;
        }
        if (duplicates != null && (newTableSize = Hashing.closedTableSize((entries = RegularImmutableMap.removeDuplicates(entries, n, n - dupCount, duplicates)).length, 1.2)) != tableSize) {
            return RegularImmutableMap.fromEntryArrayCheckingBucketOverflow(entries.length, entries, true);
        }
        return new RegularImmutableMap<K, V>(entries, table, mask);
    }

    static <K, V> Map.Entry<K, V>[] removeDuplicates(Map.Entry<K, V>[] entries, int n, int newN, IdentityHashMap<Map.Entry<K, V>, Boolean> duplicates) {
        ImmutableMapEntry<K, V>[] newEntries = ImmutableMapEntry.createEntryArray(newN);
        int out = 0;
        for (int in = 0; in < n; ++in) {
            Map.Entry<K, V> entry = entries[in];
            Boolean status = duplicates.get(entry);
            if (status != null) {
                if (!status.booleanValue()) continue;
                duplicates.put(entry, false);
            }
            newEntries[out++] = entry;
        }
        return newEntries;
    }

    static <K, V> ImmutableMapEntry<K, V> makeImmutable(Map.Entry<K, V> entry, K key, V value) {
        boolean reusable = entry instanceof ImmutableMapEntry && ((ImmutableMapEntry)entry).isReusable();
        return reusable ? (ImmutableMapEntry)entry : new ImmutableMapEntry<K, V>(key, value);
    }

    static <K, V> ImmutableMapEntry<K, V> makeImmutable(Map.Entry<K, V> entry) {
        return RegularImmutableMap.makeImmutable(entry, entry.getKey(), entry.getValue());
    }

    private RegularImmutableMap(Map.Entry<K, V>[] entries, @Nullable ImmutableMapEntry<K, V> @Nullable [] table, int mask) {
        this.entries = entries;
        this.table = table;
        this.mask = mask;
    }

    @CanIgnoreReturnValue
    static <K, V> @Nullable ImmutableMapEntry<K, V> checkNoConflictInKeyBucket(Object key, Object newValue, @Nullable ImmutableMapEntry<K, V> keyBucketHead, boolean throwIfDuplicateKeys) throws BucketOverflowException {
        int bucketSize = 0;
        while (keyBucketHead != null) {
            if (keyBucketHead.getKey().equals(key)) {
                if (throwIfDuplicateKeys) {
                    RegularImmutableMap.checkNoConflict(false, "key", keyBucketHead, key + "=" + newValue);
                } else {
                    return keyBucketHead;
                }
            }
            if (++bucketSize > 8) {
                throw new BucketOverflowException();
            }
            keyBucketHead = keyBucketHead.getNextInKeyBucket();
        }
        return null;
    }

    @Override
    public @Nullable V get(@Nullable Object key) {
        return RegularImmutableMap.get(key, this.table, this.mask);
    }

    static <V> @Nullable V get(@Nullable Object key, @Nullable ImmutableMapEntry<?, V> @Nullable [] keyTable, int mask) {
        if (key == null || keyTable == null) {
            return null;
        }
        int index = Hashing.smear(key.hashCode()) & mask;
        for (ImmutableMapEntry<?, V> entry = keyTable[index]; entry != null; entry = entry.getNextInKeyBucket()) {
            Object candidateKey = entry.getKey();
            if (!key.equals(candidateKey)) continue;
            return entry.getValue();
        }
        return null;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Preconditions.checkNotNull(action);
        for (Map.Entry<K, V> entry : this.entries) {
            action.accept(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public int size() {
        return this.entries.length;
    }

    @Override
    boolean isPartialView() {
        return false;
    }

    @Override
    ImmutableSet<Map.Entry<K, V>> createEntrySet() {
        return new ImmutableMapEntrySet.RegularEntrySet<K, V>(this, this.entries);
    }

    @Override
    ImmutableSet<K> createKeySet() {
        return new KeySet(this);
    }

    @Override
    ImmutableCollection<V> createValues() {
        return new Values(this);
    }

    @Override
    @J2ktIncompatible
    @GwtIncompatible
    Object writeReplace() {
        return super.writeReplace();
    }

    static final class BucketOverflowException
    extends Exception {
        BucketOverflowException() {
        }
    }

    private static final class KeySet<K>
    extends IndexedImmutableSet<K> {
        private final RegularImmutableMap<K, ?> map;

        KeySet(RegularImmutableMap<K, ?> map) {
            this.map = map;
        }

        @Override
        K get(int index) {
            return this.map.entries[index].getKey();
        }

        @Override
        public boolean contains(@Nullable Object object) {
            return this.map.containsKey(object);
        }

        @Override
        boolean isPartialView() {
            return true;
        }

        @Override
        public int size() {
            return this.map.size();
        }

        @Override
        @J2ktIncompatible
        @GwtIncompatible
        Object writeReplace() {
            return super.writeReplace();
        }

        @GwtIncompatible
        @J2ktIncompatible
        private static final class SerializedForm<K>
        implements Serializable {
            final ImmutableMap<K, ?> map;
            @GwtIncompatible
            @J2ktIncompatible
            private static final long serialVersionUID = 0L;

            SerializedForm(ImmutableMap<K, ?> map) {
                this.map = map;
            }

            Object readResolve() {
                return this.map.keySet();
            }
        }
    }

    private static final class Values<K, V>
    extends ImmutableList<V> {
        final RegularImmutableMap<K, V> map;

        Values(RegularImmutableMap<K, V> map) {
            this.map = map;
        }

        @Override
        public V get(int index) {
            return this.map.entries[index].getValue();
        }

        @Override
        public int size() {
            return this.map.size();
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

        @GwtIncompatible
        @J2ktIncompatible
        private static final class SerializedForm<V>
        implements Serializable {
            final ImmutableMap<?, V> map;
            @GwtIncompatible
            @J2ktIncompatible
            private static final long serialVersionUID = 0L;

            SerializedForm(ImmutableMap<?, V> map) {
                this.map = map;
            }

            Object readResolve() {
                return this.map.values();
            }
        }
    }
}

