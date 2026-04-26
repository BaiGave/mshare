/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.objects;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.floats.AbstractFloatCollection;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatCollection;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import it.unimi.dsi.fastutil.floats.FloatIterator;
import it.unimi.dsi.fastutil.floats.FloatSpliterator;
import it.unimi.dsi.fastutil.floats.FloatSpliterators;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.AbstractReference2FloatMap;
import it.unimi.dsi.fastutil.objects.AbstractReferenceSet;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterators;
import it.unimi.dsi.fastutil.objects.Reference2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import it.unimi.dsi.fastutil.objects.ReferenceFloatPair;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class Reference2FloatArrayMap<K>
extends AbstractReference2FloatMap<K>
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient Object[] key;
    protected transient float[] value;
    protected int size;
    protected transient Reference2FloatMap.FastEntrySet<K> entries;
    protected transient ReferenceSet<K> keys;
    protected transient FloatCollection values;

    public Reference2FloatArrayMap(Object[] key, float[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Reference2FloatArrayMap() {
        this.key = ObjectArrays.EMPTY_ARRAY;
        this.value = FloatArrays.EMPTY_ARRAY;
    }

    public Reference2FloatArrayMap(int capacity) {
        this.key = new Object[capacity];
        this.value = new float[capacity];
    }

    public Reference2FloatArrayMap(Reference2FloatMap<K> m) {
        this(m.size());
        int i = 0;
        for (Reference2FloatMap.Entry entry : m.reference2FloatEntrySet()) {
            this.key[i] = entry.getKey();
            this.value[i] = entry.getFloatValue();
            ++i;
        }
        this.size = i;
    }

    public Reference2FloatArrayMap(Map<? extends K, ? extends Float> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<K, Float> e : m.entrySet()) {
            this.key[i] = e.getKey();
            this.value[i] = e.getValue().floatValue();
            ++i;
        }
        this.size = i;
    }

    public Reference2FloatArrayMap(Object[] key, float[] value, int size) {
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

    public Reference2FloatMap.FastEntrySet<K> reference2FloatEntrySet() {
        if (this.entries == null) {
            this.entries = new EntrySet();
        }
        return this.entries;
    }

    private int findKey(Object k) {
        Object[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (key[i] != k) continue;
            return i;
        }
        return -1;
    }

    @Override
    public float getFloat(Object k) {
        Object[] key = this.key;
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
    public boolean containsValue(float v) {
        float[] value = this.value;
        int i = this.size;
        while (i-- != 0) {
            if (Float.floatToRawIntBits(value[i]) != Float.floatToRawIntBits(v)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public float put(K k, float v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            float oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return oldValue;
        }
        if (this.size == this.key.length) {
            Object[] newKey = new Object[this.size == 0 ? 2 : this.size * 2];
            float[] newValue = new float[this.size == 0 ? 2 : this.size * 2];
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
    public float removeFloat(Object k) {
        int oldPos = this.findKey(k);
        if (oldPos == -1) {
            return this.defRetValue;
        }
        float oldValue = this.value[oldPos];
        int tail = this.size - oldPos - 1;
        System.arraycopy(this.key, oldPos + 1, this.key, oldPos, tail);
        System.arraycopy(this.value, oldPos + 1, this.value, oldPos, tail);
        --this.size;
        this.key[this.size] = null;
        return oldValue;
    }

    @Override
    public ReferenceSet<K> keySet() {
        if (this.keys == null) {
            this.keys = new KeySet();
        }
        return this.keys;
    }

    @Override
    public FloatCollection values() {
        if (this.values == null) {
            this.values = new ValuesCollection();
        }
        return this.values;
    }

    public Reference2FloatArrayMap<K> clone() {
        Reference2FloatArrayMap c;
        try {
            c = (Reference2FloatArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (Object[])this.key.clone();
        c.value = (float[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        Object[] key = this.key;
        float[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeObject(key[i]);
            s.writeFloat(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new Object[this.size];
        Object[] key = this.key;
        this.value = new float[this.size];
        float[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readObject();
            value[i] = s.readFloat();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Reference2FloatMap.Entry<K>>
    implements Reference2FloatMap.FastEntrySet<K> {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Reference2FloatMap.Entry<K>> iterator() {
            return new ObjectIterator<Reference2FloatMap.Entry<K>>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Reference2FloatArrayMap.this.size;
                }

                @Override
                public Reference2FloatMap.Entry<K> next() {
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
                    int tail = Reference2FloatArrayMap.this.size-- - this.next--;
                    System.arraycopy(Reference2FloatArrayMap.this.key, this.next + 1, Reference2FloatArrayMap.this.key, this.next, tail);
                    System.arraycopy(Reference2FloatArrayMap.this.value, this.next + 1, Reference2FloatArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Reference2FloatArrayMap.this.key[Reference2FloatArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Reference2FloatArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Reference2FloatMap.Entry<K>> action) {
                    int max = Reference2FloatArrayMap.this.size;
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
        public ObjectIterator<Reference2FloatMap.Entry<K>> fastIterator() {
            return new ObjectIterator<Reference2FloatMap.Entry<K>>(){
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
                    return this.next < Reference2FloatArrayMap.this.size;
                }

                @Override
                public Reference2FloatMap.Entry<K> next() {
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
                    int tail = Reference2FloatArrayMap.this.size-- - this.next--;
                    System.arraycopy(Reference2FloatArrayMap.this.key, this.next + 1, Reference2FloatArrayMap.this.key, this.next, tail);
                    System.arraycopy(Reference2FloatArrayMap.this.value, this.next + 1, Reference2FloatArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Reference2FloatArrayMap.this.key[Reference2FloatArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Reference2FloatArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Reference2FloatMap.Entry<K>> action) {
                    int max = Reference2FloatArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Reference2FloatMap.Entry<K>> spliterator() {
            return new EntrySetSpliterator(0, Reference2FloatArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Reference2FloatMap.Entry<K>> action) {
            int max = Reference2FloatArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Reference2FloatMap.Entry<K>> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Reference2FloatArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Reference2FloatArrayMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getValue() == null || !(e.getValue() instanceof Float)) {
                return false;
            }
            Object k = e.getKey();
            return Reference2FloatArrayMap.this.containsKey(k) && Float.floatToRawIntBits(Reference2FloatArrayMap.this.getFloat(k)) == Float.floatToRawIntBits(((Float)e.getValue()).floatValue());
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getValue() == null || !(e.getValue() instanceof Float)) {
                return false;
            }
            Object k = e.getKey();
            float v = ((Float)e.getValue()).floatValue();
            int oldPos = Reference2FloatArrayMap.this.findKey(k);
            if (oldPos == -1 || Float.floatToRawIntBits(v) != Float.floatToRawIntBits(Reference2FloatArrayMap.this.value[oldPos])) {
                return false;
            }
            int tail = Reference2FloatArrayMap.this.size - oldPos - 1;
            System.arraycopy(Reference2FloatArrayMap.this.key, oldPos + 1, Reference2FloatArrayMap.this.key, oldPos, tail);
            System.arraycopy(Reference2FloatArrayMap.this.value, oldPos + 1, Reference2FloatArrayMap.this.value, oldPos, tail);
            --Reference2FloatArrayMap.this.size;
            Reference2FloatArrayMap.this.key[Reference2FloatArrayMap.this.size] = null;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Reference2FloatMap.Entry<K>>
        implements ObjectSpliterator<Reference2FloatMap.Entry<K>> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Reference2FloatMap.Entry<K> get(int location) {
                return new MapEntry(location);
            }

            protected final it.unimi.dsi.fastutil.objects.Reference2FloatArrayMap$EntrySet.EntrySetSpliterator makeForSplit(int pos, int maxPos) {
                return new EntrySetSpliterator(pos, maxPos);
            }
        }
    }

    private final class KeySet
    extends AbstractReferenceSet<K> {
        private KeySet() {
        }

        @Override
        public boolean contains(Object k) {
            return Reference2FloatArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(Object k) {
            int oldPos = Reference2FloatArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Reference2FloatArrayMap.this.size - oldPos - 1;
            System.arraycopy(Reference2FloatArrayMap.this.key, oldPos + 1, Reference2FloatArrayMap.this.key, oldPos, tail);
            System.arraycopy(Reference2FloatArrayMap.this.value, oldPos + 1, Reference2FloatArrayMap.this.value, oldPos, tail);
            --Reference2FloatArrayMap.this.size;
            Reference2FloatArrayMap.this.key[Reference2FloatArrayMap.this.size] = null;
            return true;
        }

        @Override
        public ObjectIterator<K> iterator() {
            return new ObjectIterator<K>(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Reference2FloatArrayMap.this.size;
                }

                @Override
                public K next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Reference2FloatArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Reference2FloatArrayMap.this.size - this.pos;
                    System.arraycopy(Reference2FloatArrayMap.this.key, this.pos, Reference2FloatArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Reference2FloatArrayMap.this.value, this.pos, Reference2FloatArrayMap.this.value, this.pos - 1, tail);
                    --Reference2FloatArrayMap.this.size;
                    --this.pos;
                    Reference2FloatArrayMap.this.key[Reference2FloatArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(Consumer<? super K> action) {
                    Object[] key = Reference2FloatArrayMap.this.key;
                    int max = Reference2FloatArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<K> spliterator() {
            return new KeySetSpliterator(0, Reference2FloatArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super K> action) {
            Object[] key = Reference2FloatArrayMap.this.key;
            int max = Reference2FloatArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Reference2FloatArrayMap.this.size;
        }

        @Override
        public void clear() {
            Reference2FloatArrayMap.this.clear();
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
                return Reference2FloatArrayMap.this.key[location];
            }

            protected final it.unimi.dsi.fastutil.objects.Reference2FloatArrayMap$KeySet.KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(Consumer<? super K> action) {
                Object[] key = Reference2FloatArrayMap.this.key;
                int max = Reference2FloatArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(key[this.pos++]);
                }
            }
        }
    }

    private final class ValuesCollection
    extends AbstractFloatCollection {
        private ValuesCollection() {
        }

        @Override
        public boolean contains(float v) {
            return Reference2FloatArrayMap.this.containsValue(v);
        }

        @Override
        public FloatIterator iterator() {
            return new FloatIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Reference2FloatArrayMap.this.size;
                }

                @Override
                public float nextFloat() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Reference2FloatArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Reference2FloatArrayMap.this.size - this.pos;
                    System.arraycopy(Reference2FloatArrayMap.this.key, this.pos, Reference2FloatArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Reference2FloatArrayMap.this.value, this.pos, Reference2FloatArrayMap.this.value, this.pos - 1, tail);
                    --Reference2FloatArrayMap.this.size;
                    --this.pos;
                    Reference2FloatArrayMap.this.key[Reference2FloatArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(FloatConsumer action) {
                    float[] value = Reference2FloatArrayMap.this.value;
                    int max = Reference2FloatArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public FloatSpliterator spliterator() {
            return new ValuesSpliterator(0, Reference2FloatArrayMap.this.size);
        }

        @Override
        public void forEach(FloatConsumer action) {
            float[] value = Reference2FloatArrayMap.this.value;
            int max = Reference2FloatArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Reference2FloatArrayMap.this.size;
        }

        @Override
        public void clear() {
            Reference2FloatArrayMap.this.clear();
        }

        final class ValuesSpliterator
        extends FloatSpliterators.EarlyBindingSizeIndexBasedSpliterator
        implements FloatSpliterator {
            ValuesSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16720;
            }

            @Override
            protected final float get(int location) {
                return Reference2FloatArrayMap.this.value[location];
            }

            protected final it.unimi.dsi.fastutil.objects.Reference2FloatArrayMap$ValuesCollection.ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(FloatConsumer action) {
                float[] value = Reference2FloatArrayMap.this.value;
                int max = Reference2FloatArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Reference2FloatMap.Entry<K>,
    Map.Entry<K, Float>,
    ReferenceFloatPair<K> {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public K getKey() {
            return Reference2FloatArrayMap.this.key[this.index];
        }

        @Override
        public K left() {
            return Reference2FloatArrayMap.this.key[this.index];
        }

        @Override
        public float getFloatValue() {
            return Reference2FloatArrayMap.this.value[this.index];
        }

        @Override
        public float rightFloat() {
            return Reference2FloatArrayMap.this.value[this.index];
        }

        @Override
        public float setValue(float v) {
            float oldValue = Reference2FloatArrayMap.this.value[this.index];
            Reference2FloatArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        @Override
        public ReferenceFloatPair<K> right(float v) {
            Reference2FloatArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Float getValue() {
            return Float.valueOf(Reference2FloatArrayMap.this.value[this.index]);
        }

        @Override
        @Deprecated
        public Float setValue(Float v) {
            return Float.valueOf(this.setValue(v.floatValue()));
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            return Reference2FloatArrayMap.this.key[this.index] == e.getKey() && Float.floatToRawIntBits(Reference2FloatArrayMap.this.value[this.index]) == Float.floatToRawIntBits(((Float)e.getValue()).floatValue());
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(Reference2FloatArrayMap.this.key[this.index]) ^ HashCommon.float2int(Reference2FloatArrayMap.this.value[this.index]);
        }

        public String toString() {
            return Reference2FloatArrayMap.this.key[this.index] + "=>" + Reference2FloatArrayMap.this.value[this.index];
        }
    }
}

