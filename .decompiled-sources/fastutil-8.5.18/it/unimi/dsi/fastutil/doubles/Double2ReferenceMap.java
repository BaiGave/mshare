/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.doubles;

import it.unimi.dsi.fastutil.doubles.AbstractDouble2ReferenceMap;
import it.unimi.dsi.fastutil.doubles.Double2ReferenceArrayMap;
import it.unimi.dsi.fastutil.doubles.Double2ReferenceFunction;
import it.unimi.dsi.fastutil.doubles.Double2ReferenceMaps;
import it.unimi.dsi.fastutil.doubles.Double2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.doubles.DoubleObjectBiConsumer;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ReferenceCollection;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;

public interface Double2ReferenceMap<V>
extends Double2ReferenceFunction<V>,
Map<Double, V> {
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

    public ObjectSet<Entry<V>> double2ReferenceEntrySet();

    @Override
    @Deprecated
    default public ObjectSet<Map.Entry<Double, V>> entrySet() {
        return this.double2ReferenceEntrySet();
    }

    @Override
    @Deprecated
    default public V put(Double key, V value) {
        return Double2ReferenceFunction.super.put(key, value);
    }

    @Override
    @Deprecated
    default public V get(Object key) {
        return Double2ReferenceFunction.super.get(key);
    }

    @Override
    @Deprecated
    default public V remove(Object key) {
        return Double2ReferenceFunction.super.remove(key);
    }

    public DoubleSet keySet();

    @Override
    public ReferenceCollection<V> values();

    @Override
    public boolean containsKey(double var1);

    @Override
    @Deprecated
    default public boolean containsKey(Object key) {
        return Double2ReferenceFunction.super.containsKey(key);
    }

    default public void forEach(DoubleObjectBiConsumer<? super V> consumer) {
        ObjectSet<Entry<V>> entrySet = this.double2ReferenceEntrySet();
        Consumer<Entry> wrappingConsumer = entry -> consumer.accept(entry.getDoubleKey(), (Object)entry.getValue());
        if (entrySet instanceof FastEntrySet) {
            ((FastEntrySet)entrySet).fastForEach(wrappingConsumer);
        } else {
            entrySet.forEach(wrappingConsumer);
        }
    }

    @Override
    @Deprecated
    default public void forEach(BiConsumer<? super Double, ? super V> action) {
        Map.super.forEach(action);
    }

    @Override
    default public V getOrDefault(double key, V defaultValue) {
        Object v = this.get(key);
        return v != this.defaultReturnValue() || this.containsKey(key) ? v : defaultValue;
    }

    @Override
    @Deprecated
    default public V getOrDefault(Object key, V defaultValue) {
        return Map.super.getOrDefault(key, defaultValue);
    }

    @Override
    default public V putIfAbsent(double key, V value) {
        V drv;
        Object v = this.get(key);
        if (v != (drv = this.defaultReturnValue()) || this.containsKey(key)) {
            return v;
        }
        this.put(key, value);
        return drv;
    }

    default public boolean remove(double key, Object value) {
        Object curValue = this.get(key);
        if (curValue != value || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.remove(key);
        return true;
    }

    @Override
    default public boolean replace(double key, V oldValue, V newValue) {
        Object curValue = this.get(key);
        if (curValue != oldValue || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.put(key, newValue);
        return true;
    }

    @Override
    default public V replace(double key, V value) {
        return this.containsKey(key) ? this.put(key, value) : this.defaultReturnValue();
    }

    default public V computeIfAbsent(double key, DoubleFunction<? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        Object v = this.get(key);
        if (v != this.defaultReturnValue() || this.containsKey(key)) {
            return v;
        }
        V newValue = mappingFunction.apply(key);
        this.put(key, newValue);
        return newValue;
    }

    default public V computeIfAbsent(double key, Double2ReferenceFunction<? extends V> mappingFunction) {
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
    default public V computeIfAbsentPartial(double key, Double2ReferenceFunction<? extends V> mappingFunction) {
        return this.computeIfAbsent(key, mappingFunction);
    }

    @Override
    default public V computeIfPresent(double key, BiFunction<? super Double, ? super V, ? extends V> remappingFunction) {
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
    default public V compute(double key, BiFunction<? super Double, ? super V, ? extends V> remappingFunction) {
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
    default public V merge(double key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
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
    public static <V> Double2ReferenceMap<V> ofEntries(Entry<V> ... entries) {
        if (entries.length == 0) {
            return Double2ReferenceMaps.EMPTY_MAP;
        }
        if (entries.length == 1) {
            return Double2ReferenceMaps.singleton(entries[0].getDoubleKey(), entries[0].getValue());
        }
        if (entries.length <= 8) {
            double[] keys = new double[entries.length];
            Object[] vals = new Object[entries.length];
            for (int i = 0; i < entries.length; ++i) {
                Entry<V> entry = entries[i];
                keys[i] = entry.getDoubleKey();
                for (int j = 0; j < i; ++j) {
                    if (Double.doubleToRawLongBits(keys[j]) != Double.doubleToRawLongBits(keys[i])) continue;
                    throw new IllegalArgumentException("duplicate key: " + keys[i]);
                }
                vals[i] = entry.getValue();
            }
            return Double2ReferenceMaps.unmodifiable(new Double2ReferenceArrayMap(keys, vals, entries.length));
        }
        Double2ReferenceOpenHashMap newMap = new Double2ReferenceOpenHashMap(entries.length, 0.75f);
        for (Entry<V> entry : entries) {
            if (newMap.put(entry.getDoubleKey(), entry.getValue()) == null) continue;
            throw new IllegalArgumentException("duplicate key: " + entry.getDoubleKey());
        }
        return Double2ReferenceMaps.unmodifiable(newMap);
    }

    public static <V> Entry<V> entry(double key, V value) {
        return new AbstractDouble2ReferenceMap.BasicEntry<V>(key, value);
    }

    public static interface FastEntrySet<V>
    extends ObjectSet<Entry<V>> {
        public ObjectIterator<Entry<V>> fastIterator();

        default public void fastForEach(Consumer<? super Entry<V>> consumer) {
            this.forEach(consumer);
        }
    }

    public static interface Entry<V>
    extends Map.Entry<Double, V> {
        public double getDoubleKey();

        @Override
        @Deprecated
        default public Double getKey() {
            return this.getDoubleKey();
        }
    }
}

