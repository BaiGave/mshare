/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.floats;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.floats.AbstractFloat2ReferenceMap;
import it.unimi.dsi.fastutil.floats.AbstractFloatSet;
import it.unimi.dsi.fastutil.floats.Float2ReferenceArrayMap;
import it.unimi.dsi.fastutil.floats.Float2ReferenceMap;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import it.unimi.dsi.fastutil.floats.FloatIterator;
import it.unimi.dsi.fastutil.floats.FloatReferencePair;
import it.unimi.dsi.fastutil.floats.FloatSet;
import it.unimi.dsi.fastutil.floats.FloatSpliterator;
import it.unimi.dsi.fastutil.floats.FloatSpliterators;
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

public class Float2ReferenceArrayMap<V>
extends AbstractFloat2ReferenceMap<V>
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient float[] key;
    protected transient Object[] value;
    protected int size;
    protected transient Float2ReferenceMap.FastEntrySet<V> entries;
    protected transient FloatSet keys;
    protected transient ReferenceCollection<V> values;

    public Float2ReferenceArrayMap(float[] key, Object[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Float2ReferenceArrayMap() {
        this.key = FloatArrays.EMPTY_ARRAY;
        this.value = ObjectArrays.EMPTY_ARRAY;
    }

    public Float2ReferenceArrayMap(int capacity) {
        this.key = new float[capacity];
        this.value = new Object[capacity];
    }

    public Float2ReferenceArrayMap(Float2ReferenceMap<V> m) {
        this(m.size());
        int i = 0;
        for (Float2ReferenceMap.Entry entry : m.float2ReferenceEntrySet()) {
            this.key[i] = entry.getFloatKey();
            this.value[i] = entry.getValue();
            ++i;
        }
        this.size = i;
    }

    public Float2ReferenceArrayMap(Map<? extends Float, ? extends V> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<Float, V> e : m.entrySet()) {
            this.key[i] = e.getKey().floatValue();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Float2ReferenceArrayMap(float[] key, Object[] value, int size) {
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

    public Float2ReferenceMap.FastEntrySet<V> float2ReferenceEntrySet() {
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
    public V get(float k) {
        float[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (Float.floatToRawIntBits(key[i]) != Float.floatToRawIntBits(k)) continue;
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
    public boolean containsKey(float k) {
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
    public V put(float k, V v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            Object oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return (V)oldValue;
        }
        if (this.size == this.key.length) {
            float[] newKey = new float[this.size == 0 ? 2 : this.size * 2];
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
    public V remove(float k) {
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
    public FloatSet keySet() {
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

    public Float2ReferenceArrayMap<V> clone() {
        Float2ReferenceArrayMap c;
        try {
            c = (Float2ReferenceArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (float[])this.key.clone();
        c.value = (Object[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        float[] key = this.key;
        Object[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeFloat(key[i]);
            s.writeObject(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new float[this.size];
        float[] key = this.key;
        this.value = new Object[this.size];
        Object[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readFloat();
            value[i] = s.readObject();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Float2ReferenceMap.Entry<V>>
    implements Float2ReferenceMap.FastEntrySet<V> {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Float2ReferenceMap.Entry<V>> iterator() {
            return new ObjectIterator<Float2ReferenceMap.Entry<V>>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Float2ReferenceArrayMap.this.size;
                }

                @Override
                public Float2ReferenceMap.Entry<V> next() {
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
                    int tail = Float2ReferenceArrayMap.this.size-- - this.next--;
                    System.arraycopy(Float2ReferenceArrayMap.this.key, this.next + 1, Float2ReferenceArrayMap.this.key, this.next, tail);
                    System.arraycopy(Float2ReferenceArrayMap.this.value, this.next + 1, Float2ReferenceArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Float2ReferenceArrayMap.this.value[Float2ReferenceArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Float2ReferenceArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Float2ReferenceMap.Entry<V>> action) {
                    int max = Float2ReferenceArrayMap.this.size;
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
        public ObjectIterator<Float2ReferenceMap.Entry<V>> fastIterator() {
            return new ObjectIterator<Float2ReferenceMap.Entry<V>>(){
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
                    return this.next < Float2ReferenceArrayMap.this.size;
                }

                @Override
                public Float2ReferenceMap.Entry<V> next() {
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
                    int tail = Float2ReferenceArrayMap.this.size-- - this.next--;
                    System.arraycopy(Float2ReferenceArrayMap.this.key, this.next + 1, Float2ReferenceArrayMap.this.key, this.next, tail);
                    System.arraycopy(Float2ReferenceArrayMap.this.value, this.next + 1, Float2ReferenceArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Float2ReferenceArrayMap.this.value[Float2ReferenceArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Float2ReferenceArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Float2ReferenceMap.Entry<V>> action) {
                    int max = Float2ReferenceArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Float2ReferenceMap.Entry<V>> spliterator() {
            return new EntrySetSpliterator(0, Float2ReferenceArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Float2ReferenceMap.Entry<V>> action) {
            int max = Float2ReferenceArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Float2ReferenceMap.Entry<V>> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Float2ReferenceArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Float2ReferenceArrayMap.this.size;
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
            float k = ((Float)e.getKey()).floatValue();
            return Float2ReferenceArrayMap.this.containsKey(k) && Float2ReferenceArrayMap.this.get(k) == e.getValue();
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
            float k = ((Float)e.getKey()).floatValue();
            Object v = e.getValue();
            int oldPos = Float2ReferenceArrayMap.this.findKey(k);
            if (oldPos == -1 || v != Float2ReferenceArrayMap.this.value[oldPos]) {
                return false;
            }
            int tail = Float2ReferenceArrayMap.this.size - oldPos - 1;
            System.arraycopy(Float2ReferenceArrayMap.this.key, oldPos + 1, Float2ReferenceArrayMap.this.key, oldPos, tail);
            System.arraycopy(Float2ReferenceArrayMap.this.value, oldPos + 1, Float2ReferenceArrayMap.this.value, oldPos, tail);
            --Float2ReferenceArrayMap.this.size;
            Float2ReferenceArrayMap.this.value[Float2ReferenceArrayMap.this.size] = null;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Float2ReferenceMap.Entry<V>>
        implements ObjectSpliterator<Float2ReferenceMap.Entry<V>> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Float2ReferenceMap.Entry<V> get(int location) {
                return new MapEntry(location);
            }

            protected final it.unimi.dsi.fastutil.floats.Float2ReferenceArrayMap$EntrySet.EntrySetSpliterator makeForSplit(int pos, int maxPos) {
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
            return Float2ReferenceArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(float k) {
            int oldPos = Float2ReferenceArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Float2ReferenceArrayMap.this.size - oldPos - 1;
            System.arraycopy(Float2ReferenceArrayMap.this.key, oldPos + 1, Float2ReferenceArrayMap.this.key, oldPos, tail);
            System.arraycopy(Float2ReferenceArrayMap.this.value, oldPos + 1, Float2ReferenceArrayMap.this.value, oldPos, tail);
            --Float2ReferenceArrayMap.this.size;
            Float2ReferenceArrayMap.this.value[Float2ReferenceArrayMap.this.size] = null;
            return true;
        }

        @Override
        public FloatIterator iterator() {
            return new FloatIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Float2ReferenceArrayMap.this.size;
                }

                @Override
                public float nextFloat() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Float2ReferenceArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Float2ReferenceArrayMap.this.size - this.pos;
                    System.arraycopy(Float2ReferenceArrayMap.this.key, this.pos, Float2ReferenceArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Float2ReferenceArrayMap.this.value, this.pos, Float2ReferenceArrayMap.this.value, this.pos - 1, tail);
                    --Float2ReferenceArrayMap.this.size;
                    --this.pos;
                    Float2ReferenceArrayMap.this.value[Float2ReferenceArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(FloatConsumer action) {
                    float[] key = Float2ReferenceArrayMap.this.key;
                    int max = Float2ReferenceArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public FloatSpliterator spliterator() {
            return new KeySetSpliterator(0, Float2ReferenceArrayMap.this.size);
        }

        @Override
        public void forEach(FloatConsumer action) {
            float[] key = Float2ReferenceArrayMap.this.key;
            int max = Float2ReferenceArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Float2ReferenceArrayMap.this.size;
        }

        @Override
        public void clear() {
            Float2ReferenceArrayMap.this.clear();
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
                return Float2ReferenceArrayMap.this.key[location];
            }

            protected final it.unimi.dsi.fastutil.floats.Float2ReferenceArrayMap$KeySet.KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(FloatConsumer action) {
                float[] key = Float2ReferenceArrayMap.this.key;
                int max = Float2ReferenceArrayMap.this.size;
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
            return Float2ReferenceArrayMap.this.containsValue(v);
        }

        @Override
        public ObjectIterator<V> iterator() {
            return new ObjectIterator<V>(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Float2ReferenceArrayMap.this.size;
                }

                @Override
                public V next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Float2ReferenceArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Float2ReferenceArrayMap.this.size - this.pos;
                    System.arraycopy(Float2ReferenceArrayMap.this.key, this.pos, Float2ReferenceArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Float2ReferenceArrayMap.this.value, this.pos, Float2ReferenceArrayMap.this.value, this.pos - 1, tail);
                    --Float2ReferenceArrayMap.this.size;
                    --this.pos;
                    Float2ReferenceArrayMap.this.value[Float2ReferenceArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(Consumer<? super V> action) {
                    Object[] value = Float2ReferenceArrayMap.this.value;
                    int max = Float2ReferenceArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<V> spliterator() {
            return new ValuesSpliterator(0, Float2ReferenceArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super V> action) {
            Object[] value = Float2ReferenceArrayMap.this.value;
            int max = Float2ReferenceArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Float2ReferenceArrayMap.this.size;
        }

        @Override
        public void clear() {
            Float2ReferenceArrayMap.this.clear();
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
                return Float2ReferenceArrayMap.this.value[location];
            }

            protected final it.unimi.dsi.fastutil.floats.Float2ReferenceArrayMap$ValuesCollection.ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(Consumer<? super V> action) {
                Object[] value = Float2ReferenceArrayMap.this.value;
                int max = Float2ReferenceArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Float2ReferenceMap.Entry<V>,
    Map.Entry<Float, V>,
    FloatReferencePair<V> {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public float getFloatKey() {
            return Float2ReferenceArrayMap.this.key[this.index];
        }

        @Override
        public float leftFloat() {
            return Float2ReferenceArrayMap.this.key[this.index];
        }

        @Override
        public V getValue() {
            return Float2ReferenceArrayMap.this.value[this.index];
        }

        @Override
        public V right() {
            return Float2ReferenceArrayMap.this.value[this.index];
        }

        @Override
        public V setValue(V v) {
            Object oldValue = Float2ReferenceArrayMap.this.value[this.index];
            Float2ReferenceArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        public FloatReferencePair<V> right(V v) {
            Float2ReferenceArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Float getKey() {
            return Float.valueOf(Float2ReferenceArrayMap.this.key[this.index]);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            return Float.floatToRawIntBits(Float2ReferenceArrayMap.this.key[this.index]) == Float.floatToRawIntBits(((Float)e.getKey()).floatValue()) && Float2ReferenceArrayMap.this.value[this.index] == e.getValue();
        }

        @Override
        public int hashCode() {
            return HashCommon.float2int(Float2ReferenceArrayMap.this.key[this.index]) ^ (Float2ReferenceArrayMap.this.value[this.index] == null ? 0 : System.identityHashCode(Float2ReferenceArrayMap.this.value[this.index]));
        }

        public String toString() {
            return Float2ReferenceArrayMap.this.key[this.index] + "=>" + Float2ReferenceArrayMap.this.value[this.index];
        }
    }
}

