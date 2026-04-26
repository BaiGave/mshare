/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.objects;

import it.unimi.dsi.fastutil.objects.AbstractObject2ShortMap;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.Object2ShortArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectShortPair;
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
import java.util.Objects;
import java.util.function.Consumer;

public class Object2ShortArrayMap<K>
extends AbstractObject2ShortMap<K>
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient Object[] key;
    protected transient short[] value;
    protected int size;
    protected transient Object2ShortMap.FastEntrySet<K> entries;
    protected transient ObjectSet<K> keys;
    protected transient ShortCollection values;

    public Object2ShortArrayMap(Object[] key, short[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Object2ShortArrayMap() {
        this.key = ObjectArrays.EMPTY_ARRAY;
        this.value = ShortArrays.EMPTY_ARRAY;
    }

    public Object2ShortArrayMap(int capacity) {
        this.key = new Object[capacity];
        this.value = new short[capacity];
    }

    public Object2ShortArrayMap(Object2ShortMap<K> m) {
        this(m.size());
        int i = 0;
        for (Object2ShortMap.Entry entry : m.object2ShortEntrySet()) {
            this.key[i] = entry.getKey();
            this.value[i] = entry.getShortValue();
            ++i;
        }
        this.size = i;
    }

    public Object2ShortArrayMap(Map<? extends K, ? extends Short> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<K, Short> e : m.entrySet()) {
            this.key[i] = e.getKey();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Object2ShortArrayMap(Object[] key, short[] value, int size) {
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

    public Object2ShortMap.FastEntrySet<K> object2ShortEntrySet() {
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
    public short getShort(Object k) {
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
    public short put(K k, short v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            short oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return oldValue;
        }
        if (this.size == this.key.length) {
            Object[] newKey = new Object[this.size == 0 ? 2 : this.size * 2];
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
    public short removeShort(Object k) {
        int oldPos = this.findKey(k);
        if (oldPos == -1) {
            return this.defRetValue;
        }
        short oldValue = this.value[oldPos];
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
    public ShortCollection values() {
        if (this.values == null) {
            this.values = new ValuesCollection();
        }
        return this.values;
    }

    public Object2ShortArrayMap<K> clone() {
        Object2ShortArrayMap c;
        try {
            c = (Object2ShortArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (Object[])this.key.clone();
        c.value = (short[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        Object[] key = this.key;
        short[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeObject(key[i]);
            s.writeShort(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new Object[this.size];
        Object[] key = this.key;
        this.value = new short[this.size];
        short[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readObject();
            value[i] = s.readShort();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Object2ShortMap.Entry<K>>
    implements Object2ShortMap.FastEntrySet<K> {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Object2ShortMap.Entry<K>> iterator() {
            return new ObjectIterator<Object2ShortMap.Entry<K>>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Object2ShortArrayMap.this.size;
                }

                @Override
                public Object2ShortMap.Entry<K> next() {
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
                    int tail = Object2ShortArrayMap.this.size-- - this.next--;
                    System.arraycopy(Object2ShortArrayMap.this.key, this.next + 1, Object2ShortArrayMap.this.key, this.next, tail);
                    System.arraycopy(Object2ShortArrayMap.this.value, this.next + 1, Object2ShortArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Object2ShortArrayMap.this.key[Object2ShortArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Object2ShortArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Object2ShortMap.Entry<K>> action) {
                    int max = Object2ShortArrayMap.this.size;
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
        public ObjectIterator<Object2ShortMap.Entry<K>> fastIterator() {
            return new ObjectIterator<Object2ShortMap.Entry<K>>(){
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
                    return this.next < Object2ShortArrayMap.this.size;
                }

                @Override
                public Object2ShortMap.Entry<K> next() {
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
                    int tail = Object2ShortArrayMap.this.size-- - this.next--;
                    System.arraycopy(Object2ShortArrayMap.this.key, this.next + 1, Object2ShortArrayMap.this.key, this.next, tail);
                    System.arraycopy(Object2ShortArrayMap.this.value, this.next + 1, Object2ShortArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Object2ShortArrayMap.this.key[Object2ShortArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Object2ShortArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Object2ShortMap.Entry<K>> action) {
                    int max = Object2ShortArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Object2ShortMap.Entry<K>> spliterator() {
            return new EntrySetSpliterator(0, Object2ShortArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Object2ShortMap.Entry<K>> action) {
            int max = Object2ShortArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Object2ShortMap.Entry<K>> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Object2ShortArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Object2ShortArrayMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getValue() == null || !(e.getValue() instanceof Short)) {
                return false;
            }
            Object k = e.getKey();
            return Object2ShortArrayMap.this.containsKey(k) && Object2ShortArrayMap.this.getShort(k) == ((Short)e.getValue()).shortValue();
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getValue() == null || !(e.getValue() instanceof Short)) {
                return false;
            }
            Object k = e.getKey();
            short v = (Short)e.getValue();
            int oldPos = Object2ShortArrayMap.this.findKey(k);
            if (oldPos == -1 || v != Object2ShortArrayMap.this.value[oldPos]) {
                return false;
            }
            int tail = Object2ShortArrayMap.this.size - oldPos - 1;
            System.arraycopy(Object2ShortArrayMap.this.key, oldPos + 1, Object2ShortArrayMap.this.key, oldPos, tail);
            System.arraycopy(Object2ShortArrayMap.this.value, oldPos + 1, Object2ShortArrayMap.this.value, oldPos, tail);
            --Object2ShortArrayMap.this.size;
            Object2ShortArrayMap.this.key[Object2ShortArrayMap.this.size] = null;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Object2ShortMap.Entry<K>>
        implements ObjectSpliterator<Object2ShortMap.Entry<K>> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Object2ShortMap.Entry<K> get(int location) {
                return new MapEntry(location);
            }

            protected final it.unimi.dsi.fastutil.objects.Object2ShortArrayMap$EntrySet.EntrySetSpliterator makeForSplit(int pos, int maxPos) {
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
            return Object2ShortArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(Object k) {
            int oldPos = Object2ShortArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Object2ShortArrayMap.this.size - oldPos - 1;
            System.arraycopy(Object2ShortArrayMap.this.key, oldPos + 1, Object2ShortArrayMap.this.key, oldPos, tail);
            System.arraycopy(Object2ShortArrayMap.this.value, oldPos + 1, Object2ShortArrayMap.this.value, oldPos, tail);
            --Object2ShortArrayMap.this.size;
            Object2ShortArrayMap.this.key[Object2ShortArrayMap.this.size] = null;
            return true;
        }

        @Override
        public ObjectIterator<K> iterator() {
            return new ObjectIterator<K>(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Object2ShortArrayMap.this.size;
                }

                @Override
                public K next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Object2ShortArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Object2ShortArrayMap.this.size - this.pos;
                    System.arraycopy(Object2ShortArrayMap.this.key, this.pos, Object2ShortArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Object2ShortArrayMap.this.value, this.pos, Object2ShortArrayMap.this.value, this.pos - 1, tail);
                    --Object2ShortArrayMap.this.size;
                    --this.pos;
                    Object2ShortArrayMap.this.key[Object2ShortArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(Consumer<? super K> action) {
                    Object[] key = Object2ShortArrayMap.this.key;
                    int max = Object2ShortArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<K> spliterator() {
            return new KeySetSpliterator(0, Object2ShortArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super K> action) {
            Object[] key = Object2ShortArrayMap.this.key;
            int max = Object2ShortArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Object2ShortArrayMap.this.size;
        }

        @Override
        public void clear() {
            Object2ShortArrayMap.this.clear();
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
                return Object2ShortArrayMap.this.key[location];
            }

            protected final it.unimi.dsi.fastutil.objects.Object2ShortArrayMap$KeySet.KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(Consumer<? super K> action) {
                Object[] key = Object2ShortArrayMap.this.key;
                int max = Object2ShortArrayMap.this.size;
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
            return Object2ShortArrayMap.this.containsValue(v);
        }

        @Override
        public ShortIterator iterator() {
            return new ShortIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Object2ShortArrayMap.this.size;
                }

                @Override
                public short nextShort() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Object2ShortArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Object2ShortArrayMap.this.size - this.pos;
                    System.arraycopy(Object2ShortArrayMap.this.key, this.pos, Object2ShortArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Object2ShortArrayMap.this.value, this.pos, Object2ShortArrayMap.this.value, this.pos - 1, tail);
                    --Object2ShortArrayMap.this.size;
                    --this.pos;
                    Object2ShortArrayMap.this.key[Object2ShortArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(ShortConsumer action) {
                    short[] value = Object2ShortArrayMap.this.value;
                    int max = Object2ShortArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ShortSpliterator spliterator() {
            return new ValuesSpliterator(0, Object2ShortArrayMap.this.size);
        }

        @Override
        public void forEach(ShortConsumer action) {
            short[] value = Object2ShortArrayMap.this.value;
            int max = Object2ShortArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Object2ShortArrayMap.this.size;
        }

        @Override
        public void clear() {
            Object2ShortArrayMap.this.clear();
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
                return Object2ShortArrayMap.this.value[location];
            }

            protected final it.unimi.dsi.fastutil.objects.Object2ShortArrayMap$ValuesCollection.ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(ShortConsumer action) {
                short[] value = Object2ShortArrayMap.this.value;
                int max = Object2ShortArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Object2ShortMap.Entry<K>,
    Map.Entry<K, Short>,
    ObjectShortPair<K> {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public K getKey() {
            return Object2ShortArrayMap.this.key[this.index];
        }

        @Override
        public K left() {
            return Object2ShortArrayMap.this.key[this.index];
        }

        @Override
        public short getShortValue() {
            return Object2ShortArrayMap.this.value[this.index];
        }

        @Override
        public short rightShort() {
            return Object2ShortArrayMap.this.value[this.index];
        }

        @Override
        public short setValue(short v) {
            short oldValue = Object2ShortArrayMap.this.value[this.index];
            Object2ShortArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        @Override
        public ObjectShortPair<K> right(short v) {
            Object2ShortArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Short getValue() {
            return Object2ShortArrayMap.this.value[this.index];
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
            return Objects.equals(Object2ShortArrayMap.this.key[this.index], e.getKey()) && Object2ShortArrayMap.this.value[this.index] == (Short)e.getValue();
        }

        @Override
        public int hashCode() {
            return (Object2ShortArrayMap.this.key[this.index] == null ? 0 : Object2ShortArrayMap.this.key[this.index].hashCode()) ^ Object2ShortArrayMap.this.value[this.index];
        }

        public String toString() {
            return Object2ShortArrayMap.this.key[this.index] + "=>" + Object2ShortArrayMap.this.value[this.index];
        }
    }
}

