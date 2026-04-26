/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.doubles;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.doubles.AbstractDouble2LongMap;
import it.unimi.dsi.fastutil.doubles.AbstractDoubleSet;
import it.unimi.dsi.fastutil.doubles.Double2LongMap;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleLongPair;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.doubles.DoubleSpliterator;
import it.unimi.dsi.fastutil.doubles.DoubleSpliterators;
import it.unimi.dsi.fastutil.longs.AbstractLongCollection;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSpliterator;
import it.unimi.dsi.fastutil.longs.LongSpliterators;
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
import java.util.function.LongConsumer;

public class Double2LongArrayMap
extends AbstractDouble2LongMap
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient double[] key;
    protected transient long[] value;
    protected int size;
    protected transient Double2LongMap.FastEntrySet entries;
    protected transient DoubleSet keys;
    protected transient LongCollection values;

    public Double2LongArrayMap(double[] key, long[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Double2LongArrayMap() {
        this.key = DoubleArrays.EMPTY_ARRAY;
        this.value = LongArrays.EMPTY_ARRAY;
    }

    public Double2LongArrayMap(int capacity) {
        this.key = new double[capacity];
        this.value = new long[capacity];
    }

    public Double2LongArrayMap(Double2LongMap m) {
        this(m.size());
        int i = 0;
        for (Double2LongMap.Entry e : m.double2LongEntrySet()) {
            this.key[i] = e.getDoubleKey();
            this.value[i] = e.getLongValue();
            ++i;
        }
        this.size = i;
    }

    public Double2LongArrayMap(Map<? extends Double, ? extends Long> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<? extends Double, ? extends Long> e : m.entrySet()) {
            this.key[i] = e.getKey();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Double2LongArrayMap(double[] key, long[] value, int size) {
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

    public Double2LongMap.FastEntrySet double2LongEntrySet() {
        if (this.entries == null) {
            this.entries = new EntrySet();
        }
        return this.entries;
    }

    private int findKey(double k) {
        double[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (Double.doubleToRawLongBits(key[i]) != Double.doubleToRawLongBits(k)) continue;
            return i;
        }
        return -1;
    }

    @Override
    public long get(double k) {
        double[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (Double.doubleToRawLongBits(key[i]) != Double.doubleToRawLongBits(k)) continue;
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
    public boolean containsKey(double k) {
        return this.findKey(k) != -1;
    }

    @Override
    public boolean containsValue(long v) {
        long[] value = this.value;
        int i = this.size;
        while (i-- != 0) {
            if (value[i] != v) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public long put(double k, long v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            long oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return oldValue;
        }
        if (this.size == this.key.length) {
            double[] newKey = new double[this.size == 0 ? 2 : this.size * 2];
            long[] newValue = new long[this.size == 0 ? 2 : this.size * 2];
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
    public long remove(double k) {
        int oldPos = this.findKey(k);
        if (oldPos == -1) {
            return this.defRetValue;
        }
        long oldValue = this.value[oldPos];
        int tail = this.size - oldPos - 1;
        System.arraycopy(this.key, oldPos + 1, this.key, oldPos, tail);
        System.arraycopy(this.value, oldPos + 1, this.value, oldPos, tail);
        --this.size;
        return oldValue;
    }

    @Override
    public DoubleSet keySet() {
        if (this.keys == null) {
            this.keys = new KeySet();
        }
        return this.keys;
    }

    @Override
    public LongCollection values() {
        if (this.values == null) {
            this.values = new ValuesCollection();
        }
        return this.values;
    }

    public Double2LongArrayMap clone() {
        Double2LongArrayMap c;
        try {
            c = (Double2LongArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (double[])this.key.clone();
        c.value = (long[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        double[] key = this.key;
        long[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeDouble(key[i]);
            s.writeLong(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new double[this.size];
        double[] key = this.key;
        this.value = new long[this.size];
        long[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readDouble();
            value[i] = s.readLong();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Double2LongMap.Entry>
    implements Double2LongMap.FastEntrySet {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Double2LongMap.Entry> iterator() {
            return new ObjectIterator<Double2LongMap.Entry>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Double2LongArrayMap.this.size;
                }

                @Override
                public Double2LongMap.Entry next() {
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
                    int tail = Double2LongArrayMap.this.size-- - this.next--;
                    System.arraycopy(Double2LongArrayMap.this.key, this.next + 1, Double2LongArrayMap.this.key, this.next, tail);
                    System.arraycopy(Double2LongArrayMap.this.value, this.next + 1, Double2LongArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Double2LongArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Double2LongMap.Entry> action) {
                    int max = Double2LongArrayMap.this.size;
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
        public ObjectIterator<Double2LongMap.Entry> fastIterator() {
            return new ObjectIterator<Double2LongMap.Entry>(){
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
                    return this.next < Double2LongArrayMap.this.size;
                }

                @Override
                public Double2LongMap.Entry next() {
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
                    int tail = Double2LongArrayMap.this.size-- - this.next--;
                    System.arraycopy(Double2LongArrayMap.this.key, this.next + 1, Double2LongArrayMap.this.key, this.next, tail);
                    System.arraycopy(Double2LongArrayMap.this.value, this.next + 1, Double2LongArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Double2LongArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Double2LongMap.Entry> action) {
                    int max = Double2LongArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Double2LongMap.Entry> spliterator() {
            return new EntrySetSpliterator(0, Double2LongArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Double2LongMap.Entry> action) {
            int max = Double2LongArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Double2LongMap.Entry> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Double2LongArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Double2LongArrayMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Double)) {
                return false;
            }
            if (e.getValue() == null || !(e.getValue() instanceof Long)) {
                return false;
            }
            double k = (Double)e.getKey();
            return Double2LongArrayMap.this.containsKey(k) && Double2LongArrayMap.this.get(k) == ((Long)e.getValue()).longValue();
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Double)) {
                return false;
            }
            if (e.getValue() == null || !(e.getValue() instanceof Long)) {
                return false;
            }
            double k = (Double)e.getKey();
            long v = (Long)e.getValue();
            int oldPos = Double2LongArrayMap.this.findKey(k);
            if (oldPos == -1 || v != Double2LongArrayMap.this.value[oldPos]) {
                return false;
            }
            int tail = Double2LongArrayMap.this.size - oldPos - 1;
            System.arraycopy(Double2LongArrayMap.this.key, oldPos + 1, Double2LongArrayMap.this.key, oldPos, tail);
            System.arraycopy(Double2LongArrayMap.this.value, oldPos + 1, Double2LongArrayMap.this.value, oldPos, tail);
            --Double2LongArrayMap.this.size;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Double2LongMap.Entry>
        implements ObjectSpliterator<Double2LongMap.Entry> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Double2LongMap.Entry get(int location) {
                return new MapEntry(location);
            }

            protected final EntrySetSpliterator makeForSplit(int pos, int maxPos) {
                return new EntrySetSpliterator(pos, maxPos);
            }
        }
    }

    private final class KeySet
    extends AbstractDoubleSet {
        private KeySet() {
        }

        @Override
        public boolean contains(double k) {
            return Double2LongArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(double k) {
            int oldPos = Double2LongArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Double2LongArrayMap.this.size - oldPos - 1;
            System.arraycopy(Double2LongArrayMap.this.key, oldPos + 1, Double2LongArrayMap.this.key, oldPos, tail);
            System.arraycopy(Double2LongArrayMap.this.value, oldPos + 1, Double2LongArrayMap.this.value, oldPos, tail);
            --Double2LongArrayMap.this.size;
            return true;
        }

        @Override
        public DoubleIterator iterator() {
            return new DoubleIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Double2LongArrayMap.this.size;
                }

                @Override
                public double nextDouble() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Double2LongArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Double2LongArrayMap.this.size - this.pos;
                    System.arraycopy(Double2LongArrayMap.this.key, this.pos, Double2LongArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Double2LongArrayMap.this.value, this.pos, Double2LongArrayMap.this.value, this.pos - 1, tail);
                    --Double2LongArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(DoubleConsumer action) {
                    double[] key = Double2LongArrayMap.this.key;
                    int max = Double2LongArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public DoubleSpliterator spliterator() {
            return new KeySetSpliterator(0, Double2LongArrayMap.this.size);
        }

        @Override
        public void forEach(DoubleConsumer action) {
            double[] key = Double2LongArrayMap.this.key;
            int max = Double2LongArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Double2LongArrayMap.this.size;
        }

        @Override
        public void clear() {
            Double2LongArrayMap.this.clear();
        }

        final class KeySetSpliterator
        extends DoubleSpliterators.EarlyBindingSizeIndexBasedSpliterator
        implements DoubleSpliterator {
            KeySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16721;
            }

            @Override
            protected final double get(int location) {
                return Double2LongArrayMap.this.key[location];
            }

            @Override
            protected final KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(DoubleConsumer action) {
                double[] key = Double2LongArrayMap.this.key;
                int max = Double2LongArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(key[this.pos++]);
                }
            }
        }
    }

    private final class ValuesCollection
    extends AbstractLongCollection {
        private ValuesCollection() {
        }

        @Override
        public boolean contains(long v) {
            return Double2LongArrayMap.this.containsValue(v);
        }

        @Override
        public LongIterator iterator() {
            return new LongIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Double2LongArrayMap.this.size;
                }

                @Override
                public long nextLong() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Double2LongArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Double2LongArrayMap.this.size - this.pos;
                    System.arraycopy(Double2LongArrayMap.this.key, this.pos, Double2LongArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Double2LongArrayMap.this.value, this.pos, Double2LongArrayMap.this.value, this.pos - 1, tail);
                    --Double2LongArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(LongConsumer action) {
                    long[] value = Double2LongArrayMap.this.value;
                    int max = Double2LongArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public LongSpliterator spliterator() {
            return new ValuesSpliterator(0, Double2LongArrayMap.this.size);
        }

        @Override
        public void forEach(LongConsumer action) {
            long[] value = Double2LongArrayMap.this.value;
            int max = Double2LongArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Double2LongArrayMap.this.size;
        }

        @Override
        public void clear() {
            Double2LongArrayMap.this.clear();
        }

        final class ValuesSpliterator
        extends LongSpliterators.EarlyBindingSizeIndexBasedSpliterator
        implements LongSpliterator {
            ValuesSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16720;
            }

            @Override
            protected final long get(int location) {
                return Double2LongArrayMap.this.value[location];
            }

            @Override
            protected final ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(LongConsumer action) {
                long[] value = Double2LongArrayMap.this.value;
                int max = Double2LongArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Double2LongMap.Entry,
    Map.Entry<Double, Long>,
    DoubleLongPair {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public double getDoubleKey() {
            return Double2LongArrayMap.this.key[this.index];
        }

        @Override
        public double leftDouble() {
            return Double2LongArrayMap.this.key[this.index];
        }

        @Override
        public long getLongValue() {
            return Double2LongArrayMap.this.value[this.index];
        }

        @Override
        public long rightLong() {
            return Double2LongArrayMap.this.value[this.index];
        }

        @Override
        public long setValue(long v) {
            long oldValue = Double2LongArrayMap.this.value[this.index];
            Double2LongArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        @Override
        public DoubleLongPair right(long v) {
            Double2LongArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Double getKey() {
            return Double2LongArrayMap.this.key[this.index];
        }

        @Override
        @Deprecated
        public Long getValue() {
            return Double2LongArrayMap.this.value[this.index];
        }

        @Override
        @Deprecated
        public Long setValue(Long v) {
            return this.setValue((long)v);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            return Double.doubleToRawLongBits(Double2LongArrayMap.this.key[this.index]) == Double.doubleToRawLongBits((Double)e.getKey()) && Double2LongArrayMap.this.value[this.index] == (Long)e.getValue();
        }

        @Override
        public int hashCode() {
            return HashCommon.double2int(Double2LongArrayMap.this.key[this.index]) ^ HashCommon.long2int(Double2LongArrayMap.this.value[this.index]);
        }

        public String toString() {
            return Double2LongArrayMap.this.key[this.index] + "=>" + Double2LongArrayMap.this.value[this.index];
        }
    }
}

