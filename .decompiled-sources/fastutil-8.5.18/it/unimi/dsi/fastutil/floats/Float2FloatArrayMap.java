/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.floats;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.floats.AbstractFloat2FloatMap;
import it.unimi.dsi.fastutil.floats.AbstractFloatCollection;
import it.unimi.dsi.fastutil.floats.AbstractFloatSet;
import it.unimi.dsi.fastutil.floats.Float2FloatMap;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatCollection;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import it.unimi.dsi.fastutil.floats.FloatFloatPair;
import it.unimi.dsi.fastutil.floats.FloatIterator;
import it.unimi.dsi.fastutil.floats.FloatSet;
import it.unimi.dsi.fastutil.floats.FloatSpliterator;
import it.unimi.dsi.fastutil.floats.FloatSpliterators;
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

public class Float2FloatArrayMap
extends AbstractFloat2FloatMap
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient float[] key;
    protected transient float[] value;
    protected int size;
    protected transient Float2FloatMap.FastEntrySet entries;
    protected transient FloatSet keys;
    protected transient FloatCollection values;

    public Float2FloatArrayMap(float[] key, float[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Float2FloatArrayMap() {
        this.key = FloatArrays.EMPTY_ARRAY;
        this.value = FloatArrays.EMPTY_ARRAY;
    }

    public Float2FloatArrayMap(int capacity) {
        this.key = new float[capacity];
        this.value = new float[capacity];
    }

    public Float2FloatArrayMap(Float2FloatMap m) {
        this(m.size());
        int i = 0;
        for (Float2FloatMap.Entry e : m.float2FloatEntrySet()) {
            this.key[i] = e.getFloatKey();
            this.value[i] = e.getFloatValue();
            ++i;
        }
        this.size = i;
    }

    public Float2FloatArrayMap(Map<? extends Float, ? extends Float> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<? extends Float, ? extends Float> e : m.entrySet()) {
            this.key[i] = e.getKey().floatValue();
            this.value[i] = e.getValue().floatValue();
            ++i;
        }
        this.size = i;
    }

    public Float2FloatArrayMap(float[] key, float[] value, int size) {
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

    public Float2FloatMap.FastEntrySet float2FloatEntrySet() {
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
    public float get(float k) {
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
    public float put(float k, float v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            float oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return oldValue;
        }
        if (this.size == this.key.length) {
            float[] newKey = new float[this.size == 0 ? 2 : this.size * 2];
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
    public float remove(float k) {
        int oldPos = this.findKey(k);
        if (oldPos == -1) {
            return this.defRetValue;
        }
        float oldValue = this.value[oldPos];
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
    public FloatCollection values() {
        if (this.values == null) {
            this.values = new ValuesCollection();
        }
        return this.values;
    }

    public Float2FloatArrayMap clone() {
        Float2FloatArrayMap c;
        try {
            c = (Float2FloatArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (float[])this.key.clone();
        c.value = (float[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        float[] key = this.key;
        float[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeFloat(key[i]);
            s.writeFloat(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new float[this.size];
        float[] key = this.key;
        this.value = new float[this.size];
        float[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readFloat();
            value[i] = s.readFloat();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Float2FloatMap.Entry>
    implements Float2FloatMap.FastEntrySet {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Float2FloatMap.Entry> iterator() {
            return new ObjectIterator<Float2FloatMap.Entry>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Float2FloatArrayMap.this.size;
                }

                @Override
                public Float2FloatMap.Entry next() {
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
                    int tail = Float2FloatArrayMap.this.size-- - this.next--;
                    System.arraycopy(Float2FloatArrayMap.this.key, this.next + 1, Float2FloatArrayMap.this.key, this.next, tail);
                    System.arraycopy(Float2FloatArrayMap.this.value, this.next + 1, Float2FloatArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Float2FloatArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Float2FloatMap.Entry> action) {
                    int max = Float2FloatArrayMap.this.size;
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
        public ObjectIterator<Float2FloatMap.Entry> fastIterator() {
            return new ObjectIterator<Float2FloatMap.Entry>(){
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
                    return this.next < Float2FloatArrayMap.this.size;
                }

                @Override
                public Float2FloatMap.Entry next() {
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
                    int tail = Float2FloatArrayMap.this.size-- - this.next--;
                    System.arraycopy(Float2FloatArrayMap.this.key, this.next + 1, Float2FloatArrayMap.this.key, this.next, tail);
                    System.arraycopy(Float2FloatArrayMap.this.value, this.next + 1, Float2FloatArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Float2FloatArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Float2FloatMap.Entry> action) {
                    int max = Float2FloatArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Float2FloatMap.Entry> spliterator() {
            return new EntrySetSpliterator(0, Float2FloatArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Float2FloatMap.Entry> action) {
            int max = Float2FloatArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Float2FloatMap.Entry> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Float2FloatArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Float2FloatArrayMap.this.size;
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
            if (e.getValue() == null || !(e.getValue() instanceof Float)) {
                return false;
            }
            float k = ((Float)e.getKey()).floatValue();
            return Float2FloatArrayMap.this.containsKey(k) && Float.floatToRawIntBits(Float2FloatArrayMap.this.get(k)) == Float.floatToRawIntBits(((Float)e.getValue()).floatValue());
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
            if (e.getValue() == null || !(e.getValue() instanceof Float)) {
                return false;
            }
            float k = ((Float)e.getKey()).floatValue();
            float v = ((Float)e.getValue()).floatValue();
            int oldPos = Float2FloatArrayMap.this.findKey(k);
            if (oldPos == -1 || Float.floatToRawIntBits(v) != Float.floatToRawIntBits(Float2FloatArrayMap.this.value[oldPos])) {
                return false;
            }
            int tail = Float2FloatArrayMap.this.size - oldPos - 1;
            System.arraycopy(Float2FloatArrayMap.this.key, oldPos + 1, Float2FloatArrayMap.this.key, oldPos, tail);
            System.arraycopy(Float2FloatArrayMap.this.value, oldPos + 1, Float2FloatArrayMap.this.value, oldPos, tail);
            --Float2FloatArrayMap.this.size;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Float2FloatMap.Entry>
        implements ObjectSpliterator<Float2FloatMap.Entry> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Float2FloatMap.Entry get(int location) {
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
            return Float2FloatArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(float k) {
            int oldPos = Float2FloatArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Float2FloatArrayMap.this.size - oldPos - 1;
            System.arraycopy(Float2FloatArrayMap.this.key, oldPos + 1, Float2FloatArrayMap.this.key, oldPos, tail);
            System.arraycopy(Float2FloatArrayMap.this.value, oldPos + 1, Float2FloatArrayMap.this.value, oldPos, tail);
            --Float2FloatArrayMap.this.size;
            return true;
        }

        @Override
        public FloatIterator iterator() {
            return new FloatIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Float2FloatArrayMap.this.size;
                }

                @Override
                public float nextFloat() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Float2FloatArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Float2FloatArrayMap.this.size - this.pos;
                    System.arraycopy(Float2FloatArrayMap.this.key, this.pos, Float2FloatArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Float2FloatArrayMap.this.value, this.pos, Float2FloatArrayMap.this.value, this.pos - 1, tail);
                    --Float2FloatArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(FloatConsumer action) {
                    float[] key = Float2FloatArrayMap.this.key;
                    int max = Float2FloatArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public FloatSpliterator spliterator() {
            return new KeySetSpliterator(0, Float2FloatArrayMap.this.size);
        }

        @Override
        public void forEach(FloatConsumer action) {
            float[] key = Float2FloatArrayMap.this.key;
            int max = Float2FloatArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Float2FloatArrayMap.this.size;
        }

        @Override
        public void clear() {
            Float2FloatArrayMap.this.clear();
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
                return Float2FloatArrayMap.this.key[location];
            }

            @Override
            protected final KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(FloatConsumer action) {
                float[] key = Float2FloatArrayMap.this.key;
                int max = Float2FloatArrayMap.this.size;
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
            return Float2FloatArrayMap.this.containsValue(v);
        }

        @Override
        public FloatIterator iterator() {
            return new FloatIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Float2FloatArrayMap.this.size;
                }

                @Override
                public float nextFloat() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Float2FloatArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Float2FloatArrayMap.this.size - this.pos;
                    System.arraycopy(Float2FloatArrayMap.this.key, this.pos, Float2FloatArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Float2FloatArrayMap.this.value, this.pos, Float2FloatArrayMap.this.value, this.pos - 1, tail);
                    --Float2FloatArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(FloatConsumer action) {
                    float[] value = Float2FloatArrayMap.this.value;
                    int max = Float2FloatArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public FloatSpliterator spliterator() {
            return new ValuesSpliterator(0, Float2FloatArrayMap.this.size);
        }

        @Override
        public void forEach(FloatConsumer action) {
            float[] value = Float2FloatArrayMap.this.value;
            int max = Float2FloatArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Float2FloatArrayMap.this.size;
        }

        @Override
        public void clear() {
            Float2FloatArrayMap.this.clear();
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
                return Float2FloatArrayMap.this.value[location];
            }

            @Override
            protected final ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(FloatConsumer action) {
                float[] value = Float2FloatArrayMap.this.value;
                int max = Float2FloatArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Float2FloatMap.Entry,
    Map.Entry<Float, Float>,
    FloatFloatPair {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public float getFloatKey() {
            return Float2FloatArrayMap.this.key[this.index];
        }

        @Override
        public float leftFloat() {
            return Float2FloatArrayMap.this.key[this.index];
        }

        @Override
        public float getFloatValue() {
            return Float2FloatArrayMap.this.value[this.index];
        }

        @Override
        public float rightFloat() {
            return Float2FloatArrayMap.this.value[this.index];
        }

        @Override
        public float setValue(float v) {
            float oldValue = Float2FloatArrayMap.this.value[this.index];
            Float2FloatArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        @Override
        public FloatFloatPair right(float v) {
            Float2FloatArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Float getKey() {
            return Float.valueOf(Float2FloatArrayMap.this.key[this.index]);
        }

        @Override
        @Deprecated
        public Float getValue() {
            return Float.valueOf(Float2FloatArrayMap.this.value[this.index]);
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
            return Float.floatToRawIntBits(Float2FloatArrayMap.this.key[this.index]) == Float.floatToRawIntBits(((Float)e.getKey()).floatValue()) && Float.floatToRawIntBits(Float2FloatArrayMap.this.value[this.index]) == Float.floatToRawIntBits(((Float)e.getValue()).floatValue());
        }

        @Override
        public int hashCode() {
            return HashCommon.float2int(Float2FloatArrayMap.this.key[this.index]) ^ HashCommon.float2int(Float2FloatArrayMap.this.value[this.index]);
        }

        public String toString() {
            return Float2FloatArrayMap.this.key[this.index] + "=>" + Float2FloatArrayMap.this.value[this.index];
        }
    }
}

