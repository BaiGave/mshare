/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.floats;

import it.unimi.dsi.fastutil.floats.AbstractFloat2ReferenceMap;
import it.unimi.dsi.fastutil.floats.Float2ReferenceArrayMap;
import it.unimi.dsi.fastutil.floats.Float2ReferenceFunction;
import it.unimi.dsi.fastutil.floats.Float2ReferenceMaps;
import it.unimi.dsi.fastutil.floats.Float2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.floats.FloatObjectBiConsumer;
import it.unimi.dsi.fastutil.floats.FloatSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ReferenceCollection;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;

public interface Float2ReferenceMap<V>
extends Float2ReferenceFunction<V>,
Map<Float, V> {
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

    public ObjectSet<Entry<V>> float2ReferenceEntrySet();

    @Override
    @Deprecated
    default public ObjectSet<Map.Entry<Float, V>> entrySet() {
        return this.float2ReferenceEntrySet();
    }

    @Override
    @Deprecated
    default public V put(Float key, V value) {
        return Float2ReferenceFunction.super.put(key, value);
    }

    @Override
    @Deprecated
    default public V get(Object key) {
        return Float2ReferenceFunction.super.get(key);
    }

    @Override
    @Deprecated
    default public V remove(Object key) {
        return Float2ReferenceFunction.super.remove(key);
    }

    public FloatSet keySet();

    @Override
    public ReferenceCollection<V> values();

    @Override
    public boolean containsKey(float var1);

    @Override
    @Deprecated
    default public boolean containsKey(Object key) {
        return Float2ReferenceFunction.super.containsKey(key);
    }

    default public void forEach(FloatObjectBiConsumer<? super V> consumer) {
        ObjectSet<Entry<V>> entrySet = this.float2ReferenceEntrySet();
        Consumer<Entry> wrappingConsumer = entry -> consumer.accept(entry.getFloatKey(), (Object)entry.getValue());
        if (entrySet instanceof FastEntrySet) {
            ((FastEntrySet)entrySet).fastForEach(wrappingConsumer);
        } else {
            entrySet.forEach(wrappingConsumer);
        }
    }

    @Override
    @Deprecated
    default public void forEach(BiConsumer<? super Float, ? super V> action) {
        Map.super.forEach(action);
    }

    @Override
    default public V getOrDefault(float key, V defaultValue) {
        Object v = this.get(key);
        return v != this.defaultReturnValue() || this.containsKey(key) ? v : defaultValue;
    }

    @Override
    @Deprecated
    default public V getOrDefault(Object key, V defaultValue) {
        return Map.super.getOrDefault(key, defaultValue);
    }

    @Override
    default public V putIfAbsent(float key, V value) {
        V drv;
        Object v = this.get(key);
        if (v != (drv = this.defaultReturnValue()) || this.containsKey(key)) {
            return v;
        }
        this.put(key, value);
        return drv;
    }

    default public boolean remove(float key, Object value) {
        Object curValue = this.get(key);
        if (curValue != value || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.remove(key);
        return true;
    }

    @Override
    default public boolean replace(float key, V oldValue, V newValue) {
        Object curValue = this.get(key);
        if (curValue != oldValue || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.put(key, newValue);
        return true;
    }

    @Override
    default public V replace(float key, V value) {
        return this.containsKey(key) ? this.put(key, value) : this.defaultReturnValue();
    }

    default public V computeIfAbsent(float key, DoubleFunction<? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        Object v = this.get(key);
        if (v != this.defaultReturnValue() || this.containsKey(key)) {
            return v;
        }
        V newValue = mappingFunction.apply(key);
        this.put(key, newValue);
        return newValue;
    }

    default public V computeIfAbsent(float key, Float2ReferenceFunction<? extends V> mappingFunction) {
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
    default public V computeIfAbsentPartial(float key, Float2ReferenceFunction<? extends V> mappingFunction) {
        return this.computeIfAbsent(key, mappingFunction);
    }

    @Override
    default public V computeIfPresent(float key, BiFunction<? super Float, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Object oldValue = this.get(key);
        V drv = this.defaultReturnValue();
        if (oldValue == drv && !this.containsKey(key)) {
            return drv;
        }
        V newValue = remappingFunction.apply(Float.valueOf(key), oldValue);
        if (newValue == null) {
            this.remove(key);
            return drv;
        }
        this.put(key, newValue);
        return newValue;
    }

    @Override
    default public V compute(float key, BiFunction<? super Float, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Object oldValue = this.get(key);
        V drv = this.defaultReturnValue();
        boolean contained = oldValue != drv || this.containsKey(key);
        V newValue = remappingFunction.apply(Float.valueOf(key), contained ? oldValue : null);
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
    default public V merge(float key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
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
    public static <V> Float2ReferenceMap<V> ofEntries(Entry<V> ... entries) {
        if (entries.length == 0) {
            return Float2ReferenceMaps.EMPTY_MAP;
        }
        if (entries.length == 1) {
            return Float2ReferenceMaps.singleton(entries[0].getFloatKey(), entries[0].getValue());
        }
        if (entries.length <= 8) {
            float[] keys = new float[entries.length];
            Object[] vals = new Object[entries.length];
            for (int i = 0; i < entries.length; ++i) {
                Entry<V> entry = entries[i];
                keys[i] = entry.getFloatKey();
                for (int j = 0; j < i; ++j) {
                    if (Float.floatToRawIntBits(keys[j]) != Float.floatToRawIntBits(keys[i])) continue;
                    throw new IllegalArgumentException("duplicate key: " + keys[i]);
                }
                vals[i] = entry.getValue();
            }
            return Float2ReferenceMaps.unmodifiable(new Float2ReferenceArrayMap(keys, vals, entries.length));
        }
        Float2ReferenceOpenHashMap newMap = new Float2ReferenceOpenHashMap(entries.length, 0.75f);
        for (Entry<V> entry : entries) {
            if (newMap.put(entry.getFloatKey(), entry.getValue()) == null) continue;
            throw new IllegalArgumentException("duplicate key: " + entry.getFloatKey());
        }
        return Float2ReferenceMaps.unmodifiable(newMap);
    }

    public static <V> Entry<V> entry(float key, V value) {
        return new AbstractFloat2ReferenceMap.BasicEntry<V>(key, value);
    }

    public static interface FastEntrySet<V>
    extends ObjectSet<Entry<V>> {
        public ObjectIterator<Entry<V>> fastIterator();

        default public void fastForEach(Consumer<? super Entry<V>> consumer) {
            this.forEach(consumer);
        }
    }

    public static interface Entry<V>
    extends Map.Entry<Float, V> {
        public float getFloatKey();

        @Override
        @Deprecated
        default public Float getKey() {
            return Float.valueOf(this.getFloatKey());
        }
    }
}

