/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.objects;

import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.objects.AbstractObject2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongFunction;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectLongBiConsumer;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.LongBinaryOperator;
import java.util.function.ToLongFunction;

public interface Object2LongMap<K>
extends Object2LongFunction<K>,
Map<K, Long> {
    @Override
    public int size();

    @Override
    default public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void defaultReturnValue(long var1);

    @Override
    public long defaultReturnValue();

    public ObjectSet<Entry<K>> object2LongEntrySet();

    @Override
    @Deprecated
    default public ObjectSet<Map.Entry<K, Long>> entrySet() {
        return this.object2LongEntrySet();
    }

    @Override
    @Deprecated
    default public Long put(K key, Long value) {
        return Object2LongFunction.super.put(key, value);
    }

    @Override
    @Deprecated
    default public Long get(Object key) {
        return Object2LongFunction.super.get(key);
    }

    @Override
    @Deprecated
    default public Long remove(Object key) {
        return Object2LongFunction.super.remove(key);
    }

    @Override
    public ObjectSet<K> keySet();

    public LongCollection values();

    @Override
    public boolean containsKey(Object var1);

    public boolean containsValue(long var1);

    @Override
    @Deprecated
    default public boolean containsValue(Object value) {
        return value == null ? false : this.containsValue((Long)value);
    }

    default public void forEach(ObjectLongBiConsumer<? super K> consumer) {
        ObjectSet<Entry<K>> entrySet = this.object2LongEntrySet();
        Consumer<Entry> wrappingConsumer = entry -> consumer.accept((Object)entry.getKey(), entry.getLongValue());
        if (entrySet instanceof FastEntrySet) {
            ((FastEntrySet)entrySet).fastForEach(wrappingConsumer);
        } else {
            entrySet.forEach(wrappingConsumer);
        }
    }

    @Override
    @Deprecated
    default public void forEach(BiConsumer<? super K, ? super Long> action) {
        Map.super.forEach(action);
    }

    @Override
    default public long getOrDefault(Object key, long defaultValue) {
        long v = this.getLong(key);
        return v != this.defaultReturnValue() || this.containsKey(key) ? v : defaultValue;
    }

    @Override
    @Deprecated
    default public Long getOrDefault(Object key, Long defaultValue) {
        return Map.super.getOrDefault(key, defaultValue);
    }

    @Override
    default public long putIfAbsent(K key, long value) {
        long drv;
        long v = this.getLong(key);
        if (v != (drv = this.defaultReturnValue()) || this.containsKey(key)) {
            return v;
        }
        this.put(key, value);
        return drv;
    }

    default public boolean remove(Object key, long value) {
        long curValue = this.getLong(key);
        if (curValue != value || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.removeLong(key);
        return true;
    }

    @Override
    default public boolean replace(K key, long oldValue, long newValue) {
        long curValue = this.getLong(key);
        if (curValue != oldValue || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.put(key, newValue);
        return true;
    }

    @Override
    default public long replace(K key, long value) {
        return this.containsKey(key) ? this.put(key, value) : this.defaultReturnValue();
    }

    default public long computeIfAbsent(K key, ToLongFunction<? super K> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        long v = this.getLong(key);
        if (v != this.defaultReturnValue() || this.containsKey(key)) {
            return v;
        }
        long newValue = mappingFunction.applyAsLong(key);
        this.put(key, newValue);
        return newValue;
    }

    @Deprecated
    default public long computeLongIfAbsent(K key, ToLongFunction<? super K> mappingFunction) {
        return this.computeIfAbsent(key, mappingFunction);
    }

    default public long computeIfAbsent(K key, Object2LongFunction<? super K> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        long v = this.getLong(key);
        long drv = this.defaultReturnValue();
        if (v != drv || this.containsKey(key)) {
            return v;
        }
        if (!mappingFunction.containsKey(key)) {
            return drv;
        }
        long newValue = mappingFunction.getLong(key);
        this.put(key, newValue);
        return newValue;
    }

    @Deprecated
    default public long computeLongIfAbsentPartial(K key, Object2LongFunction<? super K> mappingFunction) {
        return this.computeIfAbsent(key, mappingFunction);
    }

    default public long computeLongIfPresent(K key, BiFunction<? super K, ? super Long, ? extends Long> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        long oldValue = this.getLong(key);
        long drv = this.defaultReturnValue();
        if (oldValue == drv && !this.containsKey(key)) {
            return drv;
        }
        Long newValue = remappingFunction.apply(key, oldValue);
        if (newValue == null) {
            this.removeLong(key);
            return drv;
        }
        long newVal = newValue;
        this.put(key, newVal);
        return newVal;
    }

    default public long computeLong(K key, BiFunction<? super K, ? super Long, ? extends Long> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        long oldValue = this.getLong(key);
        long drv = this.defaultReturnValue();
        boolean contained = oldValue != drv || this.containsKey(key);
        Long newValue = remappingFunction.apply(key, contained ? Long.valueOf(oldValue) : null);
        if (newValue == null) {
            if (contained) {
                this.removeLong(key);
            }
            return drv;
        }
        long newVal = newValue;
        this.put(key, newVal);
        return newVal;
    }

    @Override
    default public long merge(K key, long value, BiFunction<? super Long, ? super Long, ? extends Long> remappingFunction) {
        long newValue;
        Objects.requireNonNull(remappingFunction);
        long oldValue = this.getLong(key);
        long drv = this.defaultReturnValue();
        if (oldValue != drv || this.containsKey(key)) {
            Long mergedValue = remappingFunction.apply((Long)oldValue, (Long)value);
            if (mergedValue == null) {
                this.removeLong(key);
                return drv;
            }
            newValue = mergedValue;
        } else {
            newValue = value;
        }
        this.put(key, newValue);
        return newValue;
    }

    default public long mergeLong(K key, long value, LongBinaryOperator remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        long oldValue = this.getLong(key);
        long drv = this.defaultReturnValue();
        long newValue = oldValue != drv || this.containsKey(key) ? remappingFunction.applyAsLong(oldValue, value) : value;
        this.put(key, newValue);
        return newValue;
    }

    default public long mergeLong(K key, long value, it.unimi.dsi.fastutil.longs.LongBinaryOperator remappingFunction) {
        return this.mergeLong(key, value, (LongBinaryOperator)remappingFunction);
    }

    @Deprecated
    default public long mergeLong(K key, long value, BiFunction<? super Long, ? super Long, ? extends Long> remappingFunction) {
        return this.merge(key, value, remappingFunction);
    }

    @Override
    @Deprecated
    default public Long putIfAbsent(K key, Long value) {
        return Map.super.putIfAbsent(key, value);
    }

    @Override
    @Deprecated
    default public boolean remove(Object key, Object value) {
        return Map.super.remove(key, value);
    }

    @Override
    @Deprecated
    default public boolean replace(K key, Long oldValue, Long newValue) {
        return Map.super.replace(key, oldValue, newValue);
    }

    @Override
    @Deprecated
    default public Long replace(K key, Long value) {
        return Map.super.replace(key, value);
    }

    @Override
    @Deprecated
    default public Long merge(K key, Long value, BiFunction<? super Long, ? super Long, ? extends Long> remappingFunction) {
        return Map.super.merge(key, value, remappingFunction);
    }

    @SafeVarargs
    public static <K> Object2LongMap<K> ofEntries(Entry<K> ... entries) {
        if (entries.length == 0) {
            return Object2LongMaps.EMPTY_MAP;
        }
        if (entries.length == 1) {
            return Object2LongMaps.singleton(entries[0].getKey(), entries[0].getLongValue());
        }
        if (entries.length <= 8) {
            Object[] keys = new Object[entries.length];
            long[] vals = new long[entries.length];
            for (int i = 0; i < entries.length; ++i) {
                Entry<K> entry = entries[i];
                keys[i] = entry.getKey();
                for (int j = 0; j < i; ++j) {
                    if (!Objects.equals(keys[j], keys[i])) continue;
                    throw new IllegalArgumentException("duplicate key: " + keys[i]);
                }
                vals[i] = entry.getLongValue();
            }
            return Object2LongMaps.unmodifiable(new Object2LongArrayMap(keys, vals, entries.length));
        }
        Object2LongOpenHashMap newMap = new Object2LongOpenHashMap(entries.length, 0.75f);
        for (Entry<K> entry : entries) {
            if (newMap.put(entry.getKey(), entry.getLongValue()) == 0L) continue;
            throw new IllegalArgumentException("duplicate key: " + entry.getKey());
        }
        return Object2LongMaps.unmodifiable(newMap);
    }

    public static <K> Entry<K> entry(K key, long value) {
        return new AbstractObject2LongMap.BasicEntry<K>(key, value);
    }

    public static interface FastEntrySet<K>
    extends ObjectSet<Entry<K>> {
        public ObjectIterator<Entry<K>> fastIterator();

        default public void fastForEach(Consumer<? super Entry<K>> consumer) {
            this.forEach(consumer);
        }
    }

    public static interface Entry<K>
    extends Map.Entry<K, Long> {
        public long getLongValue();

        @Override
        public long setValue(long var1);

        @Override
        @Deprecated
        default public Long getValue() {
            return this.getLongValue();
        }

        @Override
        @Deprecated
        default public Long setValue(Long value) {
            return this.setValue((long)value);
        }
    }
}

