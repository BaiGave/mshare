/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.longs;

import it.unimi.dsi.fastutil.booleans.BooleanCollection;
import it.unimi.dsi.fastutil.longs.AbstractLong2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanArrayMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanFunction;
import it.unimi.dsi.fastutil.longs.Long2BooleanMaps;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongBooleanBiConsumer;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;

public interface Long2BooleanMap
extends Long2BooleanFunction,
Map<Long, Boolean> {
    @Override
    public int size();

    @Override
    default public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void defaultReturnValue(boolean var1);

    @Override
    public boolean defaultReturnValue();

    public ObjectSet<Entry> long2BooleanEntrySet();

    @Override
    @Deprecated
    default public ObjectSet<Map.Entry<Long, Boolean>> entrySet() {
        return this.long2BooleanEntrySet();
    }

    @Override
    @Deprecated
    default public Boolean put(Long key, Boolean value) {
        return Long2BooleanFunction.super.put(key, value);
    }

    @Override
    @Deprecated
    default public Boolean get(Object key) {
        return Long2BooleanFunction.super.get(key);
    }

    @Override
    @Deprecated
    default public Boolean remove(Object key) {
        return Long2BooleanFunction.super.remove(key);
    }

    public LongSet keySet();

    public BooleanCollection values();

    @Override
    public boolean containsKey(long var1);

    @Override
    @Deprecated
    default public boolean containsKey(Object key) {
        return Long2BooleanFunction.super.containsKey(key);
    }

    public boolean containsValue(boolean var1);

    @Override
    @Deprecated
    default public boolean containsValue(Object value) {
        return value == null ? false : this.containsValue((Boolean)value);
    }

    default public void forEach(LongBooleanBiConsumer consumer) {
        ObjectSet<Entry> entrySet = this.long2BooleanEntrySet();
        Consumer<Entry> wrappingConsumer = entry -> consumer.accept(entry.getLongKey(), entry.getBooleanValue());
        if (entrySet instanceof FastEntrySet) {
            ((FastEntrySet)entrySet).fastForEach(wrappingConsumer);
        } else {
            entrySet.forEach(wrappingConsumer);
        }
    }

    @Override
    @Deprecated
    default public void forEach(BiConsumer<? super Long, ? super Boolean> action) {
        Map.super.forEach(action);
    }

    @Override
    default public boolean getOrDefault(long key, boolean defaultValue) {
        boolean v = this.get(key);
        return v != this.defaultReturnValue() || this.containsKey(key) ? v : defaultValue;
    }

    @Override
    @Deprecated
    default public Boolean getOrDefault(Object key, Boolean defaultValue) {
        return Map.super.getOrDefault(key, defaultValue);
    }

    @Override
    default public boolean putIfAbsent(long key, boolean value) {
        boolean drv;
        boolean v = this.get(key);
        if (v != (drv = this.defaultReturnValue()) || this.containsKey(key)) {
            return v;
        }
        this.put(key, value);
        return drv;
    }

    default public boolean remove(long key, boolean value) {
        boolean curValue = this.get(key);
        if (curValue != value || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.remove(key);
        return true;
    }

    @Override
    default public boolean replace(long key, boolean oldValue, boolean newValue) {
        boolean curValue = this.get(key);
        if (curValue != oldValue || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.put(key, newValue);
        return true;
    }

    @Override
    default public boolean replace(long key, boolean value) {
        return this.containsKey(key) ? this.put(key, value) : this.defaultReturnValue();
    }

    default public boolean computeIfAbsent(long key, LongPredicate mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        boolean v = this.get(key);
        if (v != this.defaultReturnValue() || this.containsKey(key)) {
            return v;
        }
        boolean newValue = mappingFunction.test(key);
        this.put(key, newValue);
        return newValue;
    }

    default public boolean computeIfAbsentNullable(long key, LongFunction<? extends Boolean> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        boolean v = this.get(key);
        boolean drv = this.defaultReturnValue();
        if (v != drv || this.containsKey(key)) {
            return v;
        }
        Boolean mappedValue = mappingFunction.apply(key);
        if (mappedValue == null) {
            return drv;
        }
        boolean newValue = mappedValue;
        this.put(key, newValue);
        return newValue;
    }

    default public boolean computeIfAbsent(long key, Long2BooleanFunction mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        boolean v = this.get(key);
        boolean drv = this.defaultReturnValue();
        if (v != drv || this.containsKey(key)) {
            return v;
        }
        if (!mappingFunction.containsKey(key)) {
            return drv;
        }
        boolean newValue = mappingFunction.get(key);
        this.put(key, newValue);
        return newValue;
    }

    @Deprecated
    default public boolean computeIfAbsentPartial(long key, Long2BooleanFunction mappingFunction) {
        return this.computeIfAbsent(key, mappingFunction);
    }

    @Override
    default public boolean computeIfPresent(long key, BiFunction<? super Long, ? super Boolean, ? extends Boolean> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        boolean oldValue = this.get(key);
        boolean drv = this.defaultReturnValue();
        if (oldValue == drv && !this.containsKey(key)) {
            return drv;
        }
        Boolean newValue = remappingFunction.apply((Long)key, (Boolean)oldValue);
        if (newValue == null) {
            this.remove(key);
            return drv;
        }
        boolean newVal = newValue;
        this.put(key, newVal);
        return newVal;
    }

    @Override
    default public boolean compute(long key, BiFunction<? super Long, ? super Boolean, ? extends Boolean> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        boolean oldValue = this.get(key);
        boolean drv = this.defaultReturnValue();
        boolean contained = oldValue != drv || this.containsKey(key);
        Boolean newValue = remappingFunction.apply((Long)key, contained ? Boolean.valueOf(oldValue) : null);
        if (newValue == null) {
            if (contained) {
                this.remove(key);
            }
            return drv;
        }
        boolean newVal = newValue;
        this.put(key, newVal);
        return newVal;
    }

    @Override
    default public boolean merge(long key, boolean value, BiFunction<? super Boolean, ? super Boolean, ? extends Boolean> remappingFunction) {
        boolean newValue;
        Objects.requireNonNull(remappingFunction);
        boolean oldValue = this.get(key);
        boolean drv = this.defaultReturnValue();
        if (oldValue != drv || this.containsKey(key)) {
            Boolean mergedValue = remappingFunction.apply((Boolean)oldValue, (Boolean)value);
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

    public static Long2BooleanMap ofEntries(Entry ... entries) {
        if (entries.length == 0) {
            return Long2BooleanMaps.EMPTY_MAP;
        }
        if (entries.length == 1) {
            return Long2BooleanMaps.singleton(entries[0].getLongKey(), entries[0].getBooleanValue());
        }
        if (entries.length <= 8) {
            long[] keys = new long[entries.length];
            boolean[] vals = new boolean[entries.length];
            for (int i = 0; i < entries.length; ++i) {
                Entry entry = entries[i];
                keys[i] = entry.getLongKey();
                for (int j = 0; j < i; ++j) {
                    if (keys[j] != keys[i]) continue;
                    throw new IllegalArgumentException("duplicate key: " + keys[i]);
                }
                vals[i] = entry.getBooleanValue();
            }
            return Long2BooleanMaps.unmodifiable(new Long2BooleanArrayMap(keys, vals, entries.length));
        }
        Long2BooleanOpenHashMap newMap = new Long2BooleanOpenHashMap(entries.length, 0.75f);
        for (Entry entry : entries) {
            if (!newMap.put(entry.getLongKey(), entry.getBooleanValue())) continue;
            throw new IllegalArgumentException("duplicate key: " + entry.getLongKey());
        }
        return Long2BooleanMaps.unmodifiable(newMap);
    }

    public static Entry entry(long key, boolean value) {
        return new AbstractLong2BooleanMap.BasicEntry(key, value);
    }

    public static interface FastEntrySet
    extends ObjectSet<Entry> {
        public ObjectIterator<Entry> fastIterator();

        default public void fastForEach(Consumer<? super Entry> consumer) {
            this.forEach(consumer);
        }
    }

    public static interface Entry
    extends Map.Entry<Long, Boolean> {
        public long getLongKey();

        @Override
        @Deprecated
        default public Long getKey() {
            return this.getLongKey();
        }

        public boolean getBooleanValue();

        @Override
        public boolean setValue(boolean var1);

        @Override
        @Deprecated
        default public Boolean getValue() {
            return this.getBooleanValue();
        }

        @Override
        @Deprecated
        default public Boolean setValue(Boolean value) {
            return this.setValue((boolean)value);
        }
    }
}

