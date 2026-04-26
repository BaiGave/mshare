/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.floats;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.floats.AbstractFloat2IntMap;
import it.unimi.dsi.fastutil.floats.AbstractFloatSet;
import it.unimi.dsi.fastutil.floats.Float2IntMap;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import it.unimi.dsi.fastutil.floats.FloatIntPair;
import it.unimi.dsi.fastutil.floats.FloatIterator;
import it.unimi.dsi.fastutil.floats.FloatSet;
import it.unimi.dsi.fastutil.floats.FloatSpliterator;
import it.unimi.dsi.fastutil.floats.FloatSpliterators;
import it.unimi.dsi.fastutil.ints.AbstractIntCollection;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
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
import java.util.function.IntConsumer;

public class Float2IntArrayMap
extends AbstractFloat2IntMap
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient float[] key;
    protected transient int[] value;
    protected int size;
    protected transient Float2IntMap.FastEntrySet entries;
    protected transient FloatSet keys;
    protected transient IntCollection values;

    public Float2IntArrayMap(float[] key, int[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Float2IntArrayMap() {
        this.key = FloatArrays.EMPTY_ARRAY;
        this.value = IntArrays.EMPTY_ARRAY;
    }

    public Float2IntArrayMap(int capacity) {
        this.key = new float[capacity];
        this.value = new int[capacity];
    }

    public Float2IntArrayMap(Float2IntMap m) {
        this(m.size());
        int i = 0;
        for (Float2IntMap.Entry e : m.float2IntEntrySet()) {
            this.key[i] = e.getFloatKey();
            this.value[i] = e.getIntValue();
            ++i;
        }
        this.size = i;
    }

    public Float2IntArrayMap(Map<? extends Float, ? extends Integer> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<? extends Float, ? extends Integer> e : m.entrySet()) {
            this.key[i] = e.getKey().floatValue();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Float2IntArrayMap(float[] key, int[] value, int size) {
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

    public Float2IntMap.FastEntrySet float2IntEntrySet() {
        if (this.entries == null) {
            this.entries = new EntrySet();
        }
        return this.entries;
    }

    private int findKey(float k) {
        float[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (Float.floatToRawIntBits(key[i]) != Float.floatToRawIntBits(k)) continue;
            return i;
        }
        return -1;
    }

    @Override
    public int get(float k) {
        float[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (Float.floatToRawIntBits(key[i]) != Float.floatToRawIntBits(k)) continue;
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
    public boolean containsKey(float k) {
        return this.findKey(k) != -1;
    }

    @Override
    public boolean containsValue(int v) {
        int[] value = this.value;
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
    public int put(float k, int v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            int oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return oldValue;
        }
        if (this.size == this.key.length) {
            float[] newKey = new float[this.size == 0 ? 2 : this.size * 2];
            int[] newValue = new int[this.size == 0 ? 2 : this.size * 2];
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
    public int remove(float k) {
        int oldPos = this.findKey(k);
        if (oldPos == -1) {
            return this.defRetValue;
        }
        int oldValue = this.value[oldPos];
        int tail = this.size - oldPos - 1;
        System.arraycopy(this.key, oldPos + 1, this.key, oldPos, tail);
        System.arraycopy(this.value, oldPos + 1, this.value, oldPos, tail);
        --this.size;
        return oldValue;
    }

    @Override
    public FloatSet keySet() {
        if (this.keys == null) {
            this.keys = new KeySet();
        }
        return this.keys;
    }

    @Override
    public IntCollection values() {
        if (this.values == null) {
            this.values = new ValuesCollection();
        }
        return this.values;
    }

    public Float2IntArrayMap clone() {
        Float2IntArrayMap c;
        try {
            c = (Float2IntArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (float[])this.key.clone();
        c.value = (int[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        float[] key = this.key;
        int[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeFloat(key[i]);
            s.writeInt(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new float[this.size];
        float[] key = this.key;
        this.value = new int[this.size];
        int[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readFloat();
            value[i] = s.readInt();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Float2IntMap.Entry>
    implements Float2IntMap.FastEntrySet {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Float2IntMap.Entry> iterator() {
            return new ObjectIterator<Float2IntMap.Entry>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Float2IntArrayMap.this.size;
                }

                @Override
                public Float2IntMap.Entry next() {
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
                    int tail = Float2IntArrayMap.this.size-- - this.next--;
                    System.arraycopy(Float2IntArrayMap.this.key, this.next + 1, Float2IntArrayMap.this.key, this.next, tail);
                    System.arraycopy(Float2IntArrayMap.this.value, this.next + 1, Float2IntArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Float2IntArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Float2IntMap.Entry> action) {
                    int max = Float2IntArrayMap.this.size;
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
        public ObjectIterator<Float2IntMap.Entry> fastIterator() {
            return new ObjectIterator<Float2IntMap.Entry>(){
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
                    return this.next < Float2IntArrayMap.this.size;
                }

                @Override
                public Float2IntMap.Entry next() {
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
                    int tail = Float2IntArrayMap.this.size-- - this.next--;
                    System.arraycopy(Float2IntArrayMap.this.key, this.next + 1, Float2IntArrayMap.this.key, this.next, tail);
                    System.arraycopy(Float2IntArrayMap.this.value, this.next + 1, Float2IntArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Float2IntArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Float2IntMap.Entry> action) {
                    int max = Float2IntArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Float2IntMap.Entry> spliterator() {
            return new EntrySetSpliterator(0, Float2IntArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Float2IntMap.Entry> action) {
            int max = Float2IntArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Float2IntMap.Entry> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Float2IntArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Float2IntArrayMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Float)) {
                return false;
            }
            if (e.getValue() == null || !(e.getValue() instanceof Integer)) {
                return false;
            }
            float k = ((Float)e.getKey()).floatValue();
            return Float2IntArrayMap.this.containsKey(k) && Float2IntArrayMap.this.get(k) == ((Integer)e.getValue()).intValue();
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Float)) {
                return false;
            }
            if (e.getValue() == null || !(e.getValue() instanceof Integer)) {
                return false;
            }
            float k = ((Float)e.getKey()).floatValue();
            int v = (Integer)e.getValue();
            int oldPos = Float2IntArrayMap.this.findKey(k);
            if (oldPos == -1 || v != Float2IntArrayMap.this.value[oldPos]) {
                return false;
            }
            int tail = Float2IntArrayMap.this.size - oldPos - 1;
            System.arraycopy(Float2IntArrayMap.this.key, oldPos + 1, Float2IntArrayMap.this.key, oldPos, tail);
            System.arraycopy(Float2IntArrayMap.this.value, oldPos + 1, Float2IntArrayMap.this.value, oldPos, tail);
            --Float2IntArrayMap.this.size;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Float2IntMap.Entry>
        implements ObjectSpliterator<Float2IntMap.Entry> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Float2IntMap.Entry get(int location) {
                return new MapEntry(location);
            }

            protected final EntrySetSpliterator makeForSplit(int pos, int maxPos) {
                return new EntrySetSpliterator(pos, maxPos);
            }
        }
    }

    private final class KeySet
    extends AbstractFloatSet {
        private KeySet() {
        }

        @Override
        public boolean contains(float k) {
            return Float2IntArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(float k) {
            int oldPos = Float2IntArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Float2IntArrayMap.this.size - oldPos - 1;
            System.arraycopy(Float2IntArrayMap.this.key, oldPos + 1, Float2IntArrayMap.this.key, oldPos, tail);
            System.arraycopy(Float2IntArrayMap.this.value, oldPos + 1, Float2IntArrayMap.this.value, oldPos, tail);
            --Float2IntArrayMap.this.size;
            return true;
        }

        @Override
        public FloatIterator iterator() {
            return new FloatIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Float2IntArrayMap.this.size;
                }

                @Override
                public float nextFloat() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Float2IntArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Float2IntArrayMap.this.size - this.pos;
                    System.arraycopy(Float2IntArrayMap.this.key, this.pos, Float2IntArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Float2IntArrayMap.this.value, this.pos, Float2IntArrayMap.this.value, this.pos - 1, tail);
                    --Float2IntArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(FloatConsumer action) {
                    float[] key = Float2IntArrayMap.this.key;
                    int max = Float2IntArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public FloatSpliterator spliterator() {
            return new KeySetSpliterator(0, Float2IntArrayMap.this.size);
        }

        @Override
        public void forEach(FloatConsumer action) {
            float[] key = Float2IntArrayMap.this.key;
            int max = Float2IntArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Float2IntArrayMap.this.size;
        }

        @Override
        public void clear() {
            Float2IntArrayMap.this.clear();
        }

        final class KeySetSpliterator
        extends FloatSpliterators.EarlyBindingSizeIndexBasedSpliterator
        implements FloatSpliterator {
            KeySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16721;
            }

            @Override
            protected final float get(int location) {
                return Float2IntArrayMap.this.key[location];
            }

            @Override
            protected final KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(FloatConsumer action) {
                float[] key = Float2IntArrayMap.this.key;
                int max = Float2IntArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(key[this.pos++]);
                }
            }
        }
    }

    private final class ValuesCollection
    extends AbstractIntCollection {
        private ValuesCollection() {
        }

        @Override
        public boolean contains(int v) {
            return Float2IntArrayMap.this.containsValue(v);
        }

        @Override
        public IntIterator iterator() {
            return new IntIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Float2IntArrayMap.this.size;
                }

                @Override
                public int nextInt() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Float2IntArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Float2IntArrayMap.this.size - this.pos;
                    System.arraycopy(Float2IntArrayMap.this.key, this.pos, Float2IntArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Float2IntArrayMap.this.value, this.pos, Float2IntArrayMap.this.value, this.pos - 1, tail);
                    --Float2IntArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(IntConsumer action) {
                    int[] value = Float2IntArrayMap.this.value;
                    int max = Float2IntArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public IntSpliterator spliterator() {
            return new ValuesSpliterator(0, Float2IntArrayMap.this.size);
        }

        @Override
        public void forEach(IntConsumer action) {
            int[] value = Float2IntArrayMap.this.value;
            int max = Float2IntArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Float2IntArrayMap.this.size;
        }

        @Override
        public void clear() {
            Float2IntArrayMap.this.clear();
        }

        final class ValuesSpliterator
        extends IntSpliterators.EarlyBindingSizeIndexBasedSpliterator
        implements IntSpliterator {
            ValuesSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16720;
            }

            @Override
            protected final int get(int location) {
                return Float2IntArrayMap.this.value[location];
            }

            @Override
            protected final ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(IntConsumer action) {
                int[] value = Float2IntArrayMap.this.value;
                int max = Float2IntArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Float2IntMap.Entry,
    Map.Entry<Float, Integer>,
    FloatIntPair {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public float getFloatKey() {
            return Float2IntArrayMap.this.key[this.index];
        }

        @Override
        public float leftFloat() {
            return Float2IntArrayMap.this.key[this.index];
        }

        @Override
        public int getIntValue() {
            return Float2IntArrayMap.this.value[this.index];
        }

        @Override
        public int rightInt() {
            return Float2IntArrayMap.this.value[this.index];
        }

        @Override
        public int setValue(int v) {
            int oldValue = Float2IntArrayMap.this.value[this.index];
            Float2IntArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        @Override
        public FloatIntPair right(int v) {
            Float2IntArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Float getKey() {
            return Float.valueOf(Float2IntArrayMap.this.key[this.index]);
        }

        @Override
        @Deprecated
        public Integer getValue() {
            return Float2IntArrayMap.this.value[this.index];
        }

        @Override
        @Deprecated
        public Integer setValue(Integer v) {
            return this.setValue((int)v);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            return Float.floatToRawIntBits(Float2IntArrayMap.this.key[this.index]) == Float.floatToRawIntBits(((Float)e.getKey()).floatValue()) && Float2IntArrayMap.this.value[this.index] == (Integer)e.getValue();
        }

        @Override
        public int hashCode() {
            return HashCommon.float2int(Float2IntArrayMap.this.key[this.index]) ^ Float2IntArrayMap.this.value[this.index];
        }

        public String toString() {
            return Float2IntArrayMap.this.key[this.index] + "=>" + Float2IntArrayMap.this.value[this.index];
        }
    }
}

