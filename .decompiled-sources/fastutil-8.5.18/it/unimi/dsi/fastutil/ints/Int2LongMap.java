/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.ints;

import it.unimi.dsi.fastutil.ints.AbstractInt2LongMap;
import it.unimi.dsi.fastutil.ints.Int2LongArrayMap;
import it.unimi.dsi.fastutil.ints.Int2LongFunction;
import it.unimi.dsi.fastutil.ints.Int2LongMaps;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntLongBiConsumer;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntToLongFunction;
import java.util.function.LongBinaryOperator;

public interface Int2LongMap
extends Int2LongFunction,
Map<Integer, Long> {
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

    public ObjectSet<Entry> int2LongEntrySet();

    @Override
    @Deprecated
    default public ObjectSet<Map.Entry<Integer, Long>> entrySet() {
        return this.int2LongEntrySet();
    }

    @Override
    @Deprecated
    default public Long put(Integer key, Long value) {
        return Int2LongFunction.super.put(key, value);
    }

    @Override
    @Deprecated
    default public Long get(Object key) {
        return Int2LongFunction.super.get(key);
    }

    @Override
    @Deprecated
    default public Long remove(Object key) {
        return Int2LongFunction.super.remove(key);
    }

    public IntSet keySet();

    public LongCollection values();

    @Override
    public boolean containsKey(int var1);

    @Override
    @Deprecated
    default public boolean containsKey(Object key) {
        return Int2LongFunction.super.containsKey(key);
    }

    public boolean containsValue(long var1);

    @Override
    @Deprecated
    default public boolean containsValue(Object value) {
        return value == null ? false : this.containsValue((Long)value);
    }

    default public void forEach(IntLongBiConsumer consumer) {
        ObjectSet<Entry> entrySet = this.int2LongEntrySet();
        Consumer<Entry> wrappingConsumer = entry -> consumer.accept(entry.getIntKey(), entry.getLongValue());
        if (entrySet instanceof FastEntrySet) {
            ((FastEntrySet)entrySet).fastForEach(wrappingConsumer);
        } else {
            entrySet.forEach(wrappingConsumer);
        }
    }

    @Override
    @Deprecated
    default public void forEach(BiConsumer<? super Integer, ? super Long> action) {
        Map.super.forEach(action);
    }

    @Override
    default public long getOrDefault(int key, long defaultValue) {
        long v = this.get(key);
        return v != this.defaultReturnValue() || this.containsKey(key) ? v : defaultValue;
    }

    @Override
    @Deprecated
    default public Long getOrDefault(Object key, Long defaultValue) {
        return Map.super.getOrDefault(key, defaultValue);
    }

    @Override
    default public long putIfAbsent(int key, long value) {
        long drv;
        long v = this.get(key);
        if (v != (drv = this.defaultReturnValue()) || this.containsKey(key)) {
            return v;
        }
        this.put(key, value);
        return drv;
    }

    default public boolean remove(int key, long value) {
        long curValue = this.get(key);
        if (curValue != value || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.remove(key);
        return true;
    }

    @Override
    default public boolean replace(int key, long oldValue, long newValue) {
        long curValue = this.get(key);
        if (curValue != oldValue || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.put(key, newValue);
        return true;
    }

    @Override
    default public long replace(int key, long value) {
        return this.containsKey(key) ? this.put(key, value) : this.defaultReturnValue();
    }

    default public long computeIfAbsent(int key, IntToLongFunction mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        long v = this.get(key);
        if (v != this.defaultReturnValue() || this.containsKey(key)) {
            return v;
        }
        long newValue = mappingFunction.applyAsLong(key);
        this.put(key, newValue);
        return newValue;
    }

    default public long computeIfAbsentNullable(int key, IntFunction<? extends Long> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        long v = this.get(key);
        long drv = this.defaultReturnValue();
        if (v != drv || this.containsKey(key)) {
            return v;
        }
        Long mappedValue = mappingFunction.apply(key);
        if (mappedValue == null) {
            return drv;
        }
        long newValue = mappedValue;
        this.put(key, newValue);
        return newValue;
    }

    default public long computeIfAbsent(int key, Int2LongFunction mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        long v = this.get(key);
        long drv = this.defaultReturnValue();
        if (v != drv || this.containsKey(key)) {
            return v;
        }
        if (!mappingFunction.containsKey(key)) {
            return drv;
        }
        long newValue = mappingFunction.get(key);
        this.put(key, newValue);
        return newValue;
    }

    @Deprecated
    default public long computeIfAbsentPartial(int key, Int2LongFunction mappingFunction) {
        return this.computeIfAbsent(key, mappingFunction);
    }

    @Override
    default public long computeIfPresent(int key, BiFunction<? super Integer, ? super Long, ? extends Long> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        long oldValue = this.get(key);
        long drv = this.defaultReturnValue();
        if (oldValue == drv && !this.containsKey(key)) {
            return drv;
        }
        Long newValue = remappingFunction.apply((Integer)key, (Long)oldValue);
        if (newValue == null) {
            this.remove(key);
            return drv;
        }
        long newVal = newValue;
        this.put(key, newVal);
        return newVal;
    }

    @Override
    default public long compute(int key, BiFunction<? super Integer, ? super Long, ? extends Long> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        long oldValue = this.get(key);
        long drv = this.defaultReturnValue();
        boolean contained = oldValue != drv || this.containsKey(key);
        Long newValue = remappingFunction.apply((Integer)key, contained ? Long.valueOf(oldValue) : null);
        if (newValue == null) {
            if (contained) {
                this.remove(key);
            }
            return drv;
        }
        long newVal = newValue;
        this.put(key, newVal);
        return newVal;
    }

    @Override
    default public long merge(int key, long value, BiFunction<? super Long, ? super Long, ? extends Long> remappingFunction) {
        long newValue;
        Objects.requireNonNull(remappingFunction);
        long oldValue = this.get(key);
        long drv = this.defaultReturnValue();
        if (oldValue != drv || this.containsKey(key)) {
            Long mergedValue = remappingFunction.apply((Long)oldValue, (Long)value);
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

    default public long mergeLong(int key, long value, LongBinaryOperator remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        long oldValue = this.get(key);
        long drv = this.defaultReturnValue();
        long newValue = oldValue != drv || this.containsKey(key) ? remappingFunction.applyAsLong(oldValue, value) : value;
        this.put(key, newValue);
        return newValue;
    }

    default public long mergeLong(int key, long value, it.unimi.dsi.fastutil.longs.LongBinaryOperator remappingFunction) {
        return this.mergeLong(key, value, (LongBinaryOperator)remappingFunction);
    }

    @Override
    @Deprecated
    default public Long putIfAbsent(Integer key, Long value) {
        return Map.super.putIfAbsent(key, value);
    }

    @Override
    @Deprecated
    default public boolean remove(Object key, Object value) {
        return Map.super.remove(key, value);
    }

    @Override
    @Deprecated
    default public boolean replace(Integer key, Long oldValue, Long newValue) {
        return Map.super.replace(key, oldValue, newValue);
    }

    @Override
    @Deprecated
    default public Long replace(Integer key, Long value) {
        return Map.super.replace(key, value);
    }

    @Override
    @Deprecated
    default public Long computeIfAbsent(Integer key, Function<? super Integer, ? extends Long> mappingFunction) {
        return Map.super.computeIfAbsent(key, mappingFunction);
    }

    @Override
    @Deprecated
    default public Long computeIfPresent(Integer key, BiFunction<? super Integer, ? super Long, ? extends Long> remappingFunction) {
        return Map.super.computeIfPresent(key, remappingFunction);
    }

    @Override
    @Deprecated
    default public Long compute(Integer key, BiFunction<? super Integer, ? super Long, ? extends Long> remappingFunction) {
        return Map.super.compute(key, remappingFunction);
    }

    @Override
    @Deprecated
    default public Long merge(Integer key, Long value, BiFunction<? super Long, ? super Long, ? extends Long> remappingFunction) {
        return Map.super.merge(key, value, remappingFunction);
    }

    public static Int2LongMap ofEntries(Entry ... entries) {
        if (entries.length == 0) {
            return Int2LongMaps.EMPTY_MAP;
        }
        if (entries.length == 1) {
            return Int2LongMaps.singleton(entries[0].getIntKey(), entries[0].getLongValue());
        }
        if (entries.length <= 8) {
            int[] keys = new int[entries.length];
            long[] vals = new long[entries.length];
            for (int i = 0; i < entries.length; ++i) {
                Entry entry = entries[i];
                keys[i] = entry.getIntKey();
                for (int j = 0; j < i; ++j) {
                    if (keys[j] != keys[i]) continue;
                    throw new IllegalArgumentException("duplicate key: " + keys[i]);
                }
                vals[i] = entry.getLongValue();
            }
            return Int2LongMaps.unmodifiable(new Int2LongArrayMap(keys, vals, entries.length));
        }
        Int2LongOpenHashMap newMap = new Int2LongOpenHashMap(entries.length, 0.75f);
        for (Entry entry : entries) {
            if (newMap.put(entry.getIntKey(), entry.getLongValue()) == 0L) continue;
            throw new IllegalArgumentException("duplicate key: " + entry.getIntKey());
        }
        return Int2LongMaps.unmodifiable(newMap);
    }

    public static Entry entry(int key, long value) {
        return new AbstractInt2LongMap.BasicEntry(key, value);
    }

    public static interface FastEntrySet
    extends ObjectSet<Entry> {
        public ObjectIterator<Entry> fastIterator();

        default public void fastForEach(Consumer<? super Entry> consumer) {
            this.forEach(consumer);
        }
    }

    public static interface Entry
    extends Map.Entry<Integer, Long> {
        public int getIntKey();

        @Override
        @Deprecated
        default public Integer getKey() {
            return this.getIntKey();
        }

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

