/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.objects;

import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.AbstractReference2ShortMap;
import it.unimi.dsi.fastutil.objects.AbstractReferenceSet;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterators;
import it.unimi.dsi.fastutil.objects.Reference2ShortArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ShortMap;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceShortPair;
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

public class Reference2ShortArrayMap<K>
extends AbstractReference2ShortMap<K>
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient Object[] key;
    protected transient short[] value;
    protected int size;
    protected transient Reference2ShortMap.FastEntrySet<K> entries;
    protected transient ReferenceSet<K> keys;
    protected transient ShortCollection values;

    public Reference2ShortArrayMap(Object[] key, short[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Reference2ShortArrayMap() {
        this.key = ObjectArrays.EMPTY_ARRAY;
        this.value = ShortArrays.EMPTY_ARRAY;
    }

    public Reference2ShortArrayMap(int capacity) {
        this.key = new Object[capacity];
        this.value = new short[capacity];
    }

    public Reference2ShortArrayMap(Reference2ShortMap<K> m) {
        this(m.size());
        int i = 0;
        for (Reference2ShortMap.Entry entry : m.reference2ShortEntrySet()) {
            this.key[i] = entry.getKey();
            this.value[i] = entry.getShortValue();
            ++i;
        }
        this.size = i;
    }

    public Reference2ShortArrayMap(Map<? extends K, ? extends Short> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<K, Short> e : m.entrySet()) {
            this.key[i] = e.getKey();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Reference2ShortArrayMap(Object[] key, short[] value, int size) {
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

    public Reference2ShortMap.FastEntrySet<K> reference2ShortEntrySet() {
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
    public short getShort(Object k) {
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
    public ReferenceSet<K> keySet() {
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

    public Reference2ShortArrayMap<K> clone() {
        Reference2ShortArrayMap c;
        try {
            c = (Reference2ShortArrayMap)super.clone();
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
    extends AbstractObjectSet<Reference2ShortMap.Entry<K>>
    implements Reference2ShortMap.FastEntrySet<K> {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Reference2ShortMap.Entry<K>> iterator() {
            return new ObjectIterator<Reference2ShortMap.Entry<K>>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Reference2ShortArrayMap.this.size;
                }

                @Override
                public Reference2ShortMap.Entry<K> next() {
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
                    int tail = Reference2ShortArrayMap.this.size-- - this.next--;
                    System.arraycopy(Reference2ShortArrayMap.this.key, this.next + 1, Reference2ShortArrayMap.this.key, this.next, tail);
                    System.arraycopy(Reference2ShortArrayMap.this.value, this.next + 1, Reference2ShortArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Reference2ShortArrayMap.this.key[Reference2ShortArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Reference2ShortArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Reference2ShortMap.Entry<K>> action) {
                    int max = Reference2ShortArrayMap.this.size;
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
        public ObjectIterator<Reference2ShortMap.Entry<K>> fastIterator() {
            return new ObjectIterator<Reference2ShortMap.Entry<K>>(){
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
                    return this.next < Reference2ShortArrayMap.this.size;
                }

                @Override
                public Reference2ShortMap.Entry<K> next() {
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
                    int tail = Reference2ShortArrayMap.this.size-- - this.next--;
                    System.arraycopy(Reference2ShortArrayMap.this.key, this.next + 1, Reference2ShortArrayMap.this.key, this.next, tail);
                    System.arraycopy(Reference2ShortArrayMap.this.value, this.next + 1, Reference2ShortArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Reference2ShortArrayMap.this.key[Reference2ShortArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Reference2ShortArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Reference2ShortMap.Entry<K>> action) {
                    int max = Reference2ShortArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Reference2ShortMap.Entry<K>> spliterator() {
            return new EntrySetSpliterator(0, Reference2ShortArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Reference2ShortMap.Entry<K>> action) {
            int max = Reference2ShortArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Reference2ShortMap.Entry<K>> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Reference2ShortArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Reference2ShortArrayMap.this.size;
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
            return Reference2ShortArrayMap.this.containsKey(k) && Reference2ShortArrayMap.this.getShort(k) == ((Short)e.getValue()).shortValue();
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
            int oldPos = Reference2ShortArrayMap.this.findKey(k);
            if (oldPos == -1 || v != Reference2ShortArrayMap.this.value[oldPos]) {
                return false;
            }
            int tail = Reference2ShortArrayMap.this.size - oldPos - 1;
            System.arraycopy(Reference2ShortArrayMap.this.key, oldPos + 1, Reference2ShortArrayMap.this.key, oldPos, tail);
            System.arraycopy(Reference2ShortArrayMap.this.value, oldPos + 1, Reference2ShortArrayMap.this.value, oldPos, tail);
            --Reference2ShortArrayMap.this.size;
            Reference2ShortArrayMap.this.key[Reference2ShortArrayMap.this.size] = null;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Reference2ShortMap.Entry<K>>
        implements ObjectSpliterator<Reference2ShortMap.Entry<K>> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Reference2ShortMap.Entry<K> get(int location) {
                return new MapEntry(location);
            }

            protected final it.unimi.dsi.fastutil.objects.Reference2ShortArrayMap$EntrySet.EntrySetSpliterator makeForSplit(int pos, int maxPos) {
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
            return Reference2ShortArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(Object k) {
            int oldPos = Reference2ShortArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Reference2ShortArrayMap.this.size - oldPos - 1;
            System.arraycopy(Reference2ShortArrayMap.this.key, oldPos + 1, Reference2ShortArrayMap.this.key, oldPos, tail);
            System.arraycopy(Reference2ShortArrayMap.this.value, oldPos + 1, Reference2ShortArrayMap.this.value, oldPos, tail);
            --Reference2ShortArrayMap.this.size;
            Reference2ShortArrayMap.this.key[Reference2ShortArrayMap.this.size] = null;
            return true;
        }

        @Override
        public ObjectIterator<K> iterator() {
            return new ObjectIterator<K>(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Reference2ShortArrayMap.this.size;
                }

                @Override
                public K next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Reference2ShortArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Reference2ShortArrayMap.this.size - this.pos;
                    System.arraycopy(Reference2ShortArrayMap.this.key, this.pos, Reference2ShortArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Reference2ShortArrayMap.this.value, this.pos, Reference2ShortArrayMap.this.value, this.pos - 1, tail);
                    --Reference2ShortArrayMap.this.size;
                    --this.pos;
                    Reference2ShortArrayMap.this.key[Reference2ShortArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(Consumer<? super K> action) {
                    Object[] key = Reference2ShortArrayMap.this.key;
                    int max = Reference2ShortArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<K> spliterator() {
            return new KeySetSpliterator(0, Reference2ShortArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super K> action) {
            Object[] key = Reference2ShortArrayMap.this.key;
            int max = Reference2ShortArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Reference2ShortArrayMap.this.size;
        }

        @Override
        public void clear() {
            Reference2ShortArrayMap.this.clear();
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
                return Reference2ShortArrayMap.this.key[location];
            }

            protected final it.unimi.dsi.fastutil.objects.Reference2ShortArrayMap$KeySet.KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(Consumer<? super K> action) {
                Object[] key = Reference2ShortArrayMap.this.key;
                int max = Reference2ShortArrayMap.this.size;
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
            return Reference2ShortArrayMap.this.containsValue(v);
        }

        @Override
        public ShortIterator iterator() {
            return new ShortIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Reference2ShortArrayMap.this.size;
                }

                @Override
                public short nextShort() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Reference2ShortArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Reference2ShortArrayMap.this.size - this.pos;
                    System.arraycopy(Reference2ShortArrayMap.this.key, this.pos, Reference2ShortArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Reference2ShortArrayMap.this.value, this.pos, Reference2ShortArrayMap.this.value, this.pos - 1, tail);
                    --Reference2ShortArrayMap.this.size;
                    --this.pos;
                    Reference2ShortArrayMap.this.key[Reference2ShortArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(ShortConsumer action) {
                    short[] value = Reference2ShortArrayMap.this.value;
                    int max = Reference2ShortArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ShortSpliterator spliterator() {
            return new ValuesSpliterator(0, Reference2ShortArrayMap.this.size);
        }

        @Override
        public void forEach(ShortConsumer action) {
            short[] value = Reference2ShortArrayMap.this.value;
            int max = Reference2ShortArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Reference2ShortArrayMap.this.size;
        }

        @Override
        public void clear() {
            Reference2ShortArrayMap.this.clear();
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
                return Reference2ShortArrayMap.this.value[location];
            }

            protected final it.unimi.dsi.fastutil.objects.Reference2ShortArrayMap$ValuesCollection.ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(ShortConsumer action) {
                short[] value = Reference2ShortArrayMap.this.value;
                int max = Reference2ShortArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Reference2ShortMap.Entry<K>,
    Map.Entry<K, Short>,
    ReferenceShortPair<K> {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public K getKey() {
            return Reference2ShortArrayMap.this.key[this.index];
        }

        @Override
        public K left() {
            return Reference2ShortArrayMap.this.key[this.index];
        }

        @Override
        public short getShortValue() {
            return Reference2ShortArrayMap.this.value[this.index];
        }

        @Override
        public short rightShort() {
            return Reference2ShortArrayMap.this.value[this.index];
        }

        @Override
        public short setValue(short v) {
            short oldValue = Reference2ShortArrayMap.this.value[this.index];
            Reference2ShortArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        @Override
        public ReferenceShortPair<K> right(short v) {
            Reference2ShortArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Short getValue() {
            return Reference2ShortArrayMap.this.value[this.index];
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
            return Reference2ShortArrayMap.this.key[this.index] == e.getKey() && Reference2ShortArrayMap.this.value[this.index] == (Short)e.getValue();
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(Reference2ShortArrayMap.this.key[this.index]) ^ Reference2ShortArrayMap.this.value[this.index];
        }

        public String toString() {
            return Reference2ShortArrayMap.this.key[this.index] + "=>" + Reference2ShortArrayMap.this.value[this.index];
        }
    }
}

