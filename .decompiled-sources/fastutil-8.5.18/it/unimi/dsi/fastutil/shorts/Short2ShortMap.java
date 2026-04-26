/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.shorts;

import it.unimi.dsi.fastutil.SafeMath;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.shorts.AbstractShort2ShortMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortArrayMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortFunction;
import it.unimi.dsi.fastutil.shorts.Short2ShortMaps;
import it.unimi.dsi.fastutil.shorts.Short2ShortOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortBinaryOperator;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import it.unimi.dsi.fastutil.shorts.ShortShortBiConsumer;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

public interface Short2ShortMap
extends Short2ShortFunction,
Map<Short, Short> {
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

    public ObjectSet<Entry> short2ShortEntrySet();

    @Override
    @Deprecated
    default public ObjectSet<Map.Entry<Short, Short>> entrySet() {
        return this.short2ShortEntrySet();
    }

    @Override
    @Deprecated
    default public Short put(Short key, Short value) {
        return Short2ShortFunction.super.put(key, value);
    }

    @Override
    @Deprecated
    default public Short get(Object key) {
        return Short2ShortFunction.super.get(key);
    }

    @Override
    @Deprecated
    default public Short remove(Object key) {
        return Short2ShortFunction.super.remove(key);
    }

    public ShortSet keySet();

    public ShortCollection values();

    @Override
    public boolean containsKey(short var1);

    @Override
    @Deprecated
    default public boolean containsKey(Object key) {
        return Short2ShortFunction.super.containsKey(key);
    }

    public boolean containsValue(short var1);

    @Override
    @Deprecated
    default public boolean containsValue(Object value) {
        return value == null ? false : this.containsValue((Short)value);
    }

    default public void forEach(ShortShortBiConsumer consumer) {
        ObjectSet<Entry> entrySet = this.short2ShortEntrySet();
        Consumer<Entry> wrappingConsumer = entry -> consumer.accept(entry.getShortKey(), entry.getShortValue());
        if (entrySet instanceof FastEntrySet) {
            ((FastEntrySet)entrySet).fastForEach(wrappingConsumer);
        } else {
            entrySet.forEach(wrappingConsumer);
        }
    }

    @Override
    @Deprecated
    default public void forEach(BiConsumer<? super Short, ? super Short> action) {
        Map.super.forEach(action);
    }

    @Override
    default public short getOrDefault(short key, short defaultValue) {
        short v = this.get(key);
        return v != this.defaultReturnValue() || this.containsKey(key) ? v : defaultValue;
    }

    @Override
    @Deprecated
    default public Short getOrDefault(Object key, Short defaultValue) {
        return Map.super.getOrDefault(key, defaultValue);
    }

    @Override
    default public short putIfAbsent(short key, short value) {
        short drv;
        short v = this.get(key);
        if (v != (drv = this.defaultReturnValue()) || this.containsKey(key)) {
            return v;
        }
        this.put(key, value);
        return drv;
    }

    default public boolean remove(short key, short value) {
        short curValue = this.get(key);
        if (curValue != value || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.remove(key);
        return true;
    }

    @Override
    default public boolean replace(short key, short oldValue, short newValue) {
        short curValue = this.get(key);
        if (curValue != oldValue || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.put(key, newValue);
        return true;
    }

    @Override
    default public short replace(short key, short value) {
        return this.containsKey(key) ? this.put(key, value) : this.defaultReturnValue();
    }

    default public short computeIfAbsent(short key, IntUnaryOperator mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        short v = this.get(key);
        if (v != this.defaultReturnValue() || this.containsKey(key)) {
            return v;
        }
        short newValue = SafeMath.safeIntToShort(mappingFunction.applyAsInt(key));
        this.put(key, newValue);
        return newValue;
    }

    default public short computeIfAbsentNullable(short key, IntFunction<? extends Short> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        short v = this.get(key);
        short drv = this.defaultReturnValue();
        if (v != drv || this.containsKey(key)) {
            return v;
        }
        Short mappedValue = mappingFunction.apply(key);
        if (mappedValue == null) {
            return drv;
        }
        short newValue = mappedValue;
        this.put(key, newValue);
        return newValue;
    }

    default public short computeIfAbsent(short key, Short2ShortFunction mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        short v = this.get(key);
        short drv = this.defaultReturnValue();
        if (v != drv || this.containsKey(key)) {
            return v;
        }
        if (!mappingFunction.containsKey(key)) {
            return drv;
        }
        short newValue = mappingFunction.get(key);
        this.put(key, newValue);
        return newValue;
    }

    @Deprecated
    default public short computeIfAbsentPartial(short key, Short2ShortFunction mappingFunction) {
        return this.computeIfAbsent(key, mappingFunction);
    }

    @Override
    default public short computeIfPresent(short key, BiFunction<? super Short, ? super Short, ? extends Short> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        short oldValue = this.get(key);
        short drv = this.defaultReturnValue();
        if (oldValue == drv && !this.containsKey(key)) {
            return drv;
        }
        Short newValue = remappingFunction.apply((Short)key, (Short)oldValue);
        if (newValue == null) {
            this.remove(key);
            return drv;
        }
        short newVal = newValue;
        this.put(key, newVal);
        return newVal;
    }

    @Override
    default public short compute(short key, BiFunction<? super Short, ? super Short, ? extends Short> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        short oldValue = this.get(key);
        short drv = this.defaultReturnValue();
        boolean contained = oldValue != drv || this.containsKey(key);
        Short newValue = remappingFunction.apply((Short)key, contained ? Short.valueOf(oldValue) : null);
        if (newValue == null) {
            if (contained) {
                this.remove(key);
            }
            return drv;
        }
        short newVal = newValue;
        this.put(key, newVal);
        return newVal;
    }

    @Override
    default public short merge(short key, short value, BiFunction<? super Short, ? super Short, ? extends Short> remappingFunction) {
        short newValue;
        Objects.requireNonNull(remappingFunction);
        short oldValue = this.get(key);
        short drv = this.defaultReturnValue();
        if (oldValue != drv || this.containsKey(key)) {
            Short mergedValue = remappingFunction.apply((Short)oldValue, (Short)value);
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

    default public short mergeShort(short key, short value, ShortBinaryOperator remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        short oldValue = this.get(key);
        short drv = this.defaultReturnValue();
        short newValue = oldValue != drv || this.containsKey(key) ? remappingFunction.apply(oldValue, value) : value;
        this.put(key, newValue);
        return newValue;
    }

    default public short mergeShort(short key, short value, IntBinaryOperator remappingFunction) {
        return this.mergeShort(key, value, remappingFunction instanceof ShortBinaryOperator ? (ShortBinaryOperator)remappingFunction : (x, y) -> SafeMath.safeIntToShort(remappingFunction.applyAsInt(x, y)));
    }

    @Override
    @Deprecated
    default public Short putIfAbsent(Short key, Short value) {
        return Map.super.putIfAbsent(key, value);
    }

    @Override
    @Deprecated
    default public boolean remove(Object key, Object value) {
        return Map.super.remove(key, value);
    }

    @Override
    @Deprecated
    default public boolean replace(Short key, Short oldValue, Short newValue) {
        return Map.super.replace(key, oldValue, newValue);
    }

    @Override
    @Deprecated
    default public Short replace(Short key, Short value) {
        return Map.super.replace(key, value);
    }

    @Override
    @Deprecated
    default public Short computeIfAbsent(Short key, Function<? super Short, ? extends Short> mappingFunction) {
        return Map.super.computeIfAbsent(key, mappingFunction);
    }

    @Override
    @Deprecated
    default public Short computeIfPresent(Short key, BiFunction<? super Short, ? super Short, ? extends Short> remappingFunction) {
        return Map.super.computeIfPresent(key, remappingFunction);
    }

    @Override
    @Deprecated
    default public Short compute(Short key, BiFunction<? super Short, ? super Short, ? extends Short> remappingFunction) {
        return Map.super.compute(key, remappingFunction);
    }

    @Override
    @Deprecated
    default public Short merge(Short key, Short value, BiFunction<? super Short, ? super Short, ? extends Short> remappingFunction) {
        return Map.super.merge(key, value, remappingFunction);
    }

    public static Short2ShortMap ofEntries(Entry ... entries) {
        if (entries.length == 0) {
            return Short2ShortMaps.EMPTY_MAP;
        }
        if (entries.length == 1) {
            return Short2ShortMaps.singleton(entries[0].getShortKey(), entries[0].getShortValue());
        }
        if (entries.length <= 8) {
            short[] keys = new short[entries.length];
            short[] vals = new short[entries.length];
            for (int i = 0; i < entries.length; ++i) {
                Entry entry = entries[i];
                keys[i] = entry.getShortKey();
                for (int j = 0; j < i; ++j) {
                    if (keys[j] != keys[i]) continue;
                    throw new IllegalArgumentException("duplicate key: " + keys[i]);
                }
                vals[i] = entry.getShortValue();
            }
            return Short2ShortMaps.unmodifiable(new Short2ShortArrayMap(keys, vals, entries.length));
        }
        Short2ShortOpenHashMap newMap = new Short2ShortOpenHashMap(entries.length, 0.75f);
        for (Entry entry : entries) {
            if (newMap.put(entry.getShortKey(), entry.getShortValue()) == 0) continue;
            throw new IllegalArgumentException("duplicate key: " + entry.getShortKey());
        }
        return Short2ShortMaps.unmodifiable(newMap);
    }

    public static Entry entry(short key, short value) {
        return new AbstractShort2ShortMap.BasicEntry(key, value);
    }

    public static interface FastEntrySet
    extends ObjectSet<Entry> {
        public ObjectIterator<Entry> fastIterator();

        default public void fastForEach(Consumer<? super Entry> consumer) {
            this.forEach(consumer);
        }
    }

    public static interface Entry
    extends Map.Entry<Short, Short> {
        public short getShortKey();

        @Override
        @Deprecated
        default public Short getKey() {
            return this.getShortKey();
        }

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

