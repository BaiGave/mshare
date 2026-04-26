/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.objects;

import it.unimi.dsi.fastutil.ints.IntBinaryOperator;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.objects.AbstractReference2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIntBiConsumer;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntFunction;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

public interface Reference2IntMap<K>
extends Reference2IntFunction<K>,
Map<K, Integer> {
    @Override
    public int size();

    @Override
    default public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void defaultReturnValue(int var1);

    @Override
    public int defaultReturnValue();

    public ObjectSet<Entry<K>> reference2IntEntrySet();

    @Override
    @Deprecated
    default public ObjectSet<Map.Entry<K, Integer>> entrySet() {
        return this.reference2IntEntrySet();
    }

    @Override
    @Deprecated
    default public Integer put(K key, Integer value) {
        return Reference2IntFunction.super.put(key, value);
    }

    @Override
    @Deprecated
    default public Integer get(Object key) {
        return Reference2IntFunction.super.get(key);
    }

    @Override
    @Deprecated
    default public Integer remove(Object key) {
        return Reference2IntFunction.super.remove(key);
    }

    @Override
    public ReferenceSet<K> keySet();

    public IntCollection values();

    @Override
    public boolean containsKey(Object var1);

    public boolean containsValue(int var1);

    @Override
    @Deprecated
    default public boolean containsValue(Object value) {
        return value == null ? false : this.containsValue((Integer)value);
    }

    default public void forEach(ObjectIntBiConsumer<? super K> consumer) {
        ObjectSet<Entry<K>> entrySet = this.reference2IntEntrySet();
        Consumer<Entry> wrappingConsumer = entry -> consumer.accept((Object)entry.getKey(), entry.getIntValue());
        if (entrySet instanceof FastEntrySet) {
            ((FastEntrySet)entrySet).fastForEach(wrappingConsumer);
        } else {
            entrySet.forEach(wrappingConsumer);
        }
    }

    @Override
    @Deprecated
    default public void forEach(BiConsumer<? super K, ? super Integer> action) {
        Map.super.forEach(action);
    }

    @Override
    default public int getOrDefault(Object key, int defaultValue) {
        int v = this.getInt(key);
        return v != this.defaultReturnValue() || this.containsKey(key) ? v : defaultValue;
    }

    @Override
    @Deprecated
    default public Integer getOrDefault(Object key, Integer defaultValue) {
        return Map.super.getOrDefault(key, defaultValue);
    }

    @Override
    default public int putIfAbsent(K key, int value) {
        int drv;
        int v = this.getInt(key);
        if (v != (drv = this.defaultReturnValue()) || this.containsKey(key)) {
            return v;
        }
        this.put(key, value);
        return drv;
    }

    default public boolean remove(Object key, int value) {
        int curValue = this.getInt(key);
        if (curValue != value || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.removeInt(key);
        return true;
    }

    @Override
    default public boolean replace(K key, int oldValue, int newValue) {
        int curValue = this.getInt(key);
        if (curValue != oldValue || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.put(key, newValue);
        return true;
    }

    @Override
    default public int replace(K key, int value) {
        return this.containsKey(key) ? this.put(key, value) : this.defaultReturnValue();
    }

    default public int computeIfAbsent(K key, ToIntFunction<? super K> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        int v = this.getInt(key);
        if (v != this.defaultReturnValue() || this.containsKey(key)) {
            return v;
        }
        int newValue = mappingFunction.applyAsInt(key);
        this.put(key, newValue);
        return newValue;
    }

    @Deprecated
    default public int computeIntIfAbsent(K key, ToIntFunction<? super K> mappingFunction) {
        return this.computeIfAbsent(key, mappingFunction);
    }

    default public int computeIfAbsent(K key, Reference2IntFunction<? super K> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        int v = this.getInt(key);
        int drv = this.defaultReturnValue();
        if (v != drv || this.containsKey(key)) {
            return v;
        }
        if (!mappingFunction.containsKey(key)) {
            return drv;
        }
        int newValue = mappingFunction.getInt(key);
        this.put(key, newValue);
        return newValue;
    }

    @Deprecated
    default public int computeIntIfAbsentPartial(K key, Reference2IntFunction<? super K> mappingFunction) {
        return this.computeIfAbsent(key, mappingFunction);
    }

    default public int computeIntIfPresent(K key, BiFunction<? super K, ? super Integer, ? extends Integer> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        int oldValue = this.getInt(key);
        int drv = this.defaultReturnValue();
        if (oldValue == drv && !this.containsKey(key)) {
            return drv;
        }
        Integer newValue = remappingFunction.apply(key, oldValue);
        if (newValue == null) {
            this.removeInt(key);
            return drv;
        }
        int newVal = newValue;
        this.put(key, newVal);
        return newVal;
    }

    default public int computeInt(K key, BiFunction<? super K, ? super Integer, ? extends Integer> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        int oldValue = this.getInt(key);
        int drv = this.defaultReturnValue();
        boolean contained = oldValue != drv || this.containsKey(key);
        Integer newValue = remappingFunction.apply(key, contained ? Integer.valueOf(oldValue) : null);
        if (newValue == null) {
            if (contained) {
                this.removeInt(key);
            }
            return drv;
        }
        int newVal = newValue;
        this.put(key, newVal);
        return newVal;
    }

    @Override
    default public int merge(K key, int value, BiFunction<? super Integer, ? super Integer, ? extends Integer> remappingFunction) {
        int newValue;
        Objects.requireNonNull(remappingFunction);
        int oldValue = this.getInt(key);
        int drv = this.defaultReturnValue();
        if (oldValue != drv || this.containsKey(key)) {
            Integer mergedValue = remappingFunction.apply((Integer)oldValue, (Integer)value);
            if (mergedValue == null) {
                this.removeInt(key);
                return drv;
            }
            newValue = mergedValue;
        } else {
            newValue = value;
        }
        this.put(key, newValue);
        return newValue;
    }

    default public int mergeInt(K key, int value, java.util.function.IntBinaryOperator remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        int oldValue = this.getInt(key);
        int drv = this.defaultReturnValue();
        int newValue = oldValue != drv || this.containsKey(key) ? remappingFunction.applyAsInt(oldValue, value) : value;
        this.put(key, newValue);
        return newValue;
    }

    default public int mergeInt(K key, int value, IntBinaryOperator remappingFunction) {
        return this.mergeInt(key, value, (java.util.function.IntBinaryOperator)remappingFunction);
    }

    @Deprecated
    default public int mergeInt(K key, int value, BiFunction<? super Integer, ? super Integer, ? extends Integer> remappingFunction) {
        return this.merge(key, value, remappingFunction);
    }

    @Override
    @Deprecated
    default public Integer putIfAbsent(K key, Integer value) {
        return Map.super.putIfAbsent(key, value);
    }

    @Override
    @Deprecated
    default public boolean remove(Object key, Object value) {
        return Map.super.remove(key, value);
    }

    @Override
    @Deprecated
    default public boolean replace(K key, Integer oldValue, Integer newValue) {
        return Map.super.replace(key, oldValue, newValue);
    }

    @Override
    @Deprecated
    default public Integer replace(K key, Integer value) {
        return Map.super.replace(key, value);
    }

    @Override
    @Deprecated
    default public Integer merge(K key, Integer value, BiFunction<? super Integer, ? super Integer, ? extends Integer> remappingFunction) {
        return Map.super.merge(key, value, remappingFunction);
    }

    @SafeVarargs
    public static <K> Reference2IntMap<K> ofEntries(Entry<K> ... entries) {
        if (entries.length == 0) {
            return Reference2IntMaps.EMPTY_MAP;
        }
        if (entries.length == 1) {
            return Reference2IntMaps.singleton(entries[0].getKey(), entries[0].getIntValue());
        }
        if (entries.length <= 8) {
            Object[] keys = new Object[entries.length];
            int[] vals = new int[entries.length];
            for (int i = 0; i < entries.length; ++i) {
                Entry<K> entry = entries[i];
                keys[i] = entry.getKey();
                for (int j = 0; j < i; ++j) {
                    if (keys[j] != keys[i]) continue;
                    throw new IllegalArgumentException("duplicate key: " + keys[i]);
                }
                vals[i] = entry.getIntValue();
            }
            return Reference2IntMaps.unmodifiable(new Reference2IntArrayMap(keys, vals, entries.length));
        }
        Reference2IntOpenHashMap newMap = new Reference2IntOpenHashMap(entries.length, 0.75f);
        for (Entry<K> entry : entries) {
            if (newMap.put(entry.getKey(), entry.getIntValue()) == 0) continue;
            throw new IllegalArgumentException("duplicate key: " + entry.getKey());
        }
        return Reference2IntMaps.unmodifiable(newMap);
    }

    public static <K> Entry<K> entry(K key, int value) {
        return new AbstractReference2IntMap.BasicEntry<K>(key, value);
    }

    public static interface FastEntrySet<K>
    extends ObjectSet<Entry<K>> {
        public ObjectIterator<Entry<K>> fastIterator();

        default public void fastForEach(Consumer<? super Entry<K>> consumer) {
            this.forEach(consumer);
        }
    }

    public static interface Entry<K>
    extends Map.Entry<K, Integer> {
        public int getIntValue();

        @Override
        public int setValue(int var1);

        @Override
        @Deprecated
        default public Integer getValue() {
            return this.getIntValue();
        }

        @Override
        @Deprecated
        default public Integer setValue(Integer value) {
            return this.setValue((int)value);
        }
    }
}

