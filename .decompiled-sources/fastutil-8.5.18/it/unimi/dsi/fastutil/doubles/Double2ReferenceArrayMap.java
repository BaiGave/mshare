/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.doubles;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.doubles.AbstractDouble2ReferenceMap;
import it.unimi.dsi.fastutil.doubles.AbstractDoubleSet;
import it.unimi.dsi.fastutil.doubles.Double2ReferenceArrayMap;
import it.unimi.dsi.fastutil.doubles.Double2ReferenceMap;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleReferencePair;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.doubles.DoubleSpliterator;
import it.unimi.dsi.fastutil.doubles.DoubleSpliterators;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.AbstractReferenceCollection;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterators;
import it.unimi.dsi.fastutil.objects.ReferenceCollection;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public class Double2ReferenceArrayMap<V>
extends AbstractDouble2ReferenceMap<V>
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient double[] key;
    protected transient Object[] value;
    protected int size;
    protected transient Double2ReferenceMap.FastEntrySet<V> entries;
    protected transient DoubleSet keys;
    protected transient ReferenceCollection<V> values;

    public Double2ReferenceArrayMap(double[] key, Object[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Double2ReferenceArrayMap() {
        this.key = DoubleArrays.EMPTY_ARRAY;
        this.value = ObjectArrays.EMPTY_ARRAY;
    }

    public Double2ReferenceArrayMap(int capacity) {
        this.key = new double[capacity];
        this.value = new Object[capacity];
    }

    public Double2ReferenceArrayMap(Double2ReferenceMap<V> m) {
        this(m.size());
        int i = 0;
        for (Double2ReferenceMap.Entry entry : m.double2ReferenceEntrySet()) {
            this.key[i] = entry.getDoubleKey();
            this.value[i] = entry.getValue();
            ++i;
        }
        this.size = i;
    }

    public Double2ReferenceArrayMap(Map<? extends Double, ? extends V> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<Double, V> e : m.entrySet()) {
            this.key[i] = e.getKey();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Double2ReferenceArrayMap(double[] key, Object[] value, int size) {
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

    public Double2ReferenceMap.FastEntrySet<V> double2ReferenceEntrySet() {
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
    public V get(double k) {
        double[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (Double.doubleToRawLongBits(key[i]) != Double.doubleToRawLongBits(k)) continue;
            return (V)this.value[i];
        }
        return (V)this.defRetValue;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void clear() {
        Object[] value = this.value;
        int i = this.size;
        while (i-- != 0) {
            value[i] = null;
        }
        this.size = 0;
    }

    @Override
    public boolean containsKey(double k) {
        return this.findKey(k) != -1;
    }

    @Override
    public boolean containsValue(Object v) {
        Object[] value = this.value;
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
    public V put(double k, V v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            Object oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return (V)oldValue;
        }
        if (this.size == this.key.length) {
            double[] newKey = new double[this.size == 0 ? 2 : this.size * 2];
            Object[] newValue = new Object[this.size == 0 ? 2 : this.size * 2];
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
        return (V)this.defRetValue;
    }

    @Override
    public V remove(double k) {
        int oldPos = this.findKey(k);
        if (oldPos == -1) {
            return (V)this.defRetValue;
        }
        Object oldValue = this.value[oldPos];
        int tail = this.size - oldPos - 1;
        System.arraycopy(this.key, oldPos + 1, this.key, oldPos, tail);
        System.arraycopy(this.value, oldPos + 1, this.value, oldPos, tail);
        --this.size;
        this.value[this.size] = null;
        return (V)oldValue;
    }

    @Override
    public DoubleSet keySet() {
        if (this.keys == null) {
            this.keys = new KeySet();
        }
        return this.keys;
    }

    @Override
    public ReferenceCollection<V> values() {
        if (this.values == null) {
            this.values = new ValuesCollection();
        }
        return this.values;
    }

    public Double2ReferenceArrayMap<V> clone() {
        Double2ReferenceArrayMap c;
        try {
            c = (Double2ReferenceArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (double[])this.key.clone();
        c.value = (Object[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        double[] key = this.key;
        Object[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeDouble(key[i]);
            s.writeObject(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new double[this.size];
        double[] key = this.key;
        this.value = new Object[this.size];
        Object[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readDouble();
            value[i] = s.readObject();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Double2ReferenceMap.Entry<V>>
    implements Double2ReferenceMap.FastEntrySet<V> {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Double2ReferenceMap.Entry<V>> iterator() {
            return new ObjectIterator<Double2ReferenceMap.Entry<V>>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Double2ReferenceArrayMap.this.size;
                }

                @Override
                public Double2ReferenceMap.Entry<V> next() {
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
                    int tail = Double2ReferenceArrayMap.this.size-- - this.next--;
                    System.arraycopy(Double2ReferenceArrayMap.this.key, this.next + 1, Double2ReferenceArrayMap.this.key, this.next, tail);
                    System.arraycopy(Double2ReferenceArrayMap.this.value, this.next + 1, Double2ReferenceArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Double2ReferenceArrayMap.this.value[Double2ReferenceArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Double2ReferenceArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Double2ReferenceMap.Entry<V>> action) {
                    int max = Double2ReferenceArrayMap.this.size;
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
        public ObjectIterator<Double2ReferenceMap.Entry<V>> fastIterator() {
            return new ObjectIterator<Double2ReferenceMap.Entry<V>>(){
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
                    return this.next < Double2ReferenceArrayMap.this.size;
                }

                @Override
                public Double2ReferenceMap.Entry<V> next() {
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
                    int tail = Double2ReferenceArrayMap.this.size-- - this.next--;
                    System.arraycopy(Double2ReferenceArrayMap.this.key, this.next + 1, Double2ReferenceArrayMap.this.key, this.next, tail);
                    System.arraycopy(Double2ReferenceArrayMap.this.value, this.next + 1, Double2ReferenceArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Double2ReferenceArrayMap.this.value[Double2ReferenceArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Double2ReferenceArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Double2ReferenceMap.Entry<V>> action) {
                    int max = Double2ReferenceArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Double2ReferenceMap.Entry<V>> spliterator() {
            return new EntrySetSpliterator(0, Double2ReferenceArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Double2ReferenceMap.Entry<V>> action) {
            int max = Double2ReferenceArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Double2ReferenceMap.Entry<V>> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Double2ReferenceArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Double2ReferenceArrayMap.this.size;
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
            double k = (Double)e.getKey();
            return Double2ReferenceArrayMap.this.containsKey(k) && Double2ReferenceArrayMap.this.get(k) == e.getValue();
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
            double k = (Double)e.getKey();
            Object v = e.getValue();
            int oldPos = Double2ReferenceArrayMap.this.findKey(k);
            if (oldPos == -1 || v != Double2ReferenceArrayMap.this.value[oldPos]) {
                return false;
            }
            int tail = Double2ReferenceArrayMap.this.size - oldPos - 1;
            System.arraycopy(Double2ReferenceArrayMap.this.key, oldPos + 1, Double2ReferenceArrayMap.this.key, oldPos, tail);
            System.arraycopy(Double2ReferenceArrayMap.this.value, oldPos + 1, Double2ReferenceArrayMap.this.value, oldPos, tail);
            --Double2ReferenceArrayMap.this.size;
            Double2ReferenceArrayMap.this.value[Double2ReferenceArrayMap.this.size] = null;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Double2ReferenceMap.Entry<V>>
        implements ObjectSpliterator<Double2ReferenceMap.Entry<V>> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Double2ReferenceMap.Entry<V> get(int location) {
                return new MapEntry(location);
            }

            protected final it.unimi.dsi.fastutil.doubles.Double2ReferenceArrayMap$EntrySet.EntrySetSpliterator makeForSplit(int pos, int maxPos) {
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
            return Double2ReferenceArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(double k) {
            int oldPos = Double2ReferenceArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Double2ReferenceArrayMap.this.size - oldPos - 1;
            System.arraycopy(Double2ReferenceArrayMap.this.key, oldPos + 1, Double2ReferenceArrayMap.this.key, oldPos, tail);
            System.arraycopy(Double2ReferenceArrayMap.this.value, oldPos + 1, Double2ReferenceArrayMap.this.value, oldPos, tail);
            --Double2ReferenceArrayMap.this.size;
            Double2ReferenceArrayMap.this.value[Double2ReferenceArrayMap.this.size] = null;
            return true;
        }

        @Override
        public DoubleIterator iterator() {
            return new DoubleIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Double2ReferenceArrayMap.this.size;
                }

                @Override
                public double nextDouble() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Double2ReferenceArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Double2ReferenceArrayMap.this.size - this.pos;
                    System.arraycopy(Double2ReferenceArrayMap.this.key, this.pos, Double2ReferenceArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Double2ReferenceArrayMap.this.value, this.pos, Double2ReferenceArrayMap.this.value, this.pos - 1, tail);
                    --Double2ReferenceArrayMap.this.size;
                    --this.pos;
                    Double2ReferenceArrayMap.this.value[Double2ReferenceArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(DoubleConsumer action) {
                    double[] key = Double2ReferenceArrayMap.this.key;
                    int max = Double2ReferenceArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public DoubleSpliterator spliterator() {
            return new KeySetSpliterator(0, Double2ReferenceArrayMap.this.size);
        }

        @Override
        public void forEach(DoubleConsumer action) {
            double[] key = Double2ReferenceArrayMap.this.key;
            int max = Double2ReferenceArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Double2ReferenceArrayMap.this.size;
        }

        @Override
        public void clear() {
            Double2ReferenceArrayMap.this.clear();
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
                return Double2ReferenceArrayMap.this.key[location];
            }

            protected final it.unimi.dsi.fastutil.doubles.Double2ReferenceArrayMap$KeySet.KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(DoubleConsumer action) {
                double[] key = Double2ReferenceArrayMap.this.key;
                int max = Double2ReferenceArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(key[this.pos++]);
                }
            }
        }
    }

    private final class ValuesCollection
    extends AbstractReferenceCollection<V> {
        private ValuesCollection() {
        }

        @Override
        public boolean contains(Object v) {
            return Double2ReferenceArrayMap.this.containsValue(v);
        }

        @Override
        public ObjectIterator<V> iterator() {
            return new ObjectIterator<V>(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Double2ReferenceArrayMap.this.size;
                }

                @Override
                public V next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Double2ReferenceArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Double2ReferenceArrayMap.this.size - this.pos;
                    System.arraycopy(Double2ReferenceArrayMap.this.key, this.pos, Double2ReferenceArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Double2ReferenceArrayMap.this.value, this.pos, Double2ReferenceArrayMap.this.value, this.pos - 1, tail);
                    --Double2ReferenceArrayMap.this.size;
                    --this.pos;
                    Double2ReferenceArrayMap.this.value[Double2ReferenceArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(Consumer<? super V> action) {
                    Object[] value = Double2ReferenceArrayMap.this.value;
                    int max = Double2ReferenceArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<V> spliterator() {
            return new ValuesSpliterator(0, Double2ReferenceArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super V> action) {
            Object[] value = Double2ReferenceArrayMap.this.value;
            int max = Double2ReferenceArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Double2ReferenceArrayMap.this.size;
        }

        @Override
        public void clear() {
            Double2ReferenceArrayMap.this.clear();
        }

        final class ValuesSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<V>
        implements ObjectSpliterator<V> {
            ValuesSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16464;
            }

            @Override
            protected final V get(int location) {
                return Double2ReferenceArrayMap.this.value[location];
            }

            protected final it.unimi.dsi.fastutil.doubles.Double2ReferenceArrayMap$ValuesCollection.ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(Consumer<? super V> action) {
                Object[] value = Double2ReferenceArrayMap.this.value;
                int max = Double2ReferenceArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Double2ReferenceMap.Entry<V>,
    Map.Entry<Double, V>,
    DoubleReferencePair<V> {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public double getDoubleKey() {
            return Double2ReferenceArrayMap.this.key[this.index];
        }

        @Override
        public double leftDouble() {
            return Double2ReferenceArrayMap.this.key[this.index];
        }

        @Override
        public V getValue() {
            return Double2ReferenceArrayMap.this.value[this.index];
        }

        @Override
        public V right() {
            return Double2ReferenceArrayMap.this.value[this.index];
        }

        @Override
        public V setValue(V v) {
            Object oldValue = Double2ReferenceArrayMap.this.value[this.index];
            Double2ReferenceArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        public DoubleReferencePair<V> right(V v) {
            Double2ReferenceArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Double getKey() {
            return Double2ReferenceArrayMap.this.key[this.index];
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            return Double.doubleToRawLongBits(Double2ReferenceArrayMap.this.key[this.index]) == Double.doubleToRawLongBits((Double)e.getKey()) && Double2ReferenceArrayMap.this.value[this.index] == e.getValue();
        }

        @Override
        public int hashCode() {
            return HashCommon.double2int(Double2ReferenceArrayMap.this.key[this.index]) ^ (Double2ReferenceArrayMap.this.value[this.index] == null ? 0 : System.identityHashCode(Double2ReferenceArrayMap.this.value[this.index]));
        }

        public String toString() {
            return Double2ReferenceArrayMap.this.key[this.index] + "=>" + Double2ReferenceArrayMap.this.value[this.index];
        }
    }
}

