/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.objects;

import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.AbstractReference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.AbstractReferenceCollection;
import it.unimi.dsi.fastutil.objects.AbstractReferenceSet;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterators;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.ReferenceCollection;
import it.unimi.dsi.fastutil.objects.ReferenceReferencePair;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class Reference2ReferenceArrayMap<K, V>
extends AbstractReference2ReferenceMap<K, V>
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient Object[] key;
    protected transient Object[] value;
    protected int size;
    protected transient Reference2ReferenceMap.FastEntrySet<K, V> entries;
    protected transient ReferenceSet<K> keys;
    protected transient ReferenceCollection<V> values;

    public Reference2ReferenceArrayMap(Object[] key, Object[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Reference2ReferenceArrayMap() {
        this.key = ObjectArrays.EMPTY_ARRAY;
        this.value = ObjectArrays.EMPTY_ARRAY;
    }

    public Reference2ReferenceArrayMap(int capacity) {
        this.key = new Object[capacity];
        this.value = new Object[capacity];
    }

    public Reference2ReferenceArrayMap(Reference2ReferenceMap<K, V> m) {
        this(m.size());
        int i = 0;
        for (Reference2ReferenceMap.Entry entry : m.reference2ReferenceEntrySet()) {
            this.key[i] = entry.getKey();
            this.value[i] = entry.getValue();
            ++i;
        }
        this.size = i;
    }

    public Reference2ReferenceArrayMap(Map<? extends K, ? extends V> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<K, V> e : m.entrySet()) {
            this.key[i] = e.getKey();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Reference2ReferenceArrayMap(Object[] key, Object[] value, int size) {
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

    public Reference2ReferenceMap.FastEntrySet<K, V> reference2ReferenceEntrySet() {
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
    public V get(Object k) {
        Object[] key = this.key;
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
        Object[] key = this.key;
        Object[] value = this.value;
        int i = this.size;
        while (i-- != 0) {
            key[i] = null;
            value[i] = null;
        }
        this.size = 0;
    }

    @Override
    public boolean containsKey(Object k) {
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
    public V put(K k, V v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            Object oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return (V)oldValue;
        }
        if (this.size == this.key.length) {
            Object[] newKey = new Object[this.size == 0 ? 2 : this.size * 2];
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
    public V remove(Object k) {
        int oldPos = this.findKey(k);
        if (oldPos == -1) {
            return (V)this.defRetValue;
        }
        Object oldValue = this.value[oldPos];
        int tail = this.size - oldPos - 1;
        System.arraycopy(this.key, oldPos + 1, this.key, oldPos, tail);
        System.arraycopy(this.value, oldPos + 1, this.value, oldPos, tail);
        --this.size;
        this.key[this.size] = null;
        this.value[this.size] = null;
        return (V)oldValue;
    }

    @Override
    public ReferenceSet<K> keySet() {
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

    public Reference2ReferenceArrayMap<K, V> clone() {
        Reference2ReferenceArrayMap c;
        try {
            c = (Reference2ReferenceArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (Object[])this.key.clone();
        c.value = (Object[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        Object[] key = this.key;
        Object[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeObject(key[i]);
            s.writeObject(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new Object[this.size];
        Object[] key = this.key;
        this.value = new Object[this.size];
        Object[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readObject();
            value[i] = s.readObject();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Reference2ReferenceMap.Entry<K, V>>
    implements Reference2ReferenceMap.FastEntrySet<K, V> {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Reference2ReferenceMap.Entry<K, V>> iterator() {
            return new ObjectIterator<Reference2ReferenceMap.Entry<K, V>>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Reference2ReferenceArrayMap.this.size;
                }

                @Override
                public Reference2ReferenceMap.Entry<K, V> next() {
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
                    int tail = Reference2ReferenceArrayMap.this.size-- - this.next--;
                    System.arraycopy(Reference2ReferenceArrayMap.this.key, this.next + 1, Reference2ReferenceArrayMap.this.key, this.next, tail);
                    System.arraycopy(Reference2ReferenceArrayMap.this.value, this.next + 1, Reference2ReferenceArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Reference2ReferenceArrayMap.this.key[Reference2ReferenceArrayMap.this.size] = null;
                    Reference2ReferenceArrayMap.this.value[Reference2ReferenceArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Reference2ReferenceArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Reference2ReferenceMap.Entry<K, V>> action) {
                    int max = Reference2ReferenceArrayMap.this.size;
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
        public ObjectIterator<Reference2ReferenceMap.Entry<K, V>> fastIterator() {
            return new ObjectIterator<Reference2ReferenceMap.Entry<K, V>>(){
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
                    return this.next < Reference2ReferenceArrayMap.this.size;
                }

                @Override
                public Reference2ReferenceMap.Entry<K, V> next() {
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
                    int tail = Reference2ReferenceArrayMap.this.size-- - this.next--;
                    System.arraycopy(Reference2ReferenceArrayMap.this.key, this.next + 1, Reference2ReferenceArrayMap.this.key, this.next, tail);
                    System.arraycopy(Reference2ReferenceArrayMap.this.value, this.next + 1, Reference2ReferenceArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Reference2ReferenceArrayMap.this.key[Reference2ReferenceArrayMap.this.size] = null;
                    Reference2ReferenceArrayMap.this.value[Reference2ReferenceArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Reference2ReferenceArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Reference2ReferenceMap.Entry<K, V>> action) {
                    int max = Reference2ReferenceArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Reference2ReferenceMap.Entry<K, V>> spliterator() {
            return new EntrySetSpliterator(0, Reference2ReferenceArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Reference2ReferenceMap.Entry<K, V>> action) {
            int max = Reference2ReferenceArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Reference2ReferenceMap.Entry<K, V>> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Reference2ReferenceArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Reference2ReferenceArrayMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            Object k = e.getKey();
            return Reference2ReferenceArrayMap.this.containsKey(k) && Reference2ReferenceArrayMap.this.get(k) == e.getValue();
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            Object k = e.getKey();
            Object v = e.getValue();
            int oldPos = Reference2ReferenceArrayMap.this.findKey(k);
            if (oldPos == -1 || v != Reference2ReferenceArrayMap.this.value[oldPos]) {
                return false;
            }
            int tail = Reference2ReferenceArrayMap.this.size - oldPos - 1;
            System.arraycopy(Reference2ReferenceArrayMap.this.key, oldPos + 1, Reference2ReferenceArrayMap.this.key, oldPos, tail);
            System.arraycopy(Reference2ReferenceArrayMap.this.value, oldPos + 1, Reference2ReferenceArrayMap.this.value, oldPos, tail);
            --Reference2ReferenceArrayMap.this.size;
            Reference2ReferenceArrayMap.this.key[Reference2ReferenceArrayMap.this.size] = null;
            Reference2ReferenceArrayMap.this.value[Reference2ReferenceArrayMap.this.size] = null;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Reference2ReferenceMap.Entry<K, V>>
        implements ObjectSpliterator<Reference2ReferenceMap.Entry<K, V>> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Reference2ReferenceMap.Entry<K, V> get(int location) {
                return new MapEntry(location);
            }

            protected final it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap$EntrySet.EntrySetSpliterator makeForSplit(int pos, int maxPos) {
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
            return Reference2ReferenceArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(Object k) {
            int oldPos = Reference2ReferenceArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Reference2ReferenceArrayMap.this.size - oldPos - 1;
            System.arraycopy(Reference2ReferenceArrayMap.this.key, oldPos + 1, Reference2ReferenceArrayMap.this.key, oldPos, tail);
            System.arraycopy(Reference2ReferenceArrayMap.this.value, oldPos + 1, Reference2ReferenceArrayMap.this.value, oldPos, tail);
            --Reference2ReferenceArrayMap.this.size;
            Reference2ReferenceArrayMap.this.key[Reference2ReferenceArrayMap.this.size] = null;
            Reference2ReferenceArrayMap.this.value[Reference2ReferenceArrayMap.this.size] = null;
            return true;
        }

        @Override
        public ObjectIterator<K> iterator() {
            return new ObjectIterator<K>(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Reference2ReferenceArrayMap.this.size;
                }

                @Override
                public K next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Reference2ReferenceArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Reference2ReferenceArrayMap.this.size - this.pos;
                    System.arraycopy(Reference2ReferenceArrayMap.this.key, this.pos, Reference2ReferenceArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Reference2ReferenceArrayMap.this.value, this.pos, Reference2ReferenceArrayMap.this.value, this.pos - 1, tail);
                    --Reference2ReferenceArrayMap.this.size;
                    --this.pos;
                    Reference2ReferenceArrayMap.this.key[Reference2ReferenceArrayMap.this.size] = null;
                    Reference2ReferenceArrayMap.this.value[Reference2ReferenceArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(Consumer<? super K> action) {
                    Object[] key = Reference2ReferenceArrayMap.this.key;
                    int max = Reference2ReferenceArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<K> spliterator() {
            return new KeySetSpliterator(0, Reference2ReferenceArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super K> action) {
            Object[] key = Reference2ReferenceArrayMap.this.key;
            int max = Reference2ReferenceArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Reference2ReferenceArrayMap.this.size;
        }

        @Override
        public void clear() {
            Reference2ReferenceArrayMap.this.clear();
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
                return Reference2ReferenceArrayMap.this.key[location];
            }

            protected final it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap$KeySet.KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(Consumer<? super K> action) {
                Object[] key = Reference2ReferenceArrayMap.this.key;
                int max = Reference2ReferenceArrayMap.this.size;
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
            return Reference2ReferenceArrayMap.this.containsValue(v);
        }

        @Override
        public ObjectIterator<V> iterator() {
            return new ObjectIterator<V>(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Reference2ReferenceArrayMap.this.size;
                }

                @Override
                public V next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Reference2ReferenceArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Reference2ReferenceArrayMap.this.size - this.pos;
                    System.arraycopy(Reference2ReferenceArrayMap.this.key, this.pos, Reference2ReferenceArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Reference2ReferenceArrayMap.this.value, this.pos, Reference2ReferenceArrayMap.this.value, this.pos - 1, tail);
                    --Reference2ReferenceArrayMap.this.size;
                    --this.pos;
                    Reference2ReferenceArrayMap.this.key[Reference2ReferenceArrayMap.this.size] = null;
                    Reference2ReferenceArrayMap.this.value[Reference2ReferenceArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(Consumer<? super V> action) {
                    Object[] value = Reference2ReferenceArrayMap.this.value;
                    int max = Reference2ReferenceArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<V> spliterator() {
            return new ValuesSpliterator(0, Reference2ReferenceArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super V> action) {
            Object[] value = Reference2ReferenceArrayMap.this.value;
            int max = Reference2ReferenceArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Reference2ReferenceArrayMap.this.size;
        }

        @Override
        public void clear() {
            Reference2ReferenceArrayMap.this.clear();
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
                return Reference2ReferenceArrayMap.this.value[location];
            }

            protected final it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap$ValuesCollection.ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(Consumer<? super V> action) {
                Object[] value = Reference2ReferenceArrayMap.this.value;
                int max = Reference2ReferenceArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Reference2ReferenceMap.Entry<K, V>,
    Map.Entry<K, V>,
    ReferenceReferencePair<K, V> {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public K getKey() {
            return Reference2ReferenceArrayMap.this.key[this.index];
        }

        @Override
        public K left() {
            return Reference2ReferenceArrayMap.this.key[this.index];
        }

        @Override
        public V getValue() {
            return Reference2ReferenceArrayMap.this.value[this.index];
        }

        @Override
        public V right() {
            return Reference2ReferenceArrayMap.this.value[this.index];
        }

        @Override
        public V setValue(V v) {
            Object oldValue = Reference2ReferenceArrayMap.this.value[this.index];
            Reference2ReferenceArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        @Override
        public ReferenceReferencePair<K, V> right(V v) {
            Reference2ReferenceArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            return Reference2ReferenceArrayMap.this.key[this.index] == e.getKey() && Reference2ReferenceArrayMap.this.value[this.index] == e.getValue();
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(Reference2ReferenceArrayMap.this.key[this.index]) ^ (Reference2ReferenceArrayMap.this.value[this.index] == null ? 0 : System.identityHashCode(Reference2ReferenceArrayMap.this.value[this.index]));
        }

        public String toString() {
            return Reference2ReferenceArrayMap.this.key[this.index] + "=>" + Reference2ReferenceArrayMap.this.value[this.index];
        }
    }
}

