/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.objects;

import it.unimi.dsi.fastutil.bytes.AbstractByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteConsumer;
import it.unimi.dsi.fastutil.bytes.ByteIterator;
import it.unimi.dsi.fastutil.bytes.ByteSpliterator;
import it.unimi.dsi.fastutil.bytes.ByteSpliterators;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.AbstractReference2ByteMap;
import it.unimi.dsi.fastutil.objects.AbstractReferenceSet;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterators;
import it.unimi.dsi.fastutil.objects.Reference2ByteArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ByteMap;
import it.unimi.dsi.fastutil.objects.ReferenceBytePair;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class Reference2ByteArrayMap<K>
extends AbstractReference2ByteMap<K>
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient Object[] key;
    protected transient byte[] value;
    protected int size;
    protected transient Reference2ByteMap.FastEntrySet<K> entries;
    protected transient ReferenceSet<K> keys;
    protected transient ByteCollection values;

    public Reference2ByteArrayMap(Object[] key, byte[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Reference2ByteArrayMap() {
        this.key = ObjectArrays.EMPTY_ARRAY;
        this.value = ByteArrays.EMPTY_ARRAY;
    }

    public Reference2ByteArrayMap(int capacity) {
        this.key = new Object[capacity];
        this.value = new byte[capacity];
    }

    public Reference2ByteArrayMap(Reference2ByteMap<K> m) {
        this(m.size());
        int i = 0;
        for (Reference2ByteMap.Entry entry : m.reference2ByteEntrySet()) {
            this.key[i] = entry.getKey();
            this.value[i] = entry.getByteValue();
            ++i;
        }
        this.size = i;
    }

    public Reference2ByteArrayMap(Map<? extends K, ? extends Byte> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<K, Byte> e : m.entrySet()) {
            this.key[i] = e.getKey();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Reference2ByteArrayMap(Object[] key, byte[] value, int size) {
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

    public Reference2ByteMap.FastEntrySet<K> reference2ByteEntrySet() {
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
    public byte getByte(Object k) {
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
    public boolean containsValue(byte v) {
        byte[] value = this.value;
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
    public byte put(K k, byte v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            byte oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return oldValue;
        }
        if (this.size == this.key.length) {
            Object[] newKey = new Object[this.size == 0 ? 2 : this.size * 2];
            byte[] newValue = new byte[this.size == 0 ? 2 : this.size * 2];
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
    public byte removeByte(Object k) {
        int oldPos = this.findKey(k);
        if (oldPos == -1) {
            return this.defRetValue;
        }
        byte oldValue = this.value[oldPos];
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
    public ByteCollection values() {
        if (this.values == null) {
            this.values = new ValuesCollection();
        }
        return this.values;
    }

    public Reference2ByteArrayMap<K> clone() {
        Reference2ByteArrayMap c;
        try {
            c = (Reference2ByteArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (Object[])this.key.clone();
        c.value = (byte[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        Object[] key = this.key;
        byte[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeObject(key[i]);
            s.writeByte(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new Object[this.size];
        Object[] key = this.key;
        this.value = new byte[this.size];
        byte[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readObject();
            value[i] = s.readByte();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Reference2ByteMap.Entry<K>>
    implements Reference2ByteMap.FastEntrySet<K> {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Reference2ByteMap.Entry<K>> iterator() {
            return new ObjectIterator<Reference2ByteMap.Entry<K>>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Reference2ByteArrayMap.this.size;
                }

                @Override
                public Reference2ByteMap.Entry<K> next() {
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
                    int tail = Reference2ByteArrayMap.this.size-- - this.next--;
                    System.arraycopy(Reference2ByteArrayMap.this.key, this.next + 1, Reference2ByteArrayMap.this.key, this.next, tail);
                    System.arraycopy(Reference2ByteArrayMap.this.value, this.next + 1, Reference2ByteArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Reference2ByteArrayMap.this.key[Reference2ByteArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Reference2ByteArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Reference2ByteMap.Entry<K>> action) {
                    int max = Reference2ByteArrayMap.this.size;
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
        public ObjectIterator<Reference2ByteMap.Entry<K>> fastIterator() {
            return new ObjectIterator<Reference2ByteMap.Entry<K>>(){
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
                    return this.next < Reference2ByteArrayMap.this.size;
                }

                @Override
                public Reference2ByteMap.Entry<K> next() {
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
                    int tail = Reference2ByteArrayMap.this.size-- - this.next--;
                    System.arraycopy(Reference2ByteArrayMap.this.key, this.next + 1, Reference2ByteArrayMap.this.key, this.next, tail);
                    System.arraycopy(Reference2ByteArrayMap.this.value, this.next + 1, Reference2ByteArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Reference2ByteArrayMap.this.key[Reference2ByteArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Reference2ByteArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Reference2ByteMap.Entry<K>> action) {
                    int max = Reference2ByteArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Reference2ByteMap.Entry<K>> spliterator() {
            return new EntrySetSpliterator(0, Reference2ByteArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Reference2ByteMap.Entry<K>> action) {
            int max = Reference2ByteArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Reference2ByteMap.Entry<K>> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Reference2ByteArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Reference2ByteArrayMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getValue() == null || !(e.getValue() instanceof Byte)) {
                return false;
            }
            Object k = e.getKey();
            return Reference2ByteArrayMap.this.containsKey(k) && Reference2ByteArrayMap.this.getByte(k) == ((Byte)e.getValue()).byteValue();
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getValue() == null || !(e.getValue() instanceof Byte)) {
                return false;
            }
            Object k = e.getKey();
            byte v = (Byte)e.getValue();
            int oldPos = Reference2ByteArrayMap.this.findKey(k);
            if (oldPos == -1 || v != Reference2ByteArrayMap.this.value[oldPos]) {
                return false;
            }
            int tail = Reference2ByteArrayMap.this.size - oldPos - 1;
            System.arraycopy(Reference2ByteArrayMap.this.key, oldPos + 1, Reference2ByteArrayMap.this.key, oldPos, tail);
            System.arraycopy(Reference2ByteArrayMap.this.value, oldPos + 1, Reference2ByteArrayMap.this.value, oldPos, tail);
            --Reference2ByteArrayMap.this.size;
            Reference2ByteArrayMap.this.key[Reference2ByteArrayMap.this.size] = null;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Reference2ByteMap.Entry<K>>
        implements ObjectSpliterator<Reference2ByteMap.Entry<K>> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Reference2ByteMap.Entry<K> get(int location) {
                return new MapEntry(location);
            }

            protected final it.unimi.dsi.fastutil.objects.Reference2ByteArrayMap$EntrySet.EntrySetSpliterator makeForSplit(int pos, int maxPos) {
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
            return Reference2ByteArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(Object k) {
            int oldPos = Reference2ByteArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Reference2ByteArrayMap.this.size - oldPos - 1;
            System.arraycopy(Reference2ByteArrayMap.this.key, oldPos + 1, Reference2ByteArrayMap.this.key, oldPos, tail);
            System.arraycopy(Reference2ByteArrayMap.this.value, oldPos + 1, Reference2ByteArrayMap.this.value, oldPos, tail);
            --Reference2ByteArrayMap.this.size;
            Reference2ByteArrayMap.this.key[Reference2ByteArrayMap.this.size] = null;
            return true;
        }

        @Override
        public ObjectIterator<K> iterator() {
            return new ObjectIterator<K>(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Reference2ByteArrayMap.this.size;
                }

                @Override
                public K next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Reference2ByteArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Reference2ByteArrayMap.this.size - this.pos;
                    System.arraycopy(Reference2ByteArrayMap.this.key, this.pos, Reference2ByteArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Reference2ByteArrayMap.this.value, this.pos, Reference2ByteArrayMap.this.value, this.pos - 1, tail);
                    --Reference2ByteArrayMap.this.size;
                    --this.pos;
                    Reference2ByteArrayMap.this.key[Reference2ByteArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(Consumer<? super K> action) {
                    Object[] key = Reference2ByteArrayMap.this.key;
                    int max = Reference2ByteArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<K> spliterator() {
            return new KeySetSpliterator(0, Reference2ByteArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super K> action) {
            Object[] key = Reference2ByteArrayMap.this.key;
            int max = Reference2ByteArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Reference2ByteArrayMap.this.size;
        }

        @Override
        public void clear() {
            Reference2ByteArrayMap.this.clear();
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
                return Reference2ByteArrayMap.this.key[location];
            }

            protected final it.unimi.dsi.fastutil.objects.Reference2ByteArrayMap$KeySet.KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(Consumer<? super K> action) {
                Object[] key = Reference2ByteArrayMap.this.key;
                int max = Reference2ByteArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(key[this.pos++]);
                }
            }
        }
    }

    private final class ValuesCollection
    extends AbstractByteCollection {
        private ValuesCollection() {
        }

        @Override
        public boolean contains(byte v) {
            return Reference2ByteArrayMap.this.containsValue(v);
        }

        @Override
        public ByteIterator iterator() {
            return new ByteIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Reference2ByteArrayMap.this.size;
                }

                @Override
                public byte nextByte() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Reference2ByteArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Reference2ByteArrayMap.this.size - this.pos;
                    System.arraycopy(Reference2ByteArrayMap.this.key, this.pos, Reference2ByteArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Reference2ByteArrayMap.this.value, this.pos, Reference2ByteArrayMap.this.value, this.pos - 1, tail);
                    --Reference2ByteArrayMap.this.size;
                    --this.pos;
                    Reference2ByteArrayMap.this.key[Reference2ByteArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(ByteConsumer action) {
                    byte[] value = Reference2ByteArrayMap.this.value;
                    int max = Reference2ByteArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ByteSpliterator spliterator() {
            return new ValuesSpliterator(0, Reference2ByteArrayMap.this.size);
        }

        @Override
        public void forEach(ByteConsumer action) {
            byte[] value = Reference2ByteArrayMap.this.value;
            int max = Reference2ByteArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Reference2ByteArrayMap.this.size;
        }

        @Override
        public void clear() {
            Reference2ByteArrayMap.this.clear();
        }

        final class ValuesSpliterator
        extends ByteSpliterators.EarlyBindingSizeIndexBasedSpliterator
        implements ByteSpliterator {
            ValuesSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16720;
            }

            @Override
            protected final byte get(int location) {
                return Reference2ByteArrayMap.this.value[location];
            }

            protected final it.unimi.dsi.fastutil.objects.Reference2ByteArrayMap$ValuesCollection.ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(ByteConsumer action) {
                byte[] value = Reference2ByteArrayMap.this.value;
                int max = Reference2ByteArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Reference2ByteMap.Entry<K>,
    Map.Entry<K, Byte>,
    ReferenceBytePair<K> {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public K getKey() {
            return Reference2ByteArrayMap.this.key[this.index];
        }

        @Override
        public K left() {
            return Reference2ByteArrayMap.this.key[this.index];
        }

        @Override
        public byte getByteValue() {
            return Reference2ByteArrayMap.this.value[this.index];
        }

        @Override
        public byte rightByte() {
            return Reference2ByteArrayMap.this.value[this.index];
        }

        @Override
        public byte setValue(byte v) {
            byte oldValue = Reference2ByteArrayMap.this.value[this.index];
            Reference2ByteArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        @Override
        public ReferenceBytePair<K> right(byte v) {
            Reference2ByteArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Byte getValue() {
            return Reference2ByteArrayMap.this.value[this.index];
        }

        @Override
        @Deprecated
        public Byte setValue(Byte v) {
            return this.setValue((byte)v);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            return Reference2ByteArrayMap.this.key[this.index] == e.getKey() && Reference2ByteArrayMap.this.value[this.index] == (Byte)e.getValue();
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(Reference2ByteArrayMap.this.key[this.index]) ^ Reference2ByteArrayMap.this.value[this.index];
        }

        public String toString() {
            return Reference2ByteArrayMap.this.key[this.index] + "=>" + Reference2ByteArrayMap.this.value[this.index];
        }
    }
}

