/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.shorts;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.doubles.AbstractDoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleSpliterator;
import it.unimi.dsi.fastutil.doubles.DoubleSpliterators;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterators;
import it.unimi.dsi.fastutil.shorts.AbstractShort2DoubleMap;
import it.unimi.dsi.fastutil.shorts.AbstractShortSet;
import it.unimi.dsi.fastutil.shorts.Short2DoubleMap;
import it.unimi.dsi.fastutil.shorts.ShortArrays;
import it.unimi.dsi.fastutil.shorts.ShortConsumer;
import it.unimi.dsi.fastutil.shorts.ShortDoublePair;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import it.unimi.dsi.fastutil.shorts.ShortSpliterator;
import it.unimi.dsi.fastutil.shorts.ShortSpliterators;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public class Short2DoubleArrayMap
extends AbstractShort2DoubleMap
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient short[] key;
    protected transient double[] value;
    protected int size;
    protected transient Short2DoubleMap.FastEntrySet entries;
    protected transient ShortSet keys;
    protected transient DoubleCollection values;

    public Short2DoubleArrayMap(short[] key, double[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Short2DoubleArrayMap() {
        this.key = ShortArrays.EMPTY_ARRAY;
        this.value = DoubleArrays.EMPTY_ARRAY;
    }

    public Short2DoubleArrayMap(int capacity) {
        this.key = new short[capacity];
        this.value = new double[capacity];
    }

    public Short2DoubleArrayMap(Short2DoubleMap m) {
        this(m.size());
        int i = 0;
        for (Short2DoubleMap.Entry e : m.short2DoubleEntrySet()) {
            this.key[i] = e.getShortKey();
            this.value[i] = e.getDoubleValue();
            ++i;
        }
        this.size = i;
    }

    public Short2DoubleArrayMap(Map<? extends Short, ? extends Double> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<? extends Short, ? extends Double> e : m.entrySet()) {
            this.key[i] = e.getKey();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Short2DoubleArrayMap(short[] key, double[] value, int size) {
        this.key = key;
        this.value = value;
        this.size = size;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
        if (size > key.length) {
            throw new IllegalArgumentException("The provided size (" + size + ") is larger than or equal to the backing-arrays size (" + key.length + ")");
        }
    }

    public Short2DoubleMap.FastEntrySet short2DoubleEntrySet() {
        if (this.entries == null) {
            this.entries = new EntrySet();
        }
        return this.entries;
    }

    private int findKey(short k) {
        short[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (key[i] != k) continue;
            return i;
        }
        return -1;
    }

    @Override
    public double get(short k) {
        short[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (key[i] != k) continue;
            return this.value[i];
        }
        return this.defRetValue;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void clear() {
        this.size = 0;
    }

    @Override
    public boolean containsKey(short k) {
        return this.findKey(k) != -1;
    }

    @Override
    public boolean containsValue(double v) {
        double[] value = this.value;
        int i = this.size;
        while (i-- != 0) {
            if (Double.doubleToRawLongBits(value[i]) != Double.doubleToRawLongBits(v)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public double put(short k, double v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            double oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return oldValue;
        }
        if (this.size == this.key.length) {
            short[] newKey = new short[this.size == 0 ? 2 : this.size * 2];
            double[] newValue = new double[this.size == 0 ? 2 : this.size * 2];
            int i = this.size;
            while (i-- != 0) {
                newKey[i] = this.key[i];
                newValue[i] = this.value[i];
            }
            this.key = newKey;
            this.value = newValue;
        }
        this.key[this.size] = k;
        this.value[this.size] = v;
        ++this.size;
        return this.defRetValue;
    }

    @Override
    public double remove(short k) {
        int oldPos = this.findKey(k);
        if (oldPos == -1) {
            return this.defRetValue;
        }
        double oldValue = this.value[oldPos];
        int tail = this.size - oldPos - 1;
        System.arraycopy(this.key, oldPos + 1, this.key, oldPos, tail);
        System.arraycopy(this.value, oldPos + 1, this.value, oldPos, tail);
        --this.size;
        return oldValue;
    }

    @Override
    public ShortSet keySet() {
        if (this.keys == null) {
            this.keys = new KeySet();
        }
        return this.keys;
    }

    @Override
    public DoubleCollection values() {
        if (this.values == null) {
            this.values = new ValuesCollection();
        }
        return this.values;
    }

    public Short2DoubleArrayMap clone() {
        Short2DoubleArrayMap c;
        try {
            c = (Short2DoubleArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (short[])this.key.clone();
        c.value = (double[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        short[] key = this.key;
        double[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeShort(key[i]);
            s.writeDouble(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new short[this.size];
        short[] key = this.key;
        this.value = new double[this.size];
        double[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readShort();
            value[i] = s.readDouble();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Short2DoubleMap.Entry>
    implements Short2DoubleMap.FastEntrySet {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Short2DoubleMap.Entry> iterator() {
            return new ObjectIterator<Short2DoubleMap.Entry>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Short2DoubleArrayMap.this.size;
                }

                @Override
                public Short2DoubleMap.Entry next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    this.curr = this.next++;
                    this.entry = new MapEntry(this.curr);
                    return this.entry;
                }

                @Override
                public void remove() {
                    if (this.curr == -1) {
                        throw new IllegalStateException();
                    }
                    this.curr = -1;
                    int tail = Short2DoubleArrayMap.this.size-- - this.next--;
                    System.arraycopy(Short2DoubleArrayMap.this.key, this.next + 1, Short2DoubleArrayMap.this.key, this.next, tail);
                    System.arraycopy(Short2DoubleArrayMap.this.value, this.next + 1, Short2DoubleArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Short2DoubleArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Short2DoubleMap.Entry> action) {
                    int max = Short2DoubleArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.curr = this.curr;
                        this.entry = new MapEntry(this.curr);
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectIterator<Short2DoubleMap.Entry> fastIterator() {
            return new ObjectIterator<Short2DoubleMap.Entry>(){
                private MapEntry entry;
                int next;
                int curr;
                {
                    this.entry = new MapEntry();
                    this.next = 0;
                    this.curr = -1;
                }

                @Override
                public boolean hasNext() {
                    return this.next < Short2DoubleArrayMap.this.size;
                }

                @Override
                public Short2DoubleMap.Entry next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    this.curr = this.next++;
                    this.entry.index = this.curr;
                    return this.entry;
                }

                @Override
                public void remove() {
                    if (this.curr == -1) {
                        throw new IllegalStateException();
                    }
                    this.curr = -1;
                    int tail = Short2DoubleArrayMap.this.size-- - this.next--;
                    System.arraycopy(Short2DoubleArrayMap.this.key, this.next + 1, Short2DoubleArrayMap.this.key, this.next, tail);
                    System.arraycopy(Short2DoubleArrayMap.this.value, this.next + 1, Short2DoubleArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Short2DoubleArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Short2DoubleMap.Entry> action) {
                    int max = Short2DoubleArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Short2DoubleMap.Entry> spliterator() {
            return new EntrySetSpliterator(0, Short2DoubleArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Short2DoubleMap.Entry> action) {
            int max = Short2DoubleArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Short2DoubleMap.Entry> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Short2DoubleArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Short2DoubleArrayMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Short)) {
                return false;
            }
            if (e.getValue() == null || !(e.getValue() instanceof Double)) {
                return false;
            }
            short k = (Short)e.getKey();
            return Short2DoubleArrayMap.this.containsKey(k) && Double.doubleToRawLongBits(Short2DoubleArrayMap.this.get(k)) == Double.doubleToRawLongBits((Double)e.getValue());
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Short)) {
                return false;
            }
            if (e.getValue() == null || !(e.getValue() instanceof Double)) {
                return false;
            }
            short k = (Short)e.getKey();
            double v = (Double)e.getValue();
            int oldPos = Short2DoubleArrayMap.this.findKey(k);
            if (oldPos == -1 || Double.doubleToRawLongBits(v) != Double.doubleToRawLongBits(Short2DoubleArrayMap.this.value[oldPos])) {
                return false;
            }
            int tail = Short2DoubleArrayMap.this.size - oldPos - 1;
            System.arraycopy(Short2DoubleArrayMap.this.key, oldPos + 1, Short2DoubleArrayMap.this.key, oldPos, tail);
            System.arraycopy(Short2DoubleArrayMap.this.value, oldPos + 1, Short2DoubleArrayMap.this.value, oldPos, tail);
            --Short2DoubleArrayMap.this.size;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Short2DoubleMap.Entry>
        implements ObjectSpliterator<Short2DoubleMap.Entry> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Short2DoubleMap.Entry get(int location) {
                return new MapEntry(location);
            }

            protected final EntrySetSpliterator makeForSplit(int pos, int maxPos) {
                return new EntrySetSpliterator(pos, maxPos);
            }
        }
    }

    private final class KeySet
    extends AbstractShortSet {
        private KeySet() {
        }

        @Override
        public boolean contains(short k) {
            return Short2DoubleArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(short k) {
            int oldPos = Short2DoubleArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Short2DoubleArrayMap.this.size - oldPos - 1;
            System.arraycopy(Short2DoubleArrayMap.this.key, oldPos + 1, Short2DoubleArrayMap.this.key, oldPos, tail);
            System.arraycopy(Short2DoubleArrayMap.this.value, oldPos + 1, Short2DoubleArrayMap.this.value, oldPos, tail);
            --Short2DoubleArrayMap.this.size;
            return true;
        }

        @Override
        public ShortIterator iterator() {
            return new ShortIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Short2DoubleArrayMap.this.size;
                }

                @Override
                public short nextShort() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Short2DoubleArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Short2DoubleArrayMap.this.size - this.pos;
                    System.arraycopy(Short2DoubleArrayMap.this.key, this.pos, Short2DoubleArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Short2DoubleArrayMap.this.value, this.pos, Short2DoubleArrayMap.this.value, this.pos - 1, tail);
                    --Short2DoubleArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(ShortConsumer action) {
                    short[] key = Short2DoubleArrayMap.this.key;
                    int max = Short2DoubleArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ShortSpliterator spliterator() {
            return new KeySetSpliterator(0, Short2DoubleArrayMap.this.size);
        }

        @Override
        public void forEach(ShortConsumer action) {
            short[] key = Short2DoubleArrayMap.this.key;
            int max = Short2DoubleArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Short2DoubleArrayMap.this.size;
        }

        @Override
        public void clear() {
            Short2DoubleArrayMap.this.clear();
        }

        final class KeySetSpliterator
        extends ShortSpliterators.EarlyBindingSizeIndexBasedSpliterator
        implements ShortSpliterator {
            KeySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16721;
            }

            @Override
            protected final short get(int location) {
                return Short2DoubleArrayMap.this.key[location];
            }

            @Override
            protected final KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(ShortConsumer action) {
                short[] key = Short2DoubleArrayMap.this.key;
                int max = Short2DoubleArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(key[this.pos++]);
                }
            }
        }
    }

    private final class ValuesCollection
    extends AbstractDoubleCollection {
        private ValuesCollection() {
        }

        @Override
        public boolean contains(double v) {
            return Short2DoubleArrayMap.this.containsValue(v);
        }

        @Override
        public DoubleIterator iterator() {
            return new DoubleIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Short2DoubleArrayMap.this.size;
                }

                @Override
                public double nextDouble() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Short2DoubleArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Short2DoubleArrayMap.this.size - this.pos;
                    System.arraycopy(Short2DoubleArrayMap.this.key, this.pos, Short2DoubleArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Short2DoubleArrayMap.this.value, this.pos, Short2DoubleArrayMap.this.value, this.pos - 1, tail);
                    --Short2DoubleArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(DoubleConsumer action) {
                    double[] value = Short2DoubleArrayMap.this.value;
                    int max = Short2DoubleArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public DoubleSpliterator spliterator() {
            return new ValuesSpliterator(0, Short2DoubleArrayMap.this.size);
        }

        @Override
        public void forEach(DoubleConsumer action) {
            double[] value = Short2DoubleArrayMap.this.value;
            int max = Short2DoubleArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Short2DoubleArrayMap.this.size;
        }

        @Override
        public void clear() {
            Short2DoubleArrayMap.this.clear();
        }

        final class ValuesSpliterator
        extends DoubleSpliterators.EarlyBindingSizeIndexBasedSpliterator
        implements DoubleSpliterator {
            ValuesSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16720;
            }

            @Override
            protected final double get(int location) {
                return Short2DoubleArrayMap.this.value[location];
            }

            @Override
            protected final ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(DoubleConsumer action) {
                double[] value = Short2DoubleArrayMap.this.value;
                int max = Short2DoubleArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Short2DoubleMap.Entry,
    Map.Entry<Short, Double>,
    ShortDoublePair {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public short getShortKey() {
            return Short2DoubleArrayMap.this.key[this.index];
        }

        @Override
        public short leftShort() {
            return Short2DoubleArrayMap.this.key[this.index];
        }

        @Override
        public double getDoubleValue() {
            return Short2DoubleArrayMap.this.value[this.index];
        }

        @Override
        public double rightDouble() {
            return Short2DoubleArrayMap.this.value[this.index];
        }

        @Override
        public double setValue(double v) {
            double oldValue = Short2DoubleArrayMap.this.value[this.index];
            Short2DoubleArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        @Override
        public ShortDoublePair right(double v) {
            Short2DoubleArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Short getKey() {
            return Short2DoubleArrayMap.this.key[this.index];
        }

        @Override
        @Deprecated
        public Double getValue() {
            return Short2DoubleArrayMap.this.value[this.index];
        }

        @Override
        @Deprecated
        public Double setValue(Double v) {
            return this.setValue((double)v);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            return Short2DoubleArrayMap.this.key[this.index] == (Short)e.getKey() && Double.doubleToRawLongBits(Short2DoubleArrayMap.this.value[this.index]) == Double.doubleToRawLongBits((Double)e.getValue());
        }

        @Override
        public int hashCode() {
            return Short2DoubleArrayMap.this.key[this.index] ^ HashCommon.double2int(Short2DoubleArrayMap.this.value[this.index]);
        }

        public String toString() {
            return Short2DoubleArrayMap.this.key[this.index] + "=>" + Short2DoubleArrayMap.this.value[this.index];
        }
    }
}

