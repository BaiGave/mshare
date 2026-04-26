/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMapEntry;
import com.google.common.collect.ImmutableMapEntrySet;
import com.google.common.collect.ImmutableMapKeySet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.RegularImmutableMap;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.google.j2objc.annotations.RetainedWith;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@GwtIncompatible
final class JdkBackedImmutableBiMap<K, V>
extends ImmutableBiMap<K, V> {
    private final transient ImmutableList<Map.Entry<K, V>> entries;
    private final Map<K, V> forwardDelegate;
    private final Map<V, K> backwardDelegate;
    private final @Nullable JdkBackedImmutableBiMap<V, K> inverse;
    @LazyInit
    @RetainedWith
    private transient @Nullable JdkBackedImmutableBiMap<V, K> lazyInverse;

    static <K, V> ImmutableBiMap<K, V> create(int n, @Nullable Map.Entry<K, V>[] entryArray) {
        HashMap forwardDelegate = Maps.newHashMapWithExpectedSize(n);
        HashMap backwardDelegate = Maps.newHashMapWithExpectedSize(n);
        for (int i = 0; i < n; ++i) {
            ImmutableMapEntry<K, V> e = RegularImmutableMap.makeImmutable(Objects.requireNonNull(entryArray[i]));
            entryArray[i] = e;
            Object oldValue = forwardDelegate.putIfAbsent(e.getKey(), e.getValue());
            if (oldValue != null) {
                throw JdkBackedImmutableBiMap.conflictException("key", e.getKey() + "=" + oldValue, entryArray[i]);
            }
            Object oldKey = backwardDelegate.putIfAbsent(e.getValue(), e.getKey());
            if (oldKey == null) continue;
            throw JdkBackedImmutableBiMap.conflictException("value", oldKey + "=" + e.getValue(), entryArray[i]);
        }
        ImmutableList<Map.Entry<K, V>> entryList = ImmutableList.asImmutableList(entryArray, n);
        return new JdkBackedImmutableBiMap<K, V>(entryList, forwardDelegate, backwardDelegate, null);
    }

    private JdkBackedImmutableBiMap(ImmutableList<Map.Entry<K, V>> entries, Map<K, V> forwardDelegate, Map<V, K> backwardDelegate, @Nullable JdkBackedImmutableBiMap<V, K> inverse) {
        this.entries = entries;
        this.forwardDelegate = forwardDelegate;
        this.backwardDelegate = backwardDelegate;
        this.inverse = inverse;
    }

    @Override
    public int size() {
        return this.entries.size();
    }

    @Override
    public ImmutableBiMap<V, K> inverse() {
        return this.inverse != null ? this.inverse : this.lazyInverse();
    }

    private ImmutableBiMap<V, K> lazyInverse() {
        JdkBackedImmutableBiMap<K, V> result = this.lazyInverse;
        return result == null ? (this.lazyInverse = new JdkBackedImmutableBiMap<K, V>(new InverseEntries<K, V>(this.entries), this.backwardDelegate, this.forwardDelegate, this)) : result;
    }

    @Override
    public @Nullable V get(@Nullable Object key) {
        return this.forwardDelegate.get(key);
    }

    @Override
    ImmutableSet<Map.Entry<K, V>> createEntrySet() {
        return new ImmutableMapEntrySet.RegularEntrySet<K, V>(this, this.entries);
    }

    @Override
    ImmutableSet<K> createKeySet() {
        return new ImmutableMapKeySet(this);
    }

    @Override
    boolean isPartialView() {
        return false;
    }

    @Override
    @J2ktIncompatible
    Object writeReplace() {
        return super.writeReplace();
    }

    private static final class InverseEntries<K, V>
    extends ImmutableList<Map.Entry<V, K>> {
        private final ImmutableList<Map.Entry<K, V>> entries;

        InverseEntries(ImmutableList<Map.Entry<K, V>> entries) {
            this.entries = entries;
        }

        @Override
        public Map.Entry<V, K> get(int index) {
            Map.Entry entry = (Map.Entry)this.entries.get(index);
            return Maps.immutableEntry(entry.getValue(), entry.getKey());
        }

        @Override
        boolean isPartialView() {
            return false;
        }

        @Override
        public int size() {
            return this.entries.size();
        }

        @Override
        @J2ktIncompatible
        Object writeReplace() {
            return super.writeReplace();
        }
    }
}

