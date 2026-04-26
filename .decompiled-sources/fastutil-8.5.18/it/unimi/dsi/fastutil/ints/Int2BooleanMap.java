/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.ints;

import it.unimi.dsi.fastutil.booleans.BooleanCollection;
import it.unimi.dsi.fastutil.ints.AbstractInt2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanArrayMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanFunction;
import it.unimi.dsi.fastutil.ints.Int2BooleanMaps;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntBooleanBiConsumer;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;

public interface Int2BooleanMap
extends Int2BooleanFunction,
Map<Integer, Boolean> {
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

    public ObjectSet<Entry> int2BooleanEntrySet();

    @Override
    @Deprecated
    default public ObjectSet<Map.Entry<Integer, Boolean>> entrySet() {
        return this.int2BooleanEntrySet();
    }

    @Override
    @Deprecated
    default public Boolean put(Integer key, Boolean value) {
        return Int2BooleanFunction.super.put(key, value);
    }

    @Override
    @Deprecated
    default public Boolean get(Object key) {
        return Int2BooleanFunction.super.get(key);
    }

    @Override
    @Deprecated
    default public Boolean remove(Object key) {
        return Int2BooleanFunction.super.remove(key);
    }

    public IntSet keySet();

    public BooleanCollection values();

    @Override
    public boolean containsKey(int var1);

    @Override
    @Deprecated
    default public boolean containsKey(Object key) {
        return Int2BooleanFunction.super.containsKey(key);
    }

    public boolean containsValue(boolean var1);

    @Override
    @Deprecated
    default public boolean containsValue(Object value) {
        return value == null ? false : this.containsValue((Boolean)value);
    }

    default public void forEach(IntBooleanBiConsumer consumer) {
        ObjectSet<Entry> entrySet = this.int2BooleanEntrySet();
        Consumer<Entry> wrappingConsumer = entry -> consumer.accept(entry.getIntKey(), entry.getBooleanValue());
        if (entrySet instanceof FastEntrySet) {
            ((FastEntrySet)entrySet).fastForEach(wrappingConsumer);
        } else {
            entrySet.forEach(wrappingConsumer);
        }
    }

    @Override
    @Deprecated
    default public void forEach(BiConsumer<? super Integer, ? super Boolean> action) {
        Map.super.forEach(action);
    }

    @Override
    default public boolean getOrDefault(int key, boolean defaultValue) {
        boolean v = this.get(key);
        return v != this.defaultReturnValue() || this.containsKey(key) ? v : defaultValue;
    }

    @Override
    @Deprecated
    default public Boolean getOrDefault(Object key, Boolean defaultValue) {
        return Map.super.getOrDefault(key, defaultValue);
    }

    @Override
    default public boolean putIfAbsent(int key, boolean value) {
        boolean drv;
        boolean v = this.get(key);
        if (v != (drv = this.defaultReturnValue()) || this.containsKey(key)) {
            return v;
        }
        this.put(key, value);
        return drv;
    }

    default public boolean remove(int key, boolean value) {
        boolean curValue = this.get(key);
        if (curValue != value || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.remove(key);
        return true;
    }

    @Override
    default public boolean replace(int key, boolean oldValue, boolean newValue) {
        boolean curValue = this.get(key);
        if (curValue != oldValue || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.put(key, newValue);
        return true;
    }

    @Override
    default public boolean replace(int key, boolean value) {
        return this.containsKey(key) ? this.put(key, value) : this.defaultReturnValue();
    }

    default public boolean computeIfAbsent(int key, IntPredicate mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        boolean v = this.get(key);
        if (v != this.defaultReturnValue() || this.containsKey(key)) {
            return v;
        }
        boolean newValue = mappingFunction.test(key);
        this.put(key, newValue);
        return newValue;
    }

    default public boolean computeIfAbsentNullable(int key, IntFunction<? extends Boolean> mappingFunction) {
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

    default public boolean computeIfAbsent(int key, Int2BooleanFunction mappingFunction) {
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
    default public boolean computeIfAbsentPartial(int key, Int2BooleanFunction mappingFunction) {
        return this.computeIfAbsent(key, mappingFunction);
    }

    @Override
    default public boolean computeIfPresent(int key, BiFunction<? super Integer, ? super Boolean, ? extends Boolean> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        boolean oldValue = this.get(key);
        boolean drv = this.defaultReturnValue();
        if (oldValue == drv && !this.containsKey(key)) {
            return drv;
        }
        Boolean newValue = remappingFunction.apply((Integer)key, (Boolean)oldValue);
        if (newValue == null) {
            this.remove(key);
            return drv;
        }
        boolean newVal = newValue;
        this.put(key, newVal);
        return newVal;
    }

    @Override
    default public boolean compute(int key, BiFunction<? super Integer, ? super Boolean, ? extends Boolean> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        boolean oldValue = this.get(key);
        boolean drv = this.defaultReturnValue();
        boolean contained = oldValue != drv || this.containsKey(key);
        Boolean newValue = remappingFunction.apply((Integer)key, contained ? Boolean.valueOf(oldValue) : null);
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
    default public boolean merge(int key, boolean value, BiFunction<? super Boolean, ? super Boolean, ? extends Boolean> remappingFunction) {
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

    public static Int2BooleanMap ofEntries(Entry ... entries) {
        if (entries.length == 0) {
            return Int2BooleanMaps.EMPTY_MAP;
        }
        if (entries.length == 1) {
            return Int2BooleanMaps.singleton(entries[0].getIntKey(), entries[0].getBooleanValue());
        }
        if (entries.length <= 8) {
            int[] keys = new int[entries.length];
            boolean[] vals = new boolean[entries.length];
            for (int i = 0; i < entries.length; ++i) {
                Entry entry = entries[i];
                keys[i] = entry.getIntKey();
                for (int j = 0; j < i; ++j) {
                    if (keys[j] != keys[i]) continue;
                    throw new IllegalArgumentException("duplicate key: " + keys[i]);
                }
                vals[i] = entry.getBooleanValue();
            }
            return Int2BooleanMaps.unmodifiable(new Int2BooleanArrayMap(keys, vals, entries.length));
        }
        Int2BooleanOpenHashMap newMap = new Int2BooleanOpenHashMap(entries.length, 0.75f);
        for (Entry entry : entries) {
            if (!newMap.put(entry.getIntKey(), entry.getBooleanValue())) continue;
            throw new IllegalArgumentException("duplicate key: " + entry.getIntKey());
        }
        return Int2BooleanMaps.unmodifiable(newMap);
    }

    public static Entry entry(int key, boolean value) {
        return new AbstractInt2BooleanMap.BasicEntry(key, value);
    }

    public static interface FastEntrySet
    extends ObjectSet<Entry> {
        public ObjectIterator<Entry> fastIterator();

        default public void fastForEach(Consumer<? super Entry> consumer) {
            this.forEach(consumer);
        }
    }

    public static interface Entry
    extends Map.Entry<Integer, Boolean> {
        public int getIntKey();

        @Override
        @Deprecated
        default public Integer getKey() {
            return this.getIntKey();
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

