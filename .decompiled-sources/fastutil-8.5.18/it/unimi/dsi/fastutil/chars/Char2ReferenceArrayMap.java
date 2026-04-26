/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.chars;

import it.unimi.dsi.fastutil.chars.AbstractChar2ReferenceMap;
import it.unimi.dsi.fastutil.chars.AbstractCharSet;
import it.unimi.dsi.fastutil.chars.Char2ReferenceArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ReferenceMap;
import it.unimi.dsi.fastutil.chars.CharArrays;
import it.unimi.dsi.fastutil.chars.CharConsumer;
import it.unimi.dsi.fastutil.chars.CharIterator;
import it.unimi.dsi.fastutil.chars.CharReferencePair;
import it.unimi.dsi.fastutil.chars.CharSet;
import it.unimi.dsi.fastutil.chars.CharSpliterator;
import it.unimi.dsi.fastutil.chars.CharSpliterators;
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

public class Char2ReferenceArrayMap<V>
extends AbstractChar2ReferenceMap<V>
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient char[] key;
    protected transient Object[] value;
    protected int size;
    protected transient Char2ReferenceMap.FastEntrySet<V> entries;
    protected transient CharSet keys;
    protected transient ReferenceCollection<V> values;

    public Char2ReferenceArrayMap(char[] key, Object[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Char2ReferenceArrayMap() {
        this.key = CharArrays.EMPTY_ARRAY;
        this.value = ObjectArrays.EMPTY_ARRAY;
    }

    public Char2ReferenceArrayMap(int capacity) {
        this.key = new char[capacity];
        this.value = new Object[capacity];
    }

    public Char2ReferenceArrayMap(Char2ReferenceMap<V> m) {
        this(m.size());
        int i = 0;
        for (Char2ReferenceMap.Entry entry : m.char2ReferenceEntrySet()) {
            this.key[i] = entry.getCharKey();
            this.value[i] = entry.getValue();
            ++i;
        }
        this.size = i;
    }

    public Char2ReferenceArrayMap(Map<? extends Character, ? extends V> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<Character, V> e : m.entrySet()) {
            this.key[i] = e.getKey().charValue();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Char2ReferenceArrayMap(char[] key, Object[] value, int size) {
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

    public Char2ReferenceMap.FastEntrySet<V> char2ReferenceEntrySet() {
        if (this.entries == null) {
            this.entries = new EntrySet();
        }
        return this.entries;
    }

    private int findKey(char k) {
        char[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (key[i] != k) continue;
            return i;
        }
        return -1;
    }

    @Override
    public V get(char k) {
        char[] key = this.key;
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
    public boolean containsKey(char k) {
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
    public V put(char k, V v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            Object oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return (V)oldValue;
        }
        if (this.size == this.key.length) {
            char[] newKey = new char[this.size == 0 ? 2 : this.size * 2];
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
    public V remove(char k) {
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
    public CharSet keySet() {
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

    public Char2ReferenceArrayMap<V> clone() {
        Char2ReferenceArrayMap c;
        try {
            c = (Char2ReferenceArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (char[])this.key.clone();
        c.value = (Object[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        char[] key = this.key;
        Object[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeChar(key[i]);
            s.writeObject(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new char[this.size];
        char[] key = this.key;
        this.value = new Object[this.size];
        Object[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readChar();
            value[i] = s.readObject();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Char2ReferenceMap.Entry<V>>
    implements Char2ReferenceMap.FastEntrySet<V> {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Char2ReferenceMap.Entry<V>> iterator() {
            return new ObjectIterator<Char2ReferenceMap.Entry<V>>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Char2ReferenceArrayMap.this.size;
                }

                @Override
                public Char2ReferenceMap.Entry<V> next() {
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
                    int tail = Char2ReferenceArrayMap.this.size-- - this.next--;
                    System.arraycopy(Char2ReferenceArrayMap.this.key, this.next + 1, Char2ReferenceArrayMap.this.key, this.next, tail);
                    System.arraycopy(Char2ReferenceArrayMap.this.value, this.next + 1, Char2ReferenceArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Char2ReferenceArrayMap.this.value[Char2ReferenceArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Char2ReferenceArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Char2ReferenceMap.Entry<V>> action) {
                    int max = Char2ReferenceArrayMap.this.size;
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
        public ObjectIterator<Char2ReferenceMap.Entry<V>> fastIterator() {
            return new ObjectIterator<Char2ReferenceMap.Entry<V>>(){
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
                    return this.next < Char2ReferenceArrayMap.this.size;
                }

                @Override
                public Char2ReferenceMap.Entry<V> next() {
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
                    int tail = Char2ReferenceArrayMap.this.size-- - this.next--;
                    System.arraycopy(Char2ReferenceArrayMap.this.key, this.next + 1, Char2ReferenceArrayMap.this.key, this.next, tail);
                    System.arraycopy(Char2ReferenceArrayMap.this.value, this.next + 1, Char2ReferenceArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Char2ReferenceArrayMap.this.value[Char2ReferenceArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Char2ReferenceArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Char2ReferenceMap.Entry<V>> action) {
                    int max = Char2ReferenceArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Char2ReferenceMap.Entry<V>> spliterator() {
            return new EntrySetSpliterator(0, Char2ReferenceArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Char2ReferenceMap.Entry<V>> action) {
            int max = Char2ReferenceArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Char2ReferenceMap.Entry<V>> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Char2ReferenceArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Char2ReferenceArrayMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Character)) {
                return false;
            }
            char k = ((Character)e.getKey()).charValue();
            return Char2ReferenceArrayMap.this.containsKey(k) && Char2ReferenceArrayMap.this.get(k) == e.getValue();
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Character)) {
                return false;
            }
            char k = ((Character)e.getKey()).charValue();
            Object v = e.getValue();
            int oldPos = Char2ReferenceArrayMap.this.findKey(k);
            if (oldPos == -1 || v != Char2ReferenceArrayMap.this.value[oldPos]) {
                return false;
            }
            int tail = Char2ReferenceArrayMap.this.size - oldPos - 1;
            System.arraycopy(Char2ReferenceArrayMap.this.key, oldPos + 1, Char2ReferenceArrayMap.this.key, oldPos, tail);
            System.arraycopy(Char2ReferenceArrayMap.this.value, oldPos + 1, Char2ReferenceArrayMap.this.value, oldPos, tail);
            --Char2ReferenceArrayMap.this.size;
            Char2ReferenceArrayMap.this.value[Char2ReferenceArrayMap.this.size] = null;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Char2ReferenceMap.Entry<V>>
        implements ObjectSpliterator<Char2ReferenceMap.Entry<V>> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Char2ReferenceMap.Entry<V> get(int location) {
                return new MapEntry(location);
            }

            protected final it.unimi.dsi.fastutil.chars.Char2ReferenceArrayMap$EntrySet.EntrySetSpliterator makeForSplit(int pos, int maxPos) {
                return new EntrySetSpliterator(pos, maxPos);
            }
        }
    }

    private final class KeySet
    extends AbstractCharSet {
        private KeySet() {
        }

        @Override
        public boolean contains(char k) {
            return Char2ReferenceArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(char k) {
            int oldPos = Char2ReferenceArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Char2ReferenceArrayMap.this.size - oldPos - 1;
            System.arraycopy(Char2ReferenceArrayMap.this.key, oldPos + 1, Char2ReferenceArrayMap.this.key, oldPos, tail);
            System.arraycopy(Char2ReferenceArrayMap.this.value, oldPos + 1, Char2ReferenceArrayMap.this.value, oldPos, tail);
            --Char2ReferenceArrayMap.this.size;
            Char2ReferenceArrayMap.this.value[Char2ReferenceArrayMap.this.size] = null;
            return true;
        }

        @Override
        public CharIterator iterator() {
            return new CharIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Char2ReferenceArrayMap.this.size;
                }

                @Override
                public char nextChar() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Char2ReferenceArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Char2ReferenceArrayMap.this.size - this.pos;
                    System.arraycopy(Char2ReferenceArrayMap.this.key, this.pos, Char2ReferenceArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Char2ReferenceArrayMap.this.value, this.pos, Char2ReferenceArrayMap.this.value, this.pos - 1, tail);
                    --Char2ReferenceArrayMap.this.size;
                    --this.pos;
                    Char2ReferenceArrayMap.this.value[Char2ReferenceArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(CharConsumer action) {
                    char[] key = Char2ReferenceArrayMap.this.key;
                    int max = Char2ReferenceArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public CharSpliterator spliterator() {
            return new KeySetSpliterator(0, Char2ReferenceArrayMap.this.size);
        }

        @Override
        public void forEach(CharConsumer action) {
            char[] key = Char2ReferenceArrayMap.this.key;
            int max = Char2ReferenceArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Char2ReferenceArrayMap.this.size;
        }

        @Override
        public void clear() {
            Char2ReferenceArrayMap.this.clear();
        }

        final class KeySetSpliterator
        extends CharSpliterators.EarlyBindingSizeIndexBasedSpliterator
        implements CharSpliterator {
            KeySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16721;
            }

            @Override
            protected final char get(int location) {
                return Char2ReferenceArrayMap.this.key[location];
            }

            protected final it.unimi.dsi.fastutil.chars.Char2ReferenceArrayMap$KeySet.KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(CharConsumer action) {
                char[] key = Char2ReferenceArrayMap.this.key;
                int max = Char2ReferenceArrayMap.this.size;
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
            return Char2ReferenceArrayMap.this.containsValue(v);
        }

        @Override
        public ObjectIterator<V> iterator() {
            return new ObjectIterator<V>(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Char2ReferenceArrayMap.this.size;
                }

                @Override
                public V next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Char2ReferenceArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Char2ReferenceArrayMap.this.size - this.pos;
                    System.arraycopy(Char2ReferenceArrayMap.this.key, this.pos, Char2ReferenceArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Char2ReferenceArrayMap.this.value, this.pos, Char2ReferenceArrayMap.this.value, this.pos - 1, tail);
                    --Char2ReferenceArrayMap.this.size;
                    --this.pos;
                    Char2ReferenceArrayMap.this.value[Char2ReferenceArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(Consumer<? super V> action) {
                    Object[] value = Char2ReferenceArrayMap.this.value;
                    int max = Char2ReferenceArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<V> spliterator() {
            return new ValuesSpliterator(0, Char2ReferenceArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super V> action) {
            Object[] value = Char2ReferenceArrayMap.this.value;
            int max = Char2ReferenceArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Char2ReferenceArrayMap.this.size;
        }

        @Override
        public void clear() {
            Char2ReferenceArrayMap.this.clear();
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
                return Char2ReferenceArrayMap.this.value[location];
            }

            protected final it.unimi.dsi.fastutil.chars.Char2ReferenceArrayMap$ValuesCollection.ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(Consumer<? super V> action) {
                Object[] value = Char2ReferenceArrayMap.this.value;
                int max = Char2ReferenceArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Char2ReferenceMap.Entry<V>,
    Map.Entry<Character, V>,
    CharReferencePair<V> {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public char getCharKey() {
            return Char2ReferenceArrayMap.this.key[this.index];
        }

        @Override
        public char leftChar() {
            return Char2ReferenceArrayMap.this.key[this.index];
        }

        @Override
        public V getValue() {
            return Char2ReferenceArrayMap.this.value[this.index];
        }

        @Override
        public V right() {
            return Char2ReferenceArrayMap.this.value[this.index];
        }

        @Override
        public V setValue(V v) {
            Object oldValue = Char2ReferenceArrayMap.this.value[this.index];
            Char2ReferenceArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        public CharReferencePair<V> right(V v) {
            Char2ReferenceArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Character getKey() {
            return Character.valueOf(Char2ReferenceArrayMap.this.key[this.index]);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            return Char2ReferenceArrayMap.this.key[this.index] == ((Character)e.getKey()).charValue() && Char2ReferenceArrayMap.this.value[this.index] == e.getValue();
        }

        @Override
        public int hashCode() {
            return Char2ReferenceArrayMap.this.key[this.index] ^ (Char2ReferenceArrayMap.this.value[this.index] == null ? 0 : System.identityHashCode(Char2ReferenceArrayMap.this.value[this.index]));
        }

        public String toString() {
            return Char2ReferenceArrayMap.this.key[this.index] + "=>" + Char2ReferenceArrayMap.this.value[this.index];
        }
    }
}

