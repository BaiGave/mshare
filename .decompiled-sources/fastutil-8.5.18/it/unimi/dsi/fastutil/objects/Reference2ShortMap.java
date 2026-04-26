/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.objects;

import it.unimi.dsi.fastutil.SafeMath;
import it.unimi.dsi.fastutil.objects.AbstractReference2ShortMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectShortBiConsumer;
import it.unimi.dsi.fastutil.objects.Reference2ShortArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ShortFunction;
import it.unimi.dsi.fastutil.objects.Reference2ShortMaps;
import it.unimi.dsi.fastutil.objects.Reference2ShortOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.shorts.ShortBinaryOperator;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;
import java.util.function.ToIntFunction;

public interface Reference2ShortMap<K>
extends Reference2ShortFunction<K>,
Map<K, Short> {
    @Override
    public int size();

    @Override
    default public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void defaultReturnValue(short var1);

    @Override
    public short defaultReturnValue();

    public ObjectSet<Entry<K>> reference2ShortEntrySet();

    @Override
    @Deprecated
    default public ObjectSet<Map.Entry<K, Short>> entrySet() {
        return this.reference2ShortEntrySet();
    }

    @Override
    @Deprecated
    default public Short put(K key, Short value) {
        return Reference2ShortFunction.super.put(key, value);
    }

    @Override
    @Deprecated
    default public Short get(Object key) {
        return Reference2ShortFunction.super.get(key);
    }

    @Override
    @Deprecated
    default public Short remove(Object key) {
        return Reference2ShortFunction.super.remove(key);
    }

    @Override
    public ReferenceSet<K> keySet();

    public ShortCollection values();

    @Override
    public boolean containsKey(Object var1);

    public boolean containsValue(short var1);

    @Override
    @Deprecated
    default public boolean containsValue(Object value) {
        return value == null ? false : this.containsValue((Short)value);
    }

    default public void forEach(ObjectShortBiConsumer<? super K> consumer) {
        ObjectSet<Entry<K>> entrySet = this.reference2ShortEntrySet();
        Consumer<Entry> wrappingConsumer = entry -> consumer.accept((Object)entry.getKey(), entry.getShortValue());
        if (entrySet instanceof FastEntrySet) {
            ((FastEntrySet)entrySet).fastForEach(wrappingConsumer);
        } else {
            entrySet.forEach(wrappingConsumer);
        }
    }

    @Override
    @Deprecated
    default public void forEach(BiConsumer<? super K, ? super Short> action) {
        Map.super.forEach(action);
    }

    @Override
    default public short getOrDefault(Object key, short defaultValue) {
        short v = this.getShort(key);
        return v != this.defaultReturnValue() || this.containsKey(key) ? v : defaultValue;
    }

    @Override
    @Deprecated
    default public Short getOrDefault(Object key, Short defaultValue) {
        return Map.super.getOrDefault(key, defaultValue);
    }

    @Override
    default public short putIfAbsent(K key, short value) {
        short drv;
        short v = this.getShort(key);
        if (v != (drv = this.defaultReturnValue()) || this.containsKey(key)) {
            return v;
        }
        this.put(key, value);
        return drv;
    }

    default public boolean remove(Object key, short value) {
        short curValue = this.getShort(key);
        if (curValue != value || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.removeShort(key);
        return true;
    }

    @Override
    default public boolean replace(K key, short oldValue, short newValue) {
        short curValue = this.getShort(key);
        if (curValue != oldValue || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.put(key, newValue);
        return true;
    }

    @Override
    default public short replace(K key, short value) {
        return this.containsKey(key) ? this.put(key, value) : this.defaultReturnValue();
    }

    default public short computeIfAbsent(K key, ToIntFunction<? super K> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        short v = this.getShort(key);
        if (v != this.defaultReturnValue() || this.containsKey(key)) {
            return v;
        }
        short newValue = SafeMath.safeIntToShort(mappingFunction.applyAsInt(key));
        this.put(key, newValue);
        return newValue;
    }

    @Deprecated
    default public short computeShortIfAbsent(K key, ToIntFunction<? super K> mappingFunction) {
        return this.computeIfAbsent(key, mappingFunction);
    }

    default public short computeIfAbsent(K key, Reference2ShortFunction<? super K> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        short v = this.getShort(key);
        short drv = this.defaultReturnValue();
        if (v != drv || this.containsKey(key)) {
            return v;
        }
        if (!mappingFunction.containsKey(key)) {
            return drv;
        }
        short newValue = mappingFunction.getShort(key);
        this.put(key, newValue);
        return newValue;
    }

    @Deprecated
    default public short computeShortIfAbsentPartial(K key, Reference2ShortFunction<? super K> mappingFunction) {
        return this.computeIfAbsent(key, mappingFunction);
    }

    default public short computeShortIfPresent(K key, BiFunction<? super K, ? super Short, ? extends Short> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        short oldValue = this.getShort(key);
        short drv = this.defaultReturnValue();
        if (oldValue == drv && !this.containsKey(key)) {
            return drv;
        }
        Short newValue = remappingFunction.apply(key, oldValue);
        if (newValue == null) {
            this.removeShort(key);
            return drv;
        }
        short newVal = newValue;
        this.put(key, newVal);
        return newVal;
    }

    default public short computeShort(K key, BiFunction<? super K, ? super Short, ? extends Short> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        short oldValue = this.getShort(key);
        short drv = this.defaultReturnValue();
        boolean contained = oldValue != drv || this.containsKey(key);
        Short newValue = remappingFunction.apply(key, contained ? Short.valueOf(oldValue) : null);
        if (newValue == null) {
            if (contained) {
                this.removeShort(key);
            }
            return drv;
        }
        short newVal = newValue;
        this.put(key, newVal);
        return newVal;
    }

    @Override
    default public short merge(K key, short value, BiFunction<? super Short, ? super Short, ? extends Short> remappingFunction) {
        short newValue;
        Objects.requireNonNull(remappingFunction);
        short oldValue = this.getShort(key);
        short drv = this.defaultReturnValue();
        if (oldValue != drv || this.containsKey(key)) {
            Short mergedValue = remappingFunction.apply((Short)oldValue, (Short)value);
            if (mergedValue == null) {
                this.removeShort(key);
                return drv;
            }
            newValue = mergedValue;
        } else {
            newValue = value;
        }
        this.put(key, newValue);
        return newValue;
    }

    default public short mergeShort(K key, short value, ShortBinaryOperator remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        short oldValue = this.getShort(key);
        short drv = this.defaultReturnValue();
        short newValue = oldValue != drv || this.containsKey(key) ? remappingFunction.apply(oldValue, value) : value;
        this.put(key, newValue);
        return newValue;
    }

    default public short mergeShort(K key, short value, IntBinaryOperator remappingFunction) {
        return this.mergeShort(key, value, remappingFunction instanceof ShortBinaryOperator ? (ShortBinaryOperator)remappingFunction : (x, y) -> SafeMath.safeIntToShort(remappingFunction.applyAsInt(x, y)));
    }

    @Deprecated
    default public short mergeShort(K key, short value, BiFunction<? super Short, ? super Short, ? extends Short> remappingFunction) {
        return this.merge(key, value, remappingFunction);
    }

    @Override
    @Deprecated
    default public Short putIfAbsent(K key, Short value) {
        return Map.super.putIfAbsent(key, value);
    }

    @Override
    @Deprecated
    default public boolean remove(Object key, Object value) {
        return Map.super.remove(key, value);
    }

    @Override
    @Deprecated
    default public boolean replace(K key, Short oldValue, Short newValue) {
        return Map.super.replace(key, oldValue, newValue);
    }

    @Override
    @Deprecated
    default public Short replace(K key, Short value) {
        return Map.super.replace(key, value);
    }

    @Override
    @Deprecated
    default public Short merge(K key, Short value, BiFunction<? super Short, ? super Short, ? extends Short> remappingFunction) {
        return Map.super.merge(key, value, remappingFunction);
    }

    @SafeVarargs
    public static <K> Reference2ShortMap<K> ofEntries(Entry<K> ... entries) {
        if (entries.length == 0) {
            return Reference2ShortMaps.EMPTY_MAP;
        }
        if (entries.length == 1) {
            return Reference2ShortMaps.singleton(entries[0].getKey(), entries[0].getShortValue());
        }
        if (entries.length <= 8) {
            Object[] keys = new Object[entries.length];
            short[] vals = new short[entries.length];
            for (int i = 0; i < entries.length; ++i) {
                Entry<K> entry = entries[i];
                keys[i] = entry.getKey();
                for (int j = 0; j < i; ++j) {
                    if (keys[j] != keys[i]) continue;
                    throw new IllegalArgumentException("duplicate key: " + keys[i]);
                }
                vals[i] = entry.getShortValue();
            }
            return Reference2ShortMaps.unmodifiable(new Reference2ShortArrayMap(keys, vals, entries.length));
        }
        Reference2ShortOpenHashMap newMap = new Reference2ShortOpenHashMap(entries.length, 0.75f);
        for (Entry<K> entry : entries) {
            if (newMap.put(entry.getKey(), entry.getShortValue()) == 0) continue;
            throw new IllegalArgumentException("duplicate key: " + entry.getKey());
        }
        return Reference2ShortMaps.unmodifiable(newMap);
    }

    public static <K> Entry<K> entry(K key, short value) {
        return new AbstractReference2ShortMap.BasicEntry<K>(key, value);
    }

    public static interface FastEntrySet<K>
    extends ObjectSet<Entry<K>> {
        public ObjectIterator<Entry<K>> fastIterator();

        default public void fastForEach(Consumer<? super Entry<K>> consumer) {
            this.forEach(consumer);
        }
    }

    public static interface Entry<K>
    extends Map.Entry<K, Short> {
        public short getShortValue();

        @Override
        public short setValue(short var1);

        @Override
        @Deprecated
        default public Short getValue() {
            return this.getShortValue();
        }

        @Override
        @Deprecated
        default public Short setValue(Short value) {
            return this.setValue((short)value);
        }
    }
}

