/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.bytes;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.bytes.AbstractByte2DoubleMap;
import it.unimi.dsi.fastutil.bytes.AbstractByteSet;
import it.unimi.dsi.fastutil.bytes.Byte2DoubleMap;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.bytes.ByteConsumer;
import it.unimi.dsi.fastutil.bytes.ByteDoublePair;
import it.unimi.dsi.fastutil.bytes.ByteIterator;
import it.unimi.dsi.fastutil.bytes.ByteSet;
import it.unimi.dsi.fastutil.bytes.ByteSpliterator;
import it.unimi.dsi.fastutil.bytes.ByteSpliterators;
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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public class Byte2DoubleArrayMap
extends AbstractByte2DoubleMap
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient byte[] key;
    protected transient double[] value;
    protected int size;
    protected transient Byte2DoubleMap.FastEntrySet entries;
    protected transient ByteSet keys;
    protected transient DoubleCollection values;

    public Byte2DoubleArrayMap(byte[] key, double[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Byte2DoubleArrayMap() {
        this.key = ByteArrays.EMPTY_ARRAY;
        this.value = DoubleArrays.EMPTY_ARRAY;
    }

    public Byte2DoubleArrayMap(int capacity) {
        this.key = new byte[capacity];
        this.value = new double[capacity];
    }

    public Byte2DoubleArrayMap(Byte2DoubleMap m) {
        this(m.size());
        int i = 0;
        for (Byte2DoubleMap.Entry e : m.byte2DoubleEntrySet()) {
            this.key[i] = e.getByteKey();
            this.value[i] = e.getDoubleValue();
            ++i;
        }
        this.size = i;
    }

    public Byte2DoubleArrayMap(Map<? extends Byte, ? extends Double> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<? extends Byte, ? extends Double> e : m.entrySet()) {
            this.key[i] = e.getKey();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Byte2DoubleArrayMap(byte[] key, double[] value, int size) {
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

    public Byte2DoubleMap.FastEntrySet byte2DoubleEntrySet() {
        if (this.entries == null) {
            this.entries = new EntrySet();
        }
        return this.entries;
    }

    private int findKey(byte k) {
        byte[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (key[i] != k) continue;
            return i;
        }
        return -1;
    }

    @Override
    public double get(byte k) {
        byte[] key = this.key;
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
    public boolean containsKey(byte k) {
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
    public double put(byte k, double v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            double oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return oldValue;
        }
        if (this.size == this.key.length) {
            byte[] newKey = new byte[this.size == 0 ? 2 : this.size * 2];
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
    public double remove(byte k) {
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
    public ByteSet keySet() {
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

    public Byte2DoubleArrayMap clone() {
        Byte2DoubleArrayMap c;
        try {
            c = (Byte2DoubleArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (byte[])this.key.clone();
        c.value = (double[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        byte[] key = this.key;
        double[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeByte(key[i]);
            s.writeDouble(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new byte[this.size];
        byte[] key = this.key;
        this.value = new double[this.size];
        double[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readByte();
            value[i] = s.readDouble();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Byte2DoubleMap.Entry>
    implements Byte2DoubleMap.FastEntrySet {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Byte2DoubleMap.Entry> iterator() {
            return new ObjectIterator<Byte2DoubleMap.Entry>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Byte2DoubleArrayMap.this.size;
                }

                @Override
                public Byte2DoubleMap.Entry next() {
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
                    int tail = Byte2DoubleArrayMap.this.size-- - this.next--;
                    System.arraycopy(Byte2DoubleArrayMap.this.key, this.next + 1, Byte2DoubleArrayMap.this.key, this.next, tail);
                    System.arraycopy(Byte2DoubleArrayMap.this.value, this.next + 1, Byte2DoubleArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Byte2DoubleArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Byte2DoubleMap.Entry> action) {
                    int max = Byte2DoubleArrayMap.this.size;
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
        public ObjectIterator<Byte2DoubleMap.Entry> fastIterator() {
            return new ObjectIterator<Byte2DoubleMap.Entry>(){
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
                    return this.next < Byte2DoubleArrayMap.this.size;
                }

                @Override
                public Byte2DoubleMap.Entry next() {
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
                    int tail = Byte2DoubleArrayMap.this.size-- - this.next--;
                    System.arraycopy(Byte2DoubleArrayMap.this.key, this.next + 1, Byte2DoubleArrayMap.this.key, this.next, tail);
                    System.arraycopy(Byte2DoubleArrayMap.this.value, this.next + 1, Byte2DoubleArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Byte2DoubleArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Byte2DoubleMap.Entry> action) {
                    int max = Byte2DoubleArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Byte2DoubleMap.Entry> spliterator() {
            return new EntrySetSpliterator(0, Byte2DoubleArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Byte2DoubleMap.Entry> action) {
            int max = Byte2DoubleArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Byte2DoubleMap.Entry> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Byte2DoubleArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Byte2DoubleArrayMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Byte)) {
                return false;
            }
            if (e.getValue() == null || !(e.getValue() instanceof Double)) {
                return false;
            }
            byte k = (Byte)e.getKey();
            return Byte2DoubleArrayMap.this.containsKey(k) && Double.doubleToRawLongBits(Byte2DoubleArrayMap.this.get(k)) == Double.doubleToRawLongBits((Double)e.getValue());
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Byte)) {
                return false;
            }
            if (e.getValue() == null || !(e.getValue() instanceof Double)) {
                return false;
            }
            byte k = (Byte)e.getKey();
            double v = (Double)e.getValue();
            int oldPos = Byte2DoubleArrayMap.this.findKey(k);
            if (oldPos == -1 || Double.doubleToRawLongBits(v) != Double.doubleToRawLongBits(Byte2DoubleArrayMap.this.value[oldPos])) {
                return false;
            }
            int tail = Byte2DoubleArrayMap.this.size - oldPos - 1;
            System.arraycopy(Byte2DoubleArrayMap.this.key, oldPos + 1, Byte2DoubleArrayMap.this.key, oldPos, tail);
            System.arraycopy(Byte2DoubleArrayMap.this.value, oldPos + 1, Byte2DoubleArrayMap.this.value, oldPos, tail);
            --Byte2DoubleArrayMap.this.size;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Byte2DoubleMap.Entry>
        implements ObjectSpliterator<Byte2DoubleMap.Entry> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Byte2DoubleMap.Entry get(int location) {
                return new MapEntry(location);
            }

            protected final EntrySetSpliterator makeForSplit(int pos, int maxPos) {
                return new EntrySetSpliterator(pos, maxPos);
            }
        }
    }

    private final class KeySet
    extends AbstractByteSet {
        private KeySet() {
        }

        @Override
        public boolean contains(byte k) {
            return Byte2DoubleArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(byte k) {
            int oldPos = Byte2DoubleArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Byte2DoubleArrayMap.this.size - oldPos - 1;
            System.arraycopy(Byte2DoubleArrayMap.this.key, oldPos + 1, Byte2DoubleArrayMap.this.key, oldPos, tail);
            System.arraycopy(Byte2DoubleArrayMap.this.value, oldPos + 1, Byte2DoubleArrayMap.this.value, oldPos, tail);
            --Byte2DoubleArrayMap.this.size;
            return true;
        }

        @Override
        public ByteIterator iterator() {
            return new ByteIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Byte2DoubleArrayMap.this.size;
                }

                @Override
                public byte nextByte() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Byte2DoubleArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Byte2DoubleArrayMap.this.size - this.pos;
                    System.arraycopy(Byte2DoubleArrayMap.this.key, this.pos, Byte2DoubleArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Byte2DoubleArrayMap.this.value, this.pos, Byte2DoubleArrayMap.this.value, this.pos - 1, tail);
                    --Byte2DoubleArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(ByteConsumer action) {
                    byte[] key = Byte2DoubleArrayMap.this.key;
                    int max = Byte2DoubleArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ByteSpliterator spliterator() {
            return new KeySetSpliterator(0, Byte2DoubleArrayMap.this.size);
        }

        @Override
        public void forEach(ByteConsumer action) {
            byte[] key = Byte2DoubleArrayMap.this.key;
            int max = Byte2DoubleArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Byte2DoubleArrayMap.this.size;
        }

        @Override
        public void clear() {
            Byte2DoubleArrayMap.this.clear();
        }

        final class KeySetSpliterator
        extends ByteSpliterators.EarlyBindingSizeIndexBasedSpliterator
        implements ByteSpliterator {
            KeySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16721;
            }

            @Override
            protected final byte get(int location) {
                return Byte2DoubleArrayMap.this.key[location];
            }

            @Override
            protected final KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(ByteConsumer action) {
                byte[] key = Byte2DoubleArrayMap.this.key;
                int max = Byte2DoubleArrayMap.this.size;
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
            return Byte2DoubleArrayMap.this.containsValue(v);
        }

        @Override
        public DoubleIterator iterator() {
            return new DoubleIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Byte2DoubleArrayMap.this.size;
                }

                @Override
                public double nextDouble() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Byte2DoubleArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Byte2DoubleArrayMap.this.size - this.pos;
                    System.arraycopy(Byte2DoubleArrayMap.this.key, this.pos, Byte2DoubleArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Byte2DoubleArrayMap.this.value, this.pos, Byte2DoubleArrayMap.this.value, this.pos - 1, tail);
                    --Byte2DoubleArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(DoubleConsumer action) {
                    double[] value = Byte2DoubleArrayMap.this.value;
                    int max = Byte2DoubleArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public DoubleSpliterator spliterator() {
            return new ValuesSpliterator(0, Byte2DoubleArrayMap.this.size);
        }

        @Override
        public void forEach(DoubleConsumer action) {
            double[] value = Byte2DoubleArrayMap.this.value;
            int max = Byte2DoubleArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Byte2DoubleArrayMap.this.size;
        }

        @Override
        public void clear() {
            Byte2DoubleArrayMap.this.clear();
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
                return Byte2DoubleArrayMap.this.value[location];
            }

            @Override
            protected final ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(DoubleConsumer action) {
                double[] value = Byte2DoubleArrayMap.this.value;
                int max = Byte2DoubleArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Byte2DoubleMap.Entry,
    Map.Entry<Byte, Double>,
    ByteDoublePair {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public byte getByteKey() {
            return Byte2DoubleArrayMap.this.key[this.index];
        }

        @Override
        public byte leftByte() {
            return Byte2DoubleArrayMap.this.key[this.index];
        }

        @Override
        public double getDoubleValue() {
            return Byte2DoubleArrayMap.this.value[this.index];
        }

        @Override
        public double rightDouble() {
            return Byte2DoubleArrayMap.this.value[this.index];
        }

        @Override
        public double setValue(double v) {
            double oldValue = Byte2DoubleArrayMap.this.value[this.index];
            Byte2DoubleArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        @Override
        public ByteDoublePair right(double v) {
            Byte2DoubleArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Byte getKey() {
            return Byte2DoubleArrayMap.this.key[this.index];
        }

        @Override
        @Deprecated
        public Double getValue() {
            return Byte2DoubleArrayMap.this.value[this.index];
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
            return Byte2DoubleArrayMap.this.key[this.index] == (Byte)e.getKey() && Double.doubleToRawLongBits(Byte2DoubleArrayMap.this.value[this.index]) == Double.doubleToRawLongBits((Double)e.getValue());
        }

        @Override
        public int hashCode() {
            return Byte2DoubleArrayMap.this.key[this.index] ^ HashCommon.double2int(Byte2DoubleArrayMap.this.value[this.index]);
        }

        public String toString() {
            return Byte2DoubleArrayMap.this.key[this.index] + "=>" + Byte2DoubleArrayMap.this.value[this.index];
        }
    }
}

