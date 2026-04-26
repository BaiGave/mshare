/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.objects;

import it.unimi.dsi.fastutil.objects.AbstractObject2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceFunction;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ReferenceCollection;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public interface Object2ReferenceMap<K, V>
extends Object2ReferenceFunction<K, V>,
Map<K, V> {
    @Override
    public int size();

    @Override
    default public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void defaultReturnValue(V var1);

    @Override
    public V defaultReturnValue();

    public ObjectSet<Entry<K, V>> object2ReferenceEntrySet();

    @Override
    default public ObjectSet<Map.Entry<K, V>> entrySet() {
        return this.object2ReferenceEntrySet();
    }

    @Override
    default public V put(K key, V value) {
        return Object2ReferenceFunction.super.put(key, value);
    }

    @Override
    default public V remove(Object key) {
        return Object2ReferenceFunction.super.remove(key);
    }

    @Override
    public ObjectSet<K> keySet();

    @Override
    public ReferenceCollection<V> values();

    @Override
    public boolean containsKey(Object var1);

    @Override
    default public V getOrDefault(Object key, V defaultValue) {
        Object v = this.get(key);
        return v != this.defaultReturnValue() || this.containsKey(key) ? v : defaultValue;
    }

    @Override
    default public V putIfAbsent(K key, V value) {
        V drv;
        Object v = this.get(key);
        if (v != (drv = this.defaultReturnValue()) || this.containsKey(key)) {
            return v;
        }
        this.put(key, value);
        return drv;
    }

    @Override
    default public boolean remove(Object key, Object value) {
        Object curValue = this.get(key);
        if (curValue != value || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.remove(key);
        return true;
    }

    @Override
    default public boolean replace(K key, V oldValue, V newValue) {
        Object curValue = this.get(key);
        if (curValue != oldValue || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.put(key, newValue);
        return true;
    }

    @Override
    default public V replace(K key, V value) {
        return this.containsKey(key) ? this.put(key, value) : this.defaultReturnValue();
    }

    @Override
    default public V computeIfAbsent(K key, Object2ReferenceFunction<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        Object v = this.get(key);
        V drv = this.defaultReturnValue();
        if (v != drv || this.containsKey(key)) {
            return v;
        }
        if (!mappingFunction.containsKey(key)) {
            return drv;
        }
        V newValue = mappingFunction.get(key);
        this.put(key, newValue);
        return newValue;
    }

    @Deprecated
    default public V computeReferenceIfAbsentPartial(K key, Object2ReferenceFunction<? super K, ? extends V> mappingFunction) {
        return this.computeIfAbsent(key, mappingFunction);
    }

    @Override
    default public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Object oldValue = this.get(key);
        V drv = this.defaultReturnValue();
        if (oldValue == drv && !this.containsKey(key)) {
            return drv;
        }
        V newValue = remappingFunction.apply(key, oldValue);
        if (newValue == null) {
            this.remove(key);
            return drv;
        }
        this.put(key, newValue);
        return newValue;
    }

    @Override
    default public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Object oldValue = this.get(key);
        V drv = this.defaultReturnValue();
        boolean contained = oldValue != drv || this.containsKey(key);
        V newValue = remappingFunction.apply(key, contained ? oldValue : null);
        if (newValue == null) {
            if (contained) {
                this.remove(key);
            }
            return drv;
        }
        this.put(key, newValue);
        return newValue;
    }

    @Override
    default public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        V newValue;
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);
        Object oldValue = this.get(key);
        V drv = this.defaultReturnValue();
        if (oldValue != drv || this.containsKey(key)) {
            V mergedValue = remappingFunction.apply(oldValue, value);
            if (mergedValue == null) {
                this.remove(key);
                return drv;
            }
            newValue = mergedValue;
        } else {
            newValue = value;
        }
        this.put(key, newValue);
        return newValue;
    }

    @SafeVarargs
    public static <K, V> Object2ReferenceMap<K, V> ofEntries(Entry<K, V> ... entries) {
        if (entries.length == 0) {
            return Object2ReferenceMaps.EMPTY_MAP;
        }
        if (entries.length == 1) {
            return Object2ReferenceMaps.singleton(entries[0].getKey(), entries[0].getValue());
        }
        if (entries.length <= 8) {
            Object[] keys = new Object[entries.length];
            Object[] vals = new Object[entries.length];
            for (int i = 0; i < entries.length; ++i) {
                Entry<K, V> entry = entries[i];
                keys[i] = entry.getKey();
                for (int j = 0; j < i; ++j) {
                    if (!Objects.equals(keys[j], keys[i])) continue;
                    throw new IllegalArgumentException("duplicate key: " + keys[i]);
                }
                vals[i] = entry.getValue();
            }
            return Object2ReferenceMaps.unmodifiable(new Object2ReferenceArrayMap(keys, vals, entries.length));
        }
        Object2ReferenceOpenHashMap newMap = new Object2ReferenceOpenHashMap(entries.length, 0.75f);
        for (Entry<K, V> entry : entries) {
            if (newMap.put(entry.getKey(), entry.getValue()) == null) continue;
            throw new IllegalArgumentException("duplicate key: " + entry.getKey());
        }
        return Object2ReferenceMaps.unmodifiable(newMap);
    }

    public static <K, V> Entry<K, V> entry(K key, V value) {
        return new AbstractObject2ReferenceMap.BasicEntry<K, V>(key, value);
    }

    public static interface Entry<K, V>
    extends Map.Entry<K, V> {
    }

    public static interface FastEntrySet<K, V>
    extends ObjectSet<Entry<K, V>> {
        public ObjectIterator<Entry<K, V>> fastIterator();

        default public void fastForEach(Consumer<? super Entry<K, V>> consumer) {
            this.forEach(consumer);
        }
    }
}

