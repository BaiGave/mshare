/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.doubles;

import it.unimi.dsi.fastutil.SafeMath;
import it.unimi.dsi.fastutil.doubles.AbstractDouble2ShortMap;
import it.unimi.dsi.fastutil.doubles.Double2ShortArrayMap;
import it.unimi.dsi.fastutil.doubles.Double2ShortFunction;
import it.unimi.dsi.fastutil.doubles.Double2ShortMaps;
import it.unimi.dsi.fastutil.doubles.Double2ShortOpenHashMap;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.doubles.DoubleShortBiConsumer;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.shorts.ShortBinaryOperator;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleToIntFunction;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;

public interface Double2ShortMap
extends Double2ShortFunction,
Map<Double, Short> {
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

    public ObjectSet<Entry> double2ShortEntrySet();

    @Override
    @Deprecated
    default public ObjectSet<Map.Entry<Double, Short>> entrySet() {
        return this.double2ShortEntrySet();
    }

    @Override
    @Deprecated
    default public Short put(Double key, Short value) {
        return Double2ShortFunction.super.put(key, value);
    }

    @Override
    @Deprecated
    default public Short get(Object key) {
        return Double2ShortFunction.super.get(key);
    }

    @Override
    @Deprecated
    default public Short remove(Object key) {
        return Double2ShortFunction.super.remove(key);
    }

    public DoubleSet keySet();

    public ShortCollection values();

    @Override
    public boolean containsKey(double var1);

    @Override
    @Deprecated
    default public boolean containsKey(Object key) {
        return Double2ShortFunction.super.containsKey(key);
    }

    public boolean containsValue(short var1);

    @Override
    @Deprecated
    default public boolean containsValue(Object value) {
        return value == null ? false : this.containsValue((Short)value);
    }

    default public void forEach(DoubleShortBiConsumer consumer) {
        ObjectSet<Entry> entrySet = this.double2ShortEntrySet();
        Consumer<Entry> wrappingConsumer = entry -> consumer.accept(entry.getDoubleKey(), entry.getShortValue());
        if (entrySet instanceof FastEntrySet) {
            ((FastEntrySet)entrySet).fastForEach(wrappingConsumer);
        } else {
            entrySet.forEach(wrappingConsumer);
        }
    }

    @Override
    @Deprecated
    default public void forEach(BiConsumer<? super Double, ? super Short> action) {
        Map.super.forEach(action);
    }

    @Override
    default public short getOrDefault(double key, short defaultValue) {
        short v = this.get(key);
        return v != this.defaultReturnValue() || this.containsKey(key) ? v : defaultValue;
    }

    @Override
    @Deprecated
    default public Short getOrDefault(Object key, Short defaultValue) {
        return Map.super.getOrDefault(key, defaultValue);
    }

    @Override
    default public short putIfAbsent(double key, short value) {
        short drv;
        short v = this.get(key);
        if (v != (drv = this.defaultReturnValue()) || this.containsKey(key)) {
            return v;
        }
        this.put(key, value);
        return drv;
    }

    default public boolean remove(double key, short value) {
        short curValue = this.get(key);
        if (curValue != value || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.remove(key);
        return true;
    }

    @Override
    default public boolean replace(double key, short oldValue, short newValue) {
        short curValue = this.get(key);
        if (curValue != oldValue || curValue == this.defaultReturnValue() && !this.containsKey(key)) {
            return false;
        }
        this.put(key, newValue);
        return true;
    }

    @Override
    default public short replace(double key, short value) {
        return this.containsKey(key) ? this.put(key, value) : this.defaultReturnValue();
    }

    default public short computeIfAbsent(double key, DoubleToIntFunction mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        short v = this.get(key);
        if (v != this.defaultReturnValue() || this.containsKey(key)) {
            return v;
        }
        short newValue = SafeMath.safeIntToShort(mappingFunction.applyAsInt(key));
        this.put(key, newValue);
        return newValue;
    }

    default public short computeIfAbsentNullable(double key, DoubleFunction<? extends Short> mappingFunction) {
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

    default public short computeIfAbsent(double key, Double2ShortFunction mappingFunction) {
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
    default public short computeIfAbsentPartial(double key, Double2ShortFunction mappingFunction) {
        return this.computeIfAbsent(key, mappingFunction);
    }

    @Override
    default public short computeIfPresent(double key, BiFunction<? super Double, ? super Short, ? extends Short> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        short oldValue = this.get(key);
        short drv = this.defaultReturnValue();
        if (oldValue == drv && !this.containsKey(key)) {
            return drv;
        }
        Short newValue = remappingFunction.apply((Double)key, (Short)oldValue);
        if (newValue == null) {
            this.remove(key);
            return drv;
        }
        short newVal = newValue;
        this.put(key, newVal);
        return newVal;
    }

    @Override
    default public short compute(double key, BiFunction<? super Double, ? super Short, ? extends Short> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        short oldValue = this.get(key);
        short drv = this.defaultReturnValue();
        boolean contained = oldValue != drv || this.containsKey(key);
        Short newValue = remappingFunction.apply((Double)key, contained ? Short.valueOf(oldValue) : null);
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
    default public short merge(double key, short value, BiFunction<? super Short, ? super Short, ? extends Short> remappingFunction) {
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

    default public short mergeShort(double key, short value, ShortBinaryOperator remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        short oldValue = this.get(key);
        short drv = this.defaultReturnValue();
        short newValue = oldValue != drv || this.containsKey(key) ? remappingFunction.apply(oldValue, value) : value;
        this.put(key, newValue);
        return newValue;
    }

    default public short mergeShort(double key, short value, IntBinaryOperator remappingFunction) {
        return this.mergeShort(key, value, remappingFunction instanceof ShortBinaryOperator ? (ShortBinaryOperator)remappingFunction : (x, y) -> SafeMath.safeIntToShort(remappingFunction.applyAsInt(x, y)));
    }

    @Override
    @Deprecated
    default public Short putIfAbsent(Double key, Short value) {
        return Map.super.putIfAbsent(key, value);
    }

    @Override
    @Deprecated
    default public boolean remove(Object key, Object value) {
        return Map.super.remove(key, value);
    }

    @Override
    @Deprecated
    default public boolean replace(Double key, Short oldValue, Short newValue) {
        return Map.super.replace(key, oldValue, newValue);
    }

    @Override
    @Deprecated
    default public Short replace(Double key, Short value) {
        return Map.super.replace(key, value);
    }

    @Override
    @Deprecated
    default public Short computeIfAbsent(Double key, Function<? super Double, ? extends Short> mappingFunction) {
        return Map.super.computeIfAbsent(key, mappingFunction);
    }

    @Override
    @Deprecated
    default public Short computeIfPresent(Double key, BiFunction<? super Double, ? super Short, ? extends Short> remappingFunction) {
        return Map.super.computeIfPresent(key, remappingFunction);
    }

    @Override
    @Deprecated
    default public Short compute(Double key, BiFunction<? super Double, ? super Short, ? extends Short> remappingFunction) {
        return Map.super.compute(key, remappingFunction);
    }

    @Override
    @Deprecated
    default public Short merge(Double key, Short value, BiFunction<? super Short, ? super Short, ? extends Short> remappingFunction) {
        return Map.super.merge(key, value, remappingFunction);
    }

    public static Double2ShortMap ofEntries(Entry ... entries) {
        if (entries.length == 0) {
            return Double2ShortMaps.EMPTY_MAP;
        }
        if (entries.length == 1) {
            return Double2ShortMaps.singleton(entries[0].getDoubleKey(), entries[0].getShortValue());
        }
        if (entries.length <= 8) {
            double[] keys = new double[entries.length];
            short[] vals = new short[entries.length];
            for (int i = 0; i < entries.length; ++i) {
                Entry entry = entries[i];
                keys[i] = entry.getDoubleKey();
                for (int j = 0; j < i; ++j) {
                    if (Double.doubleToRawLongBits(keys[j]) != Double.doubleToRawLongBits(keys[i])) continue;
                    throw new IllegalArgumentException("duplicate key: " + keys[i]);
                }
                vals[i] = entry.getShortValue();
            }
            return Double2ShortMaps.unmodifiable(new Double2ShortArrayMap(keys, vals, entries.length));
        }
        Double2ShortOpenHashMap newMap = new Double2ShortOpenHashMap(entries.length, 0.75f);
        for (Entry entry : entries) {
            if (newMap.put(entry.getDoubleKey(), entry.getShortValue()) == 0) continue;
            throw new IllegalArgumentException("duplicate key: " + entry.getDoubleKey());
        }
        return Double2ShortMaps.unmodifiable(newMap);
    }

    public static Entry entry(double key, short value) {
        return new AbstractDouble2ShortMap.BasicEntry(key, value);
    }

    public static interface FastEntrySet
    extends ObjectSet<Entry> {
        public ObjectIterator<Entry> fastIterator();

        default public void fastForEach(Consumer<? super Entry> consumer) {
            this.forEach(consumer);
        }
    }

    public static interface Entry
    extends Map.Entry<Double, Short> {
        public double getDoubleKey();

        @Override
        @Deprecated
        default public Double getKey() {
            return this.getDoubleKey();
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

