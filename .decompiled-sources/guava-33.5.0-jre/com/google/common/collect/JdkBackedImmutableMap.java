/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMapEntry;
import com.google.common.collect.ImmutableMapEntrySet;
import com.google.common.collect.ImmutableMapKeySet;
import com.google.common.collect.ImmutableMapValues;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.RegularImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

@GwtIncompatible
final class JdkBackedImmutableMap<K, V>
extends ImmutableMap<K, V> {
    private final transient Map<K, V> delegateMap;
    private final transient ImmutableList<Map.Entry<K, V>> entries;

    static <K, V> ImmutableMap<K, V> create(int n, @Nullable Map.Entry<K, V>[] entryArray, boolean throwIfDuplicateKeys) {
        HashMap<K, V> delegateMap = Maps.newHashMapWithExpectedSize(n);
        HashMap<K, Object> duplicates = null;
        int dupCount = 0;
        for (int i = 0; i < n; ++i) {
            V value;
            entryArray[i] = RegularImmutableMap.makeImmutable(Objects.requireNonNull(entryArray[i]));
            K key = entryArray[i].getKey();
            V oldValue = delegateMap.put(key, value = entryArray[i].getValue());
            if (oldValue == null) continue;
            if (throwIfDuplicateKeys) {
                throw JdkBackedImmutableMap.conflictException("key", entryArray[i], entryArray[i].getKey() + "=" + oldValue);
            }
            if (duplicates == null) {
                duplicates = new HashMap<K, Object>();
            }
            duplicates.put(key, value);
            ++dupCount;
        }
        if (duplicates != null) {
            Map.Entry[] newEntryArray = new Map.Entry[n - dupCount];
            int outI = 0;
            for (int inI = 0; inI < n; ++inI) {
                Map.Entry<K, V> entry = Objects.requireNonNull(entryArray[inI]);
                K key = entry.getKey();
                if (duplicates.containsKey(key)) {
                    Object value = duplicates.get(key);
                    if (value == null) continue;
                    entry = new ImmutableMapEntry(key, value);
                    duplicates.put(key, null);
                }
                newEntryArray[outI++] = entry;
            }
            entryArray = newEntryArray;
        }
        return new JdkBackedImmutableMap(delegateMap, ImmutableList.asImmutableList(entryArray, n));
    }

    JdkBackedImmutableMap(Map<K, V> delegateMap, ImmutableList<Map.Entry<K, V>> entries) {
        this.delegateMap = delegateMap;
        this.entries = entries;
    }

    @Override
    public int size() {
        return this.entries.size();
    }

    @Override
    public @Nullable V get(@Nullable Object key) {
        return this.delegateMap.get(key);
    }

    @Override
    ImmutableSet<Map.Entry<K, V>> createEntrySet() {
        return new ImmutableMapEntrySet.RegularEntrySet<K, V>(this, this.entries);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Preconditions.checkNotNull(action);
        this.entries.forEach((Consumer<Map.Entry<K, V>>)((Consumer<Map.Entry>)e -> action.accept((Object)e.getKey(), (Object)e.getValue())));
    }

    @Override
    ImmutableSet<K> createKeySet() {
        return new ImmutableMapKeySet(this);
    }

    @Override
    ImmutableCollection<V> createValues() {
        return new ImmutableMapValues(this);
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

