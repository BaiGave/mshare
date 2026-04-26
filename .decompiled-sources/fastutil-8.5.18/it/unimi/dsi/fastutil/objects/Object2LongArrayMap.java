/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.objects;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.AbstractLongCollection;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSpliterator;
import it.unimi.dsi.fastutil.longs.LongSpliterators;
import it.unimi.dsi.fastutil.objects.AbstractObject2LongMap;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectLongPair;
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
import java.util.function.LongConsumer;

public class Object2LongArrayMap<K>
extends AbstractObject2LongMap<K>
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient Object[] key;
    protected transient long[] value;
    protected int size;
    protected transient Object2LongMap.FastEntrySet<K> entries;
    protected transient ObjectSet<K> keys;
    protected transient LongCollection values;

    public Object2LongArrayMap(Object[] key, long[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Object2LongArrayMap() {
        this.key = ObjectArrays.EMPTY_ARRAY;
        this.value = LongArrays.EMPTY_ARRAY;
    }

    public Object2LongArrayMap(int capacity) {
        this.key = new Object[capacity];
        this.value = new long[capacity];
    }

    public Object2LongArrayMap(Object2LongMap<K> m) {
        this(m.size());
        int i = 0;
        for (Object2LongMap.Entry entry : m.object2LongEntrySet()) {
            this.key[i] = entry.getKey();
            this.value[i] = entry.getLongValue();
            ++i;
        }
        this.size = i;
    }

    public Object2LongArrayMap(Map<? extends K, ? extends Long> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<K, Long> e : m.entrySet()) {
            this.key[i] = e.getKey();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Object2LongArrayMap(Object[] key, long[] value, int size) {
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

    public Object2LongMap.FastEntrySet<K> object2LongEntrySet() {
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
    public long getLong(Object k) {
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
    public boolean containsValue(long v) {
        long[] value = this.value;
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
    public long put(K k, long v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            long oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return oldValue;
        }
        if (this.size == this.key.length) {
            Object[] newKey = new Object[this.size == 0 ? 2 : this.size * 2];
            long[] newValue = new long[this.size == 0 ? 2 : this.size * 2];
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
    public long removeLong(Object k) {
        int oldPos = this.findKey(k);
        if (oldPos == -1) {
            return this.defRetValue;
        }
        long oldValue = this.value[oldPos];
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
    public LongCollection values() {
        if (this.values == null) {
            this.values = new ValuesCollection();
        }
        return this.values;
    }

    public Object2LongArrayMap<K> clone() {
        Object2LongArrayMap c;
        try {
            c = (Object2LongArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (Object[])this.key.clone();
        c.value = (long[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        Object[] key = this.key;
        long[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeObject(key[i]);
            s.writeLong(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new Object[this.size];
        Object[] key = this.key;
        this.value = new long[this.size];
        long[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readObject();
            value[i] = s.readLong();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Object2LongMap.Entry<K>>
    implements Object2LongMap.FastEntrySet<K> {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Object2LongMap.Entry<K>> iterator() {
            return new ObjectIterator<Object2LongMap.Entry<K>>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Object2LongArrayMap.this.size;
                }

                @Override
                public Object2LongMap.Entry<K> next() {
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
                    int tail = Object2LongArrayMap.this.size-- - this.next--;
                    System.arraycopy(Object2LongArrayMap.this.key, this.next + 1, Object2LongArrayMap.this.key, this.next, tail);
                    System.arraycopy(Object2LongArrayMap.this.value, this.next + 1, Object2LongArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Object2LongArrayMap.this.key[Object2LongArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Object2LongArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Object2LongMap.Entry<K>> action) {
                    int max = Object2LongArrayMap.this.size;
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
        public ObjectIterator<Object2LongMap.Entry<K>> fastIterator() {
            return new ObjectIterator<Object2LongMap.Entry<K>>(){
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
                    return this.next < Object2LongArrayMap.this.size;
                }

                @Override
                public Object2LongMap.Entry<K> next() {
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
                    int tail = Object2LongArrayMap.this.size-- - this.next--;
                    System.arraycopy(Object2LongArrayMap.this.key, this.next + 1, Object2LongArrayMap.this.key, this.next, tail);
                    System.arraycopy(Object2LongArrayMap.this.value, this.next + 1, Object2LongArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Object2LongArrayMap.this.key[Object2LongArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Object2LongArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Object2LongMap.Entry<K>> action) {
                    int max = Object2LongArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Object2LongMap.Entry<K>> spliterator() {
            return new EntrySetSpliterator(0, Object2LongArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Object2LongMap.Entry<K>> action) {
            int max = Object2LongArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Object2LongMap.Entry<K>> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Object2LongArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Object2LongArrayMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getValue() == null || !(e.getValue() instanceof Long)) {
                return false;
            }
            Object k = e.getKey();
            return Object2LongArrayMap.this.containsKey(k) && Object2LongArrayMap.this.getLong(k) == ((Long)e.getValue()).longValue();
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getValue() == null || !(e.getValue() instanceof Long)) {
                return false;
            }
            Object k = e.getKey();
            long v = (Long)e.getValue();
            int oldPos = Object2LongArrayMap.this.findKey(k);
            if (oldPos == -1 || v != Object2LongArrayMap.this.value[oldPos]) {
                return false;
            }
            int tail = Object2LongArrayMap.this.size - oldPos - 1;
            System.arraycopy(Object2LongArrayMap.this.key, oldPos + 1, Object2LongArrayMap.this.key, oldPos, tail);
            System.arraycopy(Object2LongArrayMap.this.value, oldPos + 1, Object2LongArrayMap.this.value, oldPos, tail);
            --Object2LongArrayMap.this.size;
            Object2LongArrayMap.this.key[Object2LongArrayMap.this.size] = null;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Object2LongMap.Entry<K>>
        implements ObjectSpliterator<Object2LongMap.Entry<K>> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Object2LongMap.Entry<K> get(int location) {
                return new MapEntry(location);
            }

            protected final it.unimi.dsi.fastutil.objects.Object2LongArrayMap$EntrySet.EntrySetSpliterator makeForSplit(int pos, int maxPos) {
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
            return Object2LongArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(Object k) {
            int oldPos = Object2LongArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Object2LongArrayMap.this.size - oldPos - 1;
            System.arraycopy(Object2LongArrayMap.this.key, oldPos + 1, Object2LongArrayMap.this.key, oldPos, tail);
            System.arraycopy(Object2LongArrayMap.this.value, oldPos + 1, Object2LongArrayMap.this.value, oldPos, tail);
            --Object2LongArrayMap.this.size;
            Object2LongArrayMap.this.key[Object2LongArrayMap.this.size] = null;
            return true;
        }

        @Override
        public ObjectIterator<K> iterator() {
            return new ObjectIterator<K>(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Object2LongArrayMap.this.size;
                }

                @Override
                public K next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Object2LongArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Object2LongArrayMap.this.size - this.pos;
                    System.arraycopy(Object2LongArrayMap.this.key, this.pos, Object2LongArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Object2LongArrayMap.this.value, this.pos, Object2LongArrayMap.this.value, this.pos - 1, tail);
                    --Object2LongArrayMap.this.size;
                    --this.pos;
                    Object2LongArrayMap.this.key[Object2LongArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(Consumer<? super K> action) {
                    Object[] key = Object2LongArrayMap.this.key;
                    int max = Object2LongArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<K> spliterator() {
            return new KeySetSpliterator(0, Object2LongArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super K> action) {
            Object[] key = Object2LongArrayMap.this.key;
            int max = Object2LongArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Object2LongArrayMap.this.size;
        }

        @Override
        public void clear() {
            Object2LongArrayMap.this.clear();
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
                return Object2LongArrayMap.this.key[location];
            }

            protected final it.unimi.dsi.fastutil.objects.Object2LongArrayMap$KeySet.KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(Consumer<? super K> action) {
                Object[] key = Object2LongArrayMap.this.key;
                int max = Object2LongArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(key[this.pos++]);
                }
            }
        }
    }

    private final class ValuesCollection
    extends AbstractLongCollection {
        private ValuesCollection() {
        }

        @Override
        public boolean contains(long v) {
            return Object2LongArrayMap.this.containsValue(v);
        }

        @Override
        public LongIterator iterator() {
            return new LongIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Object2LongArrayMap.this.size;
                }

                @Override
                public long nextLong() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Object2LongArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Object2LongArrayMap.this.size - this.pos;
                    System.arraycopy(Object2LongArrayMap.this.key, this.pos, Object2LongArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Object2LongArrayMap.this.value, this.pos, Object2LongArrayMap.this.value, this.pos - 1, tail);
                    --Object2LongArrayMap.this.size;
                    --this.pos;
                    Object2LongArrayMap.this.key[Object2LongArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(LongConsumer action) {
                    long[] value = Object2LongArrayMap.this.value;
                    int max = Object2LongArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public LongSpliterator spliterator() {
            return new ValuesSpliterator(0, Object2LongArrayMap.this.size);
        }

        @Override
        public void forEach(LongConsumer action) {
            long[] value = Object2LongArrayMap.this.value;
            int max = Object2LongArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Object2LongArrayMap.this.size;
        }

        @Override
        public void clear() {
            Object2LongArrayMap.this.clear();
        }

        final class ValuesSpliterator
        extends LongSpliterators.EarlyBindingSizeIndexBasedSpliterator
        implements LongSpliterator {
            ValuesSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16720;
            }

            @Override
            protected final long get(int location) {
                return Object2LongArrayMap.this.value[location];
            }

            protected final it.unimi.dsi.fastutil.objects.Object2LongArrayMap$ValuesCollection.ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(LongConsumer action) {
                long[] value = Object2LongArrayMap.this.value;
                int max = Object2LongArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Object2LongMap.Entry<K>,
    Map.Entry<K, Long>,
    ObjectLongPair<K> {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public K getKey() {
            return Object2LongArrayMap.this.key[this.index];
        }

        @Override
        public K left() {
            return Object2LongArrayMap.this.key[this.index];
        }

        @Override
        public long getLongValue() {
            return Object2LongArrayMap.this.value[this.index];
        }

        @Override
        public long rightLong() {
            return Object2LongArrayMap.this.value[this.index];
        }

        @Override
        public long setValue(long v) {
            long oldValue = Object2LongArrayMap.this.value[this.index];
            Object2LongArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        @Override
        public ObjectLongPair<K> right(long v) {
            Object2LongArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Long getValue() {
            return Object2LongArrayMap.this.value[this.index];
        }

        @Override
        @Deprecated
        public Long setValue(Long v) {
            return this.setValue((long)v);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            return Objects.equals(Object2LongArrayMap.this.key[this.index], e.getKey()) && Object2LongArrayMap.this.value[this.index] == (Long)e.getValue();
        }

        @Override
        public int hashCode() {
            return (Object2LongArrayMap.this.key[this.index] == null ? 0 : Object2LongArrayMap.this.key[this.index].hashCode()) ^ HashCommon.long2int(Object2LongArrayMap.this.value[this.index]);
        }

        public String toString() {
            return Object2LongArrayMap.this.key[this.index] + "=>" + Object2LongArrayMap.this.value[this.index];
        }
    }
}

