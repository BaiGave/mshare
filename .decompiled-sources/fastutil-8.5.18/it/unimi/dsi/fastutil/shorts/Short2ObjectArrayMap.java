/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.shorts;

import it.unimi.dsi.fastutil.objects.AbstractObjectCollection;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterators;
import it.unimi.dsi.fastutil.shorts.AbstractShort2ObjectMap;
import it.unimi.dsi.fastutil.shorts.AbstractShortSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.ShortArrays;
import it.unimi.dsi.fastutil.shorts.ShortConsumer;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import it.unimi.dsi.fastutil.shorts.ShortObjectPair;
import it.unimi.dsi.fastutil.shorts.ShortSet;
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

public class Short2ObjectArrayMap<V>
extends AbstractShort2ObjectMap<V>
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient short[] key;
    protected transient Object[] value;
    protected int size;
    protected transient Short2ObjectMap.FastEntrySet<V> entries;
    protected transient ShortSet keys;
    protected transient ObjectCollection<V> values;

    public Short2ObjectArrayMap(short[] key, Object[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Short2ObjectArrayMap() {
        this.key = ShortArrays.EMPTY_ARRAY;
        this.value = ObjectArrays.EMPTY_ARRAY;
    }

    public Short2ObjectArrayMap(int capacity) {
        this.key = new short[capacity];
        this.value = new Object[capacity];
    }

    public Short2ObjectArrayMap(Short2ObjectMap<V> m) {
        this(m.size());
        int i = 0;
        for (Short2ObjectMap.Entry entry : m.short2ObjectEntrySet()) {
            this.key[i] = entry.getShortKey();
            this.value[i] = entry.getValue();
            ++i;
        }
        this.size = i;
    }

    public Short2ObjectArrayMap(Map<? extends Short, ? extends V> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<Short, V> e : m.entrySet()) {
            this.key[i] = e.getKey();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Short2ObjectArrayMap(short[] key, Object[] value, int size) {
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

    public Short2ObjectMap.FastEntrySet<V> short2ObjectEntrySet() {
        if (this.entries == null) {
            this.entries = new EntrySet();
        }
        return this.entries;
    }

    private int findKey(short k) {
        short[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (key[i] != k) continue;
            return i;
        }
        return -1;
    }

    @Override
    public V get(short k) {
        short[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (key[i] != k) continue;
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
    public boolean containsKey(short k) {
        return this.findKey(k) != -1;
    }

    @Override
    public boolean containsValue(Object v) {
        Object[] value = this.value;
        int i = this.size;
        while (i-- != 0) {
            if (!Objects.equals(value[i], v)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public V put(short k, V v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            Object oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return (V)oldValue;
        }
        if (this.size == this.key.length) {
            short[] newKey = new short[this.size == 0 ? 2 : this.size * 2];
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
    public V remove(short k) {
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
    public ShortSet keySet() {
        if (this.keys == null) {
            this.keys = new KeySet();
        }
        return this.keys;
    }

    @Override
    public ObjectCollection<V> values() {
        if (this.values == null) {
            this.values = new ValuesCollection();
        }
        return this.values;
    }

    public Short2ObjectArrayMap<V> clone() {
        Short2ObjectArrayMap c;
        try {
            c = (Short2ObjectArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (short[])this.key.clone();
        c.value = (Object[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        short[] key = this.key;
        Object[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeShort(key[i]);
            s.writeObject(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new short[this.size];
        short[] key = this.key;
        this.value = new Object[this.size];
        Object[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readShort();
            value[i] = s.readObject();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Short2ObjectMap.Entry<V>>
    implements Short2ObjectMap.FastEntrySet<V> {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Short2ObjectMap.Entry<V>> iterator() {
            return new ObjectIterator<Short2ObjectMap.Entry<V>>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Short2ObjectArrayMap.this.size;
                }

                @Override
                public Short2ObjectMap.Entry<V> next() {
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
                    int tail = Short2ObjectArrayMap.this.size-- - this.next--;
                    System.arraycopy(Short2ObjectArrayMap.this.key, this.next + 1, Short2ObjectArrayMap.this.key, this.next, tail);
                    System.arraycopy(Short2ObjectArrayMap.this.value, this.next + 1, Short2ObjectArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Short2ObjectArrayMap.this.value[Short2ObjectArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Short2ObjectArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Short2ObjectMap.Entry<V>> action) {
                    int max = Short2ObjectArrayMap.this.size;
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
        public ObjectIterator<Short2ObjectMap.Entry<V>> fastIterator() {
            return new ObjectIterator<Short2ObjectMap.Entry<V>>(){
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
                    return this.next < Short2ObjectArrayMap.this.size;
                }

                @Override
                public Short2ObjectMap.Entry<V> next() {
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
                    int tail = Short2ObjectArrayMap.this.size-- - this.next--;
                    System.arraycopy(Short2ObjectArrayMap.this.key, this.next + 1, Short2ObjectArrayMap.this.key, this.next, tail);
                    System.arraycopy(Short2ObjectArrayMap.this.value, this.next + 1, Short2ObjectArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Short2ObjectArrayMap.this.value[Short2ObjectArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Short2ObjectArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Short2ObjectMap.Entry<V>> action) {
                    int max = Short2ObjectArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Short2ObjectMap.Entry<V>> spliterator() {
            return new EntrySetSpliterator(0, Short2ObjectArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Short2ObjectMap.Entry<V>> action) {
            int max = Short2ObjectArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Short2ObjectMap.Entry<V>> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Short2ObjectArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Short2ObjectArrayMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Short)) {
                return false;
            }
            short k = (Short)e.getKey();
            return Short2ObjectArrayMap.this.containsKey(k) && Objects.equals(Short2ObjectArrayMap.this.get(k), e.getValue());
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Short)) {
                return false;
            }
            short k = (Short)e.getKey();
            Object v = e.getValue();
            int oldPos = Short2ObjectArrayMap.this.findKey(k);
            if (oldPos == -1 || !Objects.equals(v, Short2ObjectArrayMap.this.value[oldPos])) {
                return false;
            }
            int tail = Short2ObjectArrayMap.this.size - oldPos - 1;
            System.arraycopy(Short2ObjectArrayMap.this.key, oldPos + 1, Short2ObjectArrayMap.this.key, oldPos, tail);
            System.arraycopy(Short2ObjectArrayMap.this.value, oldPos + 1, Short2ObjectArrayMap.this.value, oldPos, tail);
            --Short2ObjectArrayMap.this.size;
            Short2ObjectArrayMap.this.value[Short2ObjectArrayMap.this.size] = null;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Short2ObjectMap.Entry<V>>
        implements ObjectSpliterator<Short2ObjectMap.Entry<V>> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Short2ObjectMap.Entry<V> get(int location) {
                return new MapEntry(location);
            }

            protected final it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap$EntrySet.EntrySetSpliterator makeForSplit(int pos, int maxPos) {
                return new EntrySetSpliterator(pos, maxPos);
            }
        }
    }

    private final class KeySet
    extends AbstractShortSet {
        private KeySet() {
        }

        @Override
        public boolean contains(short k) {
            return Short2ObjectArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(short k) {
            int oldPos = Short2ObjectArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Short2ObjectArrayMap.this.size - oldPos - 1;
            System.arraycopy(Short2ObjectArrayMap.this.key, oldPos + 1, Short2ObjectArrayMap.this.key, oldPos, tail);
            System.arraycopy(Short2ObjectArrayMap.this.value, oldPos + 1, Short2ObjectArrayMap.this.value, oldPos, tail);
            --Short2ObjectArrayMap.this.size;
            Short2ObjectArrayMap.this.value[Short2ObjectArrayMap.this.size] = null;
            return true;
        }

        @Override
        public ShortIterator iterator() {
            return new ShortIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Short2ObjectArrayMap.this.size;
                }

                @Override
                public short nextShort() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Short2ObjectArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Short2ObjectArrayMap.this.size - this.pos;
                    System.arraycopy(Short2ObjectArrayMap.this.key, this.pos, Short2ObjectArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Short2ObjectArrayMap.this.value, this.pos, Short2ObjectArrayMap.this.value, this.pos - 1, tail);
                    --Short2ObjectArrayMap.this.size;
                    --this.pos;
                    Short2ObjectArrayMap.this.value[Short2ObjectArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(ShortConsumer action) {
                    short[] key = Short2ObjectArrayMap.this.key;
                    int max = Short2ObjectArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ShortSpliterator spliterator() {
            return new KeySetSpliterator(0, Short2ObjectArrayMap.this.size);
        }

        @Override
        public void forEach(ShortConsumer action) {
            short[] key = Short2ObjectArrayMap.this.key;
            int max = Short2ObjectArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Short2ObjectArrayMap.this.size;
        }

        @Override
        public void clear() {
            Short2ObjectArrayMap.this.clear();
        }

        final class KeySetSpliterator
        extends ShortSpliterators.EarlyBindingSizeIndexBasedSpliterator
        implements ShortSpliterator {
            KeySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16721;
            }

            @Override
            protected final short get(int location) {
                return Short2ObjectArrayMap.this.key[location];
            }

            protected final it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap$KeySet.KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(ShortConsumer action) {
                short[] key = Short2ObjectArrayMap.this.key;
                int max = Short2ObjectArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(key[this.pos++]);
                }
            }
        }
    }

    private final class ValuesCollection
    extends AbstractObjectCollection<V> {
        private ValuesCollection() {
        }

        @Override
        public boolean contains(Object v) {
            return Short2ObjectArrayMap.this.containsValue(v);
        }

        @Override
        public ObjectIterator<V> iterator() {
            return new ObjectIterator<V>(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Short2ObjectArrayMap.this.size;
                }

                @Override
                public V next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Short2ObjectArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Short2ObjectArrayMap.this.size - this.pos;
                    System.arraycopy(Short2ObjectArrayMap.this.key, this.pos, Short2ObjectArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Short2ObjectArrayMap.this.value, this.pos, Short2ObjectArrayMap.this.value, this.pos - 1, tail);
                    --Short2ObjectArrayMap.this.size;
                    --this.pos;
                    Short2ObjectArrayMap.this.value[Short2ObjectArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(Consumer<? super V> action) {
                    Object[] value = Short2ObjectArrayMap.this.value;
                    int max = Short2ObjectArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<V> spliterator() {
            return new ValuesSpliterator(0, Short2ObjectArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super V> action) {
            Object[] value = Short2ObjectArrayMap.this.value;
            int max = Short2ObjectArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Short2ObjectArrayMap.this.size;
        }

        @Override
        public void clear() {
            Short2ObjectArrayMap.this.clear();
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
                return Short2ObjectArrayMap.this.value[location];
            }

            protected final it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap$ValuesCollection.ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(Consumer<? super V> action) {
                Object[] value = Short2ObjectArrayMap.this.value;
                int max = Short2ObjectArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Short2ObjectMap.Entry<V>,
    Map.Entry<Short, V>,
    ShortObjectPair<V> {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public short getShortKey() {
            return Short2ObjectArrayMap.this.key[this.index];
        }

        @Override
        public short leftShort() {
            return Short2ObjectArrayMap.this.key[this.index];
        }

        @Override
        public V getValue() {
            return Short2ObjectArrayMap.this.value[this.index];
        }

        @Override
        public V right() {
            return Short2ObjectArrayMap.this.value[this.index];
        }

        @Override
        public V setValue(V v) {
            Object oldValue = Short2ObjectArrayMap.this.value[this.index];
            Short2ObjectArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        public ShortObjectPair<V> right(V v) {
            Short2ObjectArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Short getKey() {
            return Short2ObjectArrayMap.this.key[this.index];
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            return Short2ObjectArrayMap.this.key[this.index] == (Short)e.getKey() && Objects.equals(Short2ObjectArrayMap.this.value[this.index], e.getValue());
        }

        @Override
        public int hashCode() {
            return Short2ObjectArrayMap.this.key[this.index] ^ (Short2ObjectArrayMap.this.value[this.index] == null ? 0 : Short2ObjectArrayMap.this.value[this.index].hashCode());
        }

        public String toString() {
            return Short2ObjectArrayMap.this.key[this.index] + "=>" + Short2ObjectArrayMap.this.value[this.index];
        }
    }
}

