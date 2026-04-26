/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.objects;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.doubles.AbstractDoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleSpliterator;
import it.unimi.dsi.fastutil.doubles.DoubleSpliterators;
import it.unimi.dsi.fastutil.objects.AbstractObject2DoubleMap;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSpliterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterators;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public class Object2DoubleArrayMap<K>
extends AbstractObject2DoubleMap<K>
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient Object[] key;
    protected transient double[] value;
    protected int size;
    protected transient Object2DoubleMap.FastEntrySet<K> entries;
    protected transient ObjectSet<K> keys;
    protected transient DoubleCollection values;

    public Object2DoubleArrayMap(Object[] key, double[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Object2DoubleArrayMap() {
        this.key = ObjectArrays.EMPTY_ARRAY;
        this.value = DoubleArrays.EMPTY_ARRAY;
    }

    public Object2DoubleArrayMap(int capacity) {
        this.key = new Object[capacity];
        this.value = new double[capacity];
    }

    public Object2DoubleArrayMap(Object2DoubleMap<K> m) {
        this(m.size());
        int i = 0;
        for (Object2DoubleMap.Entry entry : m.object2DoubleEntrySet()) {
            this.key[i] = entry.getKey();
            this.value[i] = entry.getDoubleValue();
            ++i;
        }
        this.size = i;
    }

    public Object2DoubleArrayMap(Map<? extends K, ? extends Double> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<K, Double> e : m.entrySet()) {
            this.key[i] = e.getKey();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Object2DoubleArrayMap(Object[] key, double[] value, int size) {
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

    public Object2DoubleMap.FastEntrySet<K> object2DoubleEntrySet() {
        if (this.entries == null) {
            this.entries = new EntrySet();
        }
        return this.entries;
    }

    private int findKey(Object k) {
        Object[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (!Objects.equals(key[i], k)) continue;
            return i;
        }
        return -1;
    }

    @Override
    public double getDouble(Object k) {
        Object[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (!Objects.equals(key[i], k)) continue;
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
        Object[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            key[i] = null;
        }
        this.size = 0;
    }

    @Override
    public boolean containsKey(Object k) {
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
    public double put(K k, double v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            double oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return oldValue;
        }
        if (this.size == this.key.length) {
            Object[] newKey = new Object[this.size == 0 ? 2 : this.size * 2];
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
    public double removeDouble(Object k) {
        int oldPos = this.findKey(k);
        if (oldPos == -1) {
            return this.defRetValue;
        }
        double oldValue = this.value[oldPos];
        int tail = this.size - oldPos - 1;
        System.arraycopy(this.key, oldPos + 1, this.key, oldPos, tail);
        System.arraycopy(this.value, oldPos + 1, this.value, oldPos, tail);
        --this.size;
        this.key[this.size] = null;
        return oldValue;
    }

    @Override
    public ObjectSet<K> keySet() {
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

    public Object2DoubleArrayMap<K> clone() {
        Object2DoubleArrayMap c;
        try {
            c = (Object2DoubleArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (Object[])this.key.clone();
        c.value = (double[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        Object[] key = this.key;
        double[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeObject(key[i]);
            s.writeDouble(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new Object[this.size];
        Object[] key = this.key;
        this.value = new double[this.size];
        double[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readObject();
            value[i] = s.readDouble();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Object2DoubleMap.Entry<K>>
    implements Object2DoubleMap.FastEntrySet<K> {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Object2DoubleMap.Entry<K>> iterator() {
            return new ObjectIterator<Object2DoubleMap.Entry<K>>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Object2DoubleArrayMap.this.size;
                }

                @Override
                public Object2DoubleMap.Entry<K> next() {
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
                    int tail = Object2DoubleArrayMap.this.size-- - this.next--;
                    System.arraycopy(Object2DoubleArrayMap.this.key, this.next + 1, Object2DoubleArrayMap.this.key, this.next, tail);
                    System.arraycopy(Object2DoubleArrayMap.this.value, this.next + 1, Object2DoubleArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Object2DoubleArrayMap.this.key[Object2DoubleArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Object2DoubleArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Object2DoubleMap.Entry<K>> action) {
                    int max = Object2DoubleArrayMap.this.size;
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
        public ObjectIterator<Object2DoubleMap.Entry<K>> fastIterator() {
            return new ObjectIterator<Object2DoubleMap.Entry<K>>(){
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
                    return this.next < Object2DoubleArrayMap.this.size;
                }

                @Override
                public Object2DoubleMap.Entry<K> next() {
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
                    int tail = Object2DoubleArrayMap.this.size-- - this.next--;
                    System.arraycopy(Object2DoubleArrayMap.this.key, this.next + 1, Object2DoubleArrayMap.this.key, this.next, tail);
                    System.arraycopy(Object2DoubleArrayMap.this.value, this.next + 1, Object2DoubleArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Object2DoubleArrayMap.this.key[Object2DoubleArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Object2DoubleArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Object2DoubleMap.Entry<K>> action) {
                    int max = Object2DoubleArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Object2DoubleMap.Entry<K>> spliterator() {
            return new EntrySetSpliterator(0, Object2DoubleArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Object2DoubleMap.Entry<K>> action) {
            int max = Object2DoubleArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Object2DoubleMap.Entry<K>> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Object2DoubleArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Object2DoubleArrayMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getValue() == null || !(e.getValue() instanceof Double)) {
                return false;
            }
            Object k = e.getKey();
            return Object2DoubleArrayMap.this.containsKey(k) && Double.doubleToRawLongBits(Object2DoubleArrayMap.this.getDouble(k)) == Double.doubleToRawLongBits((Double)e.getValue());
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getValue() == null || !(e.getValue() instanceof Double)) {
                return false;
            }
            Object k = e.getKey();
            double v = (Double)e.getValue();
            int oldPos = Object2DoubleArrayMap.this.findKey(k);
            if (oldPos == -1 || Double.doubleToRawLongBits(v) != Double.doubleToRawLongBits(Object2DoubleArrayMap.this.value[oldPos])) {
                return false;
            }
            int tail = Object2DoubleArrayMap.this.size - oldPos - 1;
            System.arraycopy(Object2DoubleArrayMap.this.key, oldPos + 1, Object2DoubleArrayMap.this.key, oldPos, tail);
            System.arraycopy(Object2DoubleArrayMap.this.value, oldPos + 1, Object2DoubleArrayMap.this.value, oldPos, tail);
            --Object2DoubleArrayMap.this.size;
            Object2DoubleArrayMap.this.key[Object2DoubleArrayMap.this.size] = null;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Object2DoubleMap.Entry<K>>
        implements ObjectSpliterator<Object2DoubleMap.Entry<K>> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Object2DoubleMap.Entry<K> get(int location) {
                return new MapEntry(location);
            }

            protected final it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap$EntrySet.EntrySetSpliterator makeForSplit(int pos, int maxPos) {
                return new EntrySetSpliterator(pos, maxPos);
            }
        }
    }

    private final class KeySet
    extends AbstractObjectSet<K> {
        private KeySet() {
        }

        @Override
        public boolean contains(Object k) {
            return Object2DoubleArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(Object k) {
            int oldPos = Object2DoubleArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Object2DoubleArrayMap.this.size - oldPos - 1;
            System.arraycopy(Object2DoubleArrayMap.this.key, oldPos + 1, Object2DoubleArrayMap.this.key, oldPos, tail);
            System.arraycopy(Object2DoubleArrayMap.this.value, oldPos + 1, Object2DoubleArrayMap.this.value, oldPos, tail);
            --Object2DoubleArrayMap.this.size;
            Object2DoubleArrayMap.this.key[Object2DoubleArrayMap.this.size] = null;
            return true;
        }

        @Override
        public ObjectIterator<K> iterator() {
            return new ObjectIterator<K>(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Object2DoubleArrayMap.this.size;
                }

                @Override
                public K next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Object2DoubleArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Object2DoubleArrayMap.this.size - this.pos;
                    System.arraycopy(Object2DoubleArrayMap.this.key, this.pos, Object2DoubleArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Object2DoubleArrayMap.this.value, this.pos, Object2DoubleArrayMap.this.value, this.pos - 1, tail);
                    --Object2DoubleArrayMap.this.size;
                    --this.pos;
                    Object2DoubleArrayMap.this.key[Object2DoubleArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(Consumer<? super K> action) {
                    Object[] key = Object2DoubleArrayMap.this.key;
                    int max = Object2DoubleArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<K> spliterator() {
            return new KeySetSpliterator(0, Object2DoubleArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super K> action) {
            Object[] key = Object2DoubleArrayMap.this.key;
            int max = Object2DoubleArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Object2DoubleArrayMap.this.size;
        }

        @Override
        public void clear() {
            Object2DoubleArrayMap.this.clear();
        }

        final class KeySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<K>
        implements ObjectSpliterator<K> {
            KeySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final K get(int location) {
                return Object2DoubleArrayMap.this.key[location];
            }

            protected final it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap$KeySet.KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(Consumer<? super K> action) {
                Object[] key = Object2DoubleArrayMap.this.key;
                int max = Object2DoubleArrayMap.this.size;
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
            return Object2DoubleArrayMap.this.containsValue(v);
        }

        @Override
        public DoubleIterator iterator() {
            return new DoubleIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Object2DoubleArrayMap.this.size;
                }

                @Override
                public double nextDouble() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Object2DoubleArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Object2DoubleArrayMap.this.size - this.pos;
                    System.arraycopy(Object2DoubleArrayMap.this.key, this.pos, Object2DoubleArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Object2DoubleArrayMap.this.value, this.pos, Object2DoubleArrayMap.this.value, this.pos - 1, tail);
                    --Object2DoubleArrayMap.this.size;
                    --this.pos;
                    Object2DoubleArrayMap.this.key[Object2DoubleArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(DoubleConsumer action) {
                    double[] value = Object2DoubleArrayMap.this.value;
                    int max = Object2DoubleArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public DoubleSpliterator spliterator() {
            return new ValuesSpliterator(0, Object2DoubleArrayMap.this.size);
        }

        @Override
        public void forEach(DoubleConsumer action) {
            double[] value = Object2DoubleArrayMap.this.value;
            int max = Object2DoubleArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Object2DoubleArrayMap.this.size;
        }

        @Override
        public void clear() {
            Object2DoubleArrayMap.this.clear();
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
                return Object2DoubleArrayMap.this.value[location];
            }

            protected final it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap$ValuesCollection.ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(DoubleConsumer action) {
                double[] value = Object2DoubleArrayMap.this.value;
                int max = Object2DoubleArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Object2DoubleMap.Entry<K>,
    Map.Entry<K, Double>,
    ObjectDoublePair<K> {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public K getKey() {
            return Object2DoubleArrayMap.this.key[this.index];
        }

        @Override
        public K left() {
            return Object2DoubleArrayMap.this.key[this.index];
        }

        @Override
        public double getDoubleValue() {
            return Object2DoubleArrayMap.this.value[this.index];
        }

        @Override
        public double rightDouble() {
            return Object2DoubleArrayMap.this.value[this.index];
        }

        @Override
        public double setValue(double v) {
            double oldValue = Object2DoubleArrayMap.this.value[this.index];
            Object2DoubleArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        @Override
        public ObjectDoublePair<K> right(double v) {
            Object2DoubleArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Double getValue() {
            return Object2DoubleArrayMap.this.value[this.index];
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
            return Objects.equals(Object2DoubleArrayMap.this.key[this.index], e.getKey()) && Double.doubleToRawLongBits(Object2DoubleArrayMap.this.value[this.index]) == Double.doubleToRawLongBits((Double)e.getValue());
        }

        @Override
        public int hashCode() {
            return (Object2DoubleArrayMap.this.key[this.index] == null ? 0 : Object2DoubleArrayMap.this.key[this.index].hashCode()) ^ HashCommon.double2int(Object2DoubleArrayMap.this.value[this.index]);
        }

        public String toString() {
            return Object2DoubleArrayMap.this.key[this.index] + "=>" + Object2DoubleArrayMap.this.value[this.index];
        }
    }
}

