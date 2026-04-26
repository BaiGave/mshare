/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.ints;

import it.unimi.dsi.fastutil.ints.AbstractInt2ShortMap;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntShortPair;
import it.unimi.dsi.fastutil.ints.IntSpliterator;
import it.unimi.dsi.fastutil.ints.IntSpliterators;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterators;
import it.unimi.dsi.fastutil.shorts.AbstractShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortArrays;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortConsumer;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import it.unimi.dsi.fastutil.shorts.ShortSpliterator;
import it.unimi.dsi.fastutil.shorts.ShortSpliterators;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class Int2ShortArrayMap
extends AbstractInt2ShortMap
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient int[] key;
    protected transient short[] value;
    protected int size;
    protected transient Int2ShortMap.FastEntrySet entries;
    protected transient IntSet keys;
    protected transient ShortCollection values;

    public Int2ShortArrayMap(int[] key, short[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Int2ShortArrayMap() {
        this.key = IntArrays.EMPTY_ARRAY;
        this.value = ShortArrays.EMPTY_ARRAY;
    }

    public Int2ShortArrayMap(int capacity) {
        this.key = new int[capacity];
        this.value = new short[capacity];
    }

    public Int2ShortArrayMap(Int2ShortMap m) {
        this(m.size());
        int i = 0;
        for (Int2ShortMap.Entry e : m.int2ShortEntrySet()) {
            this.key[i] = e.getIntKey();
            this.value[i] = e.getShortValue();
            ++i;
        }
        this.size = i;
    }

    public Int2ShortArrayMap(Map<? extends Integer, ? extends Short> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<? extends Integer, ? extends Short> e : m.entrySet()) {
            this.key[i] = e.getKey();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Int2ShortArrayMap(int[] key, short[] value, int size) {
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

    public Int2ShortMap.FastEntrySet int2ShortEntrySet() {
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
    public short get(int k) {
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
    public boolean containsValue(short v) {
        short[] value = this.value;
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
    public short put(int k, short v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            short oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return oldValue;
        }
        if (this.size == this.key.length) {
            int[] newKey = new int[this.size == 0 ? 2 : this.size * 2];
            short[] newValue = new short[this.size == 0 ? 2 : this.size * 2];
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
    public short remove(int k) {
        int oldPos = this.findKey(k);
        if (oldPos == -1) {
            return this.defRetValue;
        }
        short oldValue = this.value[oldPos];
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
    public ShortCollection values() {
        if (this.values == null) {
            this.values = new ValuesCollection();
        }
        return this.values;
    }

    public Int2ShortArrayMap clone() {
        Int2ShortArrayMap c;
        try {
            c = (Int2ShortArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (int[])this.key.clone();
        c.value = (short[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        int[] key = this.key;
        short[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeInt(key[i]);
            s.writeShort(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new int[this.size];
        int[] key = this.key;
        this.value = new short[this.size];
        short[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readInt();
            value[i] = s.readShort();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Int2ShortMap.Entry>
    implements Int2ShortMap.FastEntrySet {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Int2ShortMap.Entry> iterator() {
            return new ObjectIterator<Int2ShortMap.Entry>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Int2ShortArrayMap.this.size;
                }

                @Override
                public Int2ShortMap.Entry next() {
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
                    int tail = Int2ShortArrayMap.this.size-- - this.next--;
                    System.arraycopy(Int2ShortArrayMap.this.key, this.next + 1, Int2ShortArrayMap.this.key, this.next, tail);
                    System.arraycopy(Int2ShortArrayMap.this.value, this.next + 1, Int2ShortArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Int2ShortArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Int2ShortMap.Entry> action) {
                    int max = Int2ShortArrayMap.this.size;
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
        public ObjectIterator<Int2ShortMap.Entry> fastIterator() {
            return new ObjectIterator<Int2ShortMap.Entry>(){
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
                    return this.next < Int2ShortArrayMap.this.size;
                }

                @Override
                public Int2ShortMap.Entry next() {
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
                    int tail = Int2ShortArrayMap.this.size-- - this.next--;
                    System.arraycopy(Int2ShortArrayMap.this.key, this.next + 1, Int2ShortArrayMap.this.key, this.next, tail);
                    System.arraycopy(Int2ShortArrayMap.this.value, this.next + 1, Int2ShortArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Int2ShortArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Int2ShortMap.Entry> action) {
                    int max = Int2ShortArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Int2ShortMap.Entry> spliterator() {
            return new EntrySetSpliterator(0, Int2ShortArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Int2ShortMap.Entry> action) {
            int max = Int2ShortArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Int2ShortMap.Entry> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Int2ShortArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Int2ShortArrayMap.this.size;
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
            if (e.getValue() == null || !(e.getValue() instanceof Short)) {
                return false;
            }
            int k = (Integer)e.getKey();
            return Int2ShortArrayMap.this.containsKey(k) && Int2ShortArrayMap.this.get(k) == ((Short)e.getValue()).shortValue();
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
            if (e.getValue() == null || !(e.getValue() instanceof Short)) {
                return false;
            }
            int k = (Integer)e.getKey();
            short v = (Short)e.getValue();
            int oldPos = Int2ShortArrayMap.this.findKey(k);
            if (oldPos == -1 || v != Int2ShortArrayMap.this.value[oldPos]) {
                return false;
            }
            int tail = Int2ShortArrayMap.this.size - oldPos - 1;
            System.arraycopy(Int2ShortArrayMap.this.key, oldPos + 1, Int2ShortArrayMap.this.key, oldPos, tail);
            System.arraycopy(Int2ShortArrayMap.this.value, oldPos + 1, Int2ShortArrayMap.this.value, oldPos, tail);
            --Int2ShortArrayMap.this.size;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Int2ShortMap.Entry>
        implements ObjectSpliterator<Int2ShortMap.Entry> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Int2ShortMap.Entry get(int location) {
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
            return Int2ShortArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(int k) {
            int oldPos = Int2ShortArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Int2ShortArrayMap.this.size - oldPos - 1;
            System.arraycopy(Int2ShortArrayMap.this.key, oldPos + 1, Int2ShortArrayMap.this.key, oldPos, tail);
            System.arraycopy(Int2ShortArrayMap.this.value, oldPos + 1, Int2ShortArrayMap.this.value, oldPos, tail);
            --Int2ShortArrayMap.this.size;
            return true;
        }

        @Override
        public IntIterator iterator() {
            return new IntIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Int2ShortArrayMap.this.size;
                }

                @Override
                public int nextInt() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Int2ShortArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Int2ShortArrayMap.this.size - this.pos;
                    System.arraycopy(Int2ShortArrayMap.this.key, this.pos, Int2ShortArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Int2ShortArrayMap.this.value, this.pos, Int2ShortArrayMap.this.value, this.pos - 1, tail);
                    --Int2ShortArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(IntConsumer action) {
                    int[] key = Int2ShortArrayMap.this.key;
                    int max = Int2ShortArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public IntSpliterator spliterator() {
            return new KeySetSpliterator(0, Int2ShortArrayMap.this.size);
        }

        @Override
        public void forEach(IntConsumer action) {
            int[] key = Int2ShortArrayMap.this.key;
            int max = Int2ShortArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Int2ShortArrayMap.this.size;
        }

        @Override
        public void clear() {
            Int2ShortArrayMap.this.clear();
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
                return Int2ShortArrayMap.this.key[location];
            }

            @Override
            protected final KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(IntConsumer action) {
                int[] key = Int2ShortArrayMap.this.key;
                int max = Int2ShortArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(key[this.pos++]);
                }
            }
        }
    }

    private final class ValuesCollection
    extends AbstractShortCollection {
        private ValuesCollection() {
        }

        @Override
        public boolean contains(short v) {
            return Int2ShortArrayMap.this.containsValue(v);
        }

        @Override
        public ShortIterator iterator() {
            return new ShortIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Int2ShortArrayMap.this.size;
                }

                @Override
                public short nextShort() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Int2ShortArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Int2ShortArrayMap.this.size - this.pos;
                    System.arraycopy(Int2ShortArrayMap.this.key, this.pos, Int2ShortArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Int2ShortArrayMap.this.value, this.pos, Int2ShortArrayMap.this.value, this.pos - 1, tail);
                    --Int2ShortArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(ShortConsumer action) {
                    short[] value = Int2ShortArrayMap.this.value;
                    int max = Int2ShortArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ShortSpliterator spliterator() {
            return new ValuesSpliterator(0, Int2ShortArrayMap.this.size);
        }

        @Override
        public void forEach(ShortConsumer action) {
            short[] value = Int2ShortArrayMap.this.value;
            int max = Int2ShortArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Int2ShortArrayMap.this.size;
        }

        @Override
        public void clear() {
            Int2ShortArrayMap.this.clear();
        }

        final class ValuesSpliterator
        extends ShortSpliterators.EarlyBindingSizeIndexBasedSpliterator
        implements ShortSpliterator {
            ValuesSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16720;
            }

            @Override
            protected final short get(int location) {
                return Int2ShortArrayMap.this.value[location];
            }

            @Override
            protected final ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(ShortConsumer action) {
                short[] value = Int2ShortArrayMap.this.value;
                int max = Int2ShortArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Int2ShortMap.Entry,
    Map.Entry<Integer, Short>,
    IntShortPair {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public int getIntKey() {
            return Int2ShortArrayMap.this.key[this.index];
        }

        @Override
        public int leftInt() {
            return Int2ShortArrayMap.this.key[this.index];
        }

        @Override
        public short getShortValue() {
            return Int2ShortArrayMap.this.value[this.index];
        }

        @Override
        public short rightShort() {
            return Int2ShortArrayMap.this.value[this.index];
        }

        @Override
        public short setValue(short v) {
            short oldValue = Int2ShortArrayMap.this.value[this.index];
            Int2ShortArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        @Override
        public IntShortPair right(short v) {
            Int2ShortArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Integer getKey() {
            return Int2ShortArrayMap.this.key[this.index];
        }

        @Override
        @Deprecated
        public Short getValue() {
            return Int2ShortArrayMap.this.value[this.index];
        }

        @Override
        @Deprecated
        public Short setValue(Short v) {
            return this.setValue((short)v);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            return Int2ShortArrayMap.this.key[this.index] == (Integer)e.getKey() && Int2ShortArrayMap.this.value[this.index] == (Short)e.getValue();
        }

        @Override
        public int hashCode() {
            return Int2ShortArrayMap.this.key[this.index] ^ Int2ShortArrayMap.this.value[this.index];
        }

        public String toString() {
            return Int2ShortArrayMap.this.key[this.index] + "=>" + Int2ShortArrayMap.this.value[this.index];
        }
    }
}

