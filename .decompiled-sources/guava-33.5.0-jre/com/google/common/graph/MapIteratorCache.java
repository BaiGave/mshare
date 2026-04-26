/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.graph;

import com.google.common.base.Preconditions;
import com.google.common.collect.UnmodifiableIterator;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;

class MapIteratorCache<K, V> {
    private final Map<K, V> backingMap;
    private volatile transient @Nullable Map.Entry<K, V> cacheEntry;

    MapIteratorCache(Map<K, V> backingMap) {
        this.backingMap = Preconditions.checkNotNull(backingMap);
    }

    @CanIgnoreReturnValue
    final @Nullable V put(K key, V value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(value);
        this.clearCache();
        return this.backingMap.put(key, value);
    }

    @CanIgnoreReturnValue
    final @Nullable V remove(Object key) {
        Preconditions.checkNotNull(key);
        this.clearCache();
        return this.backingMap.remove(key);
    }

    final void clear() {
        this.clearCache();
        this.backingMap.clear();
    }

    @Nullable V get(Object key) {
        Preconditions.checkNotNull(key);
        V value = this.getIfCached(key);
        if (value == null) {
            return this.getWithoutCaching(key);
        }
        return value;
    }

    final @Nullable V getWithoutCaching(Object key) {
        Preconditions.checkNotNull(key);
        return this.backingMap.get(key);
    }

    final boolean containsKey(@Nullable Object key) {
        return this.getIfCached(key) != null || this.backingMap.containsKey(key);
    }

    final Set<K> unmodifiableKeySet() {
        return new AbstractSet<K>(){

            @Override
            public UnmodifiableIterator<K> iterator() {
                final Iterator entryIterator = MapIteratorCache.this.backingMap.entrySet().iterator();
                return new UnmodifiableIterator<K>(this){
                    final /* synthetic */ 1 this$1;
                    {
                        this.this$1 = this$1;
                    }

                    @Override
                    public boolean hasNext() {
                        return entryIterator.hasNext();
                    }

                    @Override
                    public K next() {
                        Map.Entry entry = (Map.Entry)entryIterator.next();
                        this.this$1.MapIteratorCache.this.cacheEntry = entry;
                        return entry.getKey();
                    }
                };
            }

            @Override
            public int size() {
                return MapIteratorCache.this.backingMap.size();
            }

            @Override
            public boolean contains(@Nullable Object key) {
                return MapIteratorCache.this.containsKey(key);
            }
        };
    }

    @Nullable V getIfCached(@Nullable Object key) {
        Map.Entry<K, V> entry = this.cacheEntry;
        if (entry != null && entry.getKey() == key) {
            return entry.getValue();
        }
        return null;
    }

    void clearCache() {
        this.cacheEntry = null;
    }
}

