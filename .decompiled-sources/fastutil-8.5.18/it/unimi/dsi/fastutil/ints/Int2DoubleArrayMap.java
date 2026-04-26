/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.ints;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.doubles.AbstractDoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleSpliterator;
import it.unimi.dsi.fastutil.doubles.DoubleSpliterators;
import it.unimi.dsi.fastutil.ints.AbstractInt2DoubleMap;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntDoublePair;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSpliterator;
import it.unimi.dsi.fastutil.ints.IntSpliterators;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterators;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

public class Int2DoubleArrayMap
extends AbstractInt2DoubleMap
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient int[] key;
    protected transient double[] value;
    protected int size;
    protected transient Int2DoubleMap.FastEntrySet entries;
    protected transient IntSet keys;
    protected transient DoubleCollection values;

    public Int2DoubleArrayMap(int[] key, double[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Int2DoubleArrayMap() {
        this.key = IntArrays.EMPTY_ARRAY;
        this.value = DoubleArrays.EMPTY_ARRAY;
    }

    public Int2DoubleArrayMap(int capacity) {
        this.key = new int[capacity];
        this.value = new double[capacity];
    }

    public Int2DoubleArrayMap(Int2DoubleMap m) {
        this(m.size());
        int i = 0;
        for (Int2DoubleMap.Entry e : m.int2DoubleEntrySet()) {
            this.key[i] = e.getIntKey();
            this.value[i] = e.getDoubleValue();
            ++i;
        }
        this.size = i;
    }

    public Int2DoubleArrayMap(Map<? extends Integer, ? extends Double> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<? extends Integer, ? extends Double> e : m.entrySet()) {
            this.key[i] = e.getKey();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Int2DoubleArrayMap(int[] key, double[] value, int size) {
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

    public Int2DoubleMap.FastEntrySet int2DoubleEntrySet() {
        if (this.entries == null) {
            this.entries = new EntrySet();
        }
        return this.entries;
    }

    private int findKey(int k) {
        int[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (key[i] != k) continue;
            return i;
        }
        return -1;
    }

    @Override
    public double get(int k) {
        int[] key = this.key;
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
    public boolean containsKey(int k) {
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
    public double put(int k, double v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            double oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return oldValue;
        }
        if (this.size == this.key.length) {
            int[] newKey = new int[this.size == 0 ? 2 : this.size * 2];
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
    public double remove(int k) {
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
    public IntSet keySet() {
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

    public Int2DoubleArrayMap clone() {
        Int2DoubleArrayMap c;
        try {
            c = (Int2DoubleArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (int[])this.key.clone();
        c.value = (double[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        int[] key = this.key;
        double[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeInt(key[i]);
            s.writeDouble(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new int[this.size];
        int[] key = this.key;
        this.value = new double[this.size];
        double[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readInt();
            value[i] = s.readDouble();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Int2DoubleMap.Entry>
    implements Int2DoubleMap.FastEntrySet {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Int2DoubleMap.Entry> iterator() {
            return new ObjectIterator<Int2DoubleMap.Entry>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Int2DoubleArrayMap.this.size;
                }

                @Override
                public Int2DoubleMap.Entry next() {
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
                    int tail = Int2DoubleArrayMap.this.size-- - this.next--;
                    System.arraycopy(Int2DoubleArrayMap.this.key, this.next + 1, Int2DoubleArrayMap.this.key, this.next, tail);
                    System.arraycopy(Int2DoubleArrayMap.this.value, this.next + 1, Int2DoubleArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Int2DoubleArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Int2DoubleMap.Entry> action) {
                    int max = Int2DoubleArrayMap.this.size;
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
        public ObjectIterator<Int2DoubleMap.Entry> fastIterator() {
            return new ObjectIterator<Int2DoubleMap.Entry>(){
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
                    return this.next < Int2DoubleArrayMap.this.size;
                }

                @Override
                public Int2DoubleMap.Entry next() {
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
                    int tail = Int2DoubleArrayMap.this.size-- - this.next--;
                    System.arraycopy(Int2DoubleArrayMap.this.key, this.next + 1, Int2DoubleArrayMap.this.key, this.next, tail);
                    System.arraycopy(Int2DoubleArrayMap.this.value, this.next + 1, Int2DoubleArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Int2DoubleArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Int2DoubleMap.Entry> action) {
                    int max = Int2DoubleArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Int2DoubleMap.Entry> spliterator() {
            return new EntrySetSpliterator(0, Int2DoubleArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Int2DoubleMap.Entry> action) {
            int max = Int2DoubleArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Int2DoubleMap.Entry> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Int2DoubleArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Int2DoubleArrayMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Integer)) {
                return false;
            }
            if (e.getValue() == null || !(e.getValue() instanceof Double)) {
                return false;
            }
            int k = (Integer)e.getKey();
            return Int2DoubleArrayMap.this.containsKey(k) && Double.doubleToRawLongBits(Int2DoubleArrayMap.this.get(k)) == Double.doubleToRawLongBits((Double)e.getValue());
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Integer)) {
                return false;
            }
            if (e.getValue() == null || !(e.getValue() instanceof Double)) {
                return false;
            }
            int k = (Integer)e.getKey();
            double v = (Double)e.getValue();
            int oldPos = Int2DoubleArrayMap.this.findKey(k);
            if (oldPos == -1 || Double.doubleToRawLongBits(v) != Double.doubleToRawLongBits(Int2DoubleArrayMap.this.value[oldPos])) {
                return false;
            }
            int tail = Int2DoubleArrayMap.this.size - oldPos - 1;
            System.arraycopy(Int2DoubleArrayMap.this.key, oldPos + 1, Int2DoubleArrayMap.this.key, oldPos, tail);
            System.arraycopy(Int2DoubleArrayMap.this.value, oldPos + 1, Int2DoubleArrayMap.this.value, oldPos, tail);
            --Int2DoubleArrayMap.this.size;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Int2DoubleMap.Entry>
        implements ObjectSpliterator<Int2DoubleMap.Entry> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Int2DoubleMap.Entry get(int location) {
                return new MapEntry(location);
            }

            protected final EntrySetSpliterator makeForSplit(int pos, int maxPos) {
                return new EntrySetSpliterator(pos, maxPos);
            }
        }
    }

    private final class KeySet
    extends AbstractIntSet {
        private KeySet() {
        }

        @Override
        public boolean contains(int k) {
            return Int2DoubleArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(int k) {
            int oldPos = Int2DoubleArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Int2DoubleArrayMap.this.size - oldPos - 1;
            System.arraycopy(Int2DoubleArrayMap.this.key, oldPos + 1, Int2DoubleArrayMap.this.key, oldPos, tail);
            System.arraycopy(Int2DoubleArrayMap.this.value, oldPos + 1, Int2DoubleArrayMap.this.value, oldPos, tail);
            --Int2DoubleArrayMap.this.size;
            return true;
        }

        @Override
        public IntIterator iterator() {
            return new IntIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Int2DoubleArrayMap.this.size;
                }

                @Override
                public int nextInt() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Int2DoubleArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Int2DoubleArrayMap.this.size - this.pos;
                    System.arraycopy(Int2DoubleArrayMap.this.key, this.pos, Int2DoubleArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Int2DoubleArrayMap.this.value, this.pos, Int2DoubleArrayMap.this.value, this.pos - 1, tail);
                    --Int2DoubleArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(IntConsumer action) {
                    int[] key = Int2DoubleArrayMap.this.key;
                    int max = Int2DoubleArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public IntSpliterator spliterator() {
            return new KeySetSpliterator(0, Int2DoubleArrayMap.this.size);
        }

        @Override
        public void forEach(IntConsumer action) {
            int[] key = Int2DoubleArrayMap.this.key;
            int max = Int2DoubleArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Int2DoubleArrayMap.this.size;
        }

        @Override
        public void clear() {
            Int2DoubleArrayMap.this.clear();
        }

        final class KeySetSpliterator
        extends IntSpliterators.EarlyBindingSizeIndexBasedSpliterator
        implements IntSpliterator {
            KeySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16721;
            }

            @Override
            protected final int get(int location) {
                return Int2DoubleArrayMap.this.key[location];
            }

            @Override
            protected final KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(IntConsumer action) {
                int[] key = Int2DoubleArrayMap.this.key;
                int max = Int2DoubleArrayMap.this.size;
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
            return Int2DoubleArrayMap.this.containsValue(v);
        }

        @Override
        public DoubleIterator iterator() {
            return new DoubleIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Int2DoubleArrayMap.this.size;
                }

                @Override
                public double nextDouble() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Int2DoubleArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Int2DoubleArrayMap.this.size - this.pos;
                    System.arraycopy(Int2DoubleArrayMap.this.key, this.pos, Int2DoubleArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Int2DoubleArrayMap.this.value, this.pos, Int2DoubleArrayMap.this.value, this.pos - 1, tail);
                    --Int2DoubleArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(DoubleConsumer action) {
                    double[] value = Int2DoubleArrayMap.this.value;
                    int max = Int2DoubleArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public DoubleSpliterator spliterator() {
            return new ValuesSpliterator(0, Int2DoubleArrayMap.this.size);
        }

        @Override
        public void forEach(DoubleConsumer action) {
            double[] value = Int2DoubleArrayMap.this.value;
            int max = Int2DoubleArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Int2DoubleArrayMap.this.size;
        }

        @Override
        public void clear() {
            Int2DoubleArrayMap.this.clear();
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
                return Int2DoubleArrayMap.this.value[location];
            }

            @Override
            protected final ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(DoubleConsumer action) {
                double[] value = Int2DoubleArrayMap.this.value;
                int max = Int2DoubleArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Int2DoubleMap.Entry,
    Map.Entry<Integer, Double>,
    IntDoublePair {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public int getIntKey() {
            return Int2DoubleArrayMap.this.key[this.index];
        }

        @Override
        public int leftInt() {
            return Int2DoubleArrayMap.this.key[this.index];
        }

        @Override
        public double getDoubleValue() {
            return Int2DoubleArrayMap.this.value[this.index];
        }

        @Override
        public double rightDouble() {
            return Int2DoubleArrayMap.this.value[this.index];
        }

        @Override
        public double setValue(double v) {
            double oldValue = Int2DoubleArrayMap.this.value[this.index];
            Int2DoubleArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        @Override
        public IntDoublePair right(double v) {
            Int2DoubleArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Integer getKey() {
            return Int2DoubleArrayMap.this.key[this.index];
        }

        @Override
        @Deprecated
        public Double getValue() {
            return Int2DoubleArrayMap.this.value[this.index];
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
            return Int2DoubleArrayMap.this.key[this.index] == (Integer)e.getKey() && Double.doubleToRawLongBits(Int2DoubleArrayMap.this.value[this.index]) == Double.doubleToRawLongBits((Double)e.getValue());
        }

        @Override
        public int hashCode() {
            return Int2DoubleArrayMap.this.key[this.index] ^ HashCommon.double2int(Int2DoubleArrayMap.this.value[this.index]);
        }

        public String toString() {
            return Int2DoubleArrayMap.this.key[this.index] + "=>" + Int2DoubleArrayMap.this.value[this.index];
        }
    }
}

