/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.chars;

import it.unimi.dsi.fastutil.chars.AbstractChar2CharMap;
import it.unimi.dsi.fastutil.chars.AbstractCharCollection;
import it.unimi.dsi.fastutil.chars.AbstractCharSet;
import it.unimi.dsi.fastutil.chars.Char2CharMap;
import it.unimi.dsi.fastutil.chars.CharArrays;
import it.unimi.dsi.fastutil.chars.CharCharPair;
import it.unimi.dsi.fastutil.chars.CharCollection;
import it.unimi.dsi.fastutil.chars.CharConsumer;
import it.unimi.dsi.fastutil.chars.CharIterator;
import it.unimi.dsi.fastutil.chars.CharSet;
import it.unimi.dsi.fastutil.chars.CharSpliterator;
import it.unimi.dsi.fastutil.chars.CharSpliterators;
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

public class Char2CharArrayMap
extends AbstractChar2CharMap
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient char[] key;
    protected transient char[] value;
    protected int size;
    protected transient Char2CharMap.FastEntrySet entries;
    protected transient CharSet keys;
    protected transient CharCollection values;

    public Char2CharArrayMap(char[] key, char[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Char2CharArrayMap() {
        this.key = CharArrays.EMPTY_ARRAY;
        this.value = CharArrays.EMPTY_ARRAY;
    }

    public Char2CharArrayMap(int capacity) {
        this.key = new char[capacity];
        this.value = new char[capacity];
    }

    public Char2CharArrayMap(Char2CharMap m) {
        this(m.size());
        int i = 0;
        for (Char2CharMap.Entry e : m.char2CharEntrySet()) {
            this.key[i] = e.getCharKey();
            this.value[i] = e.getCharValue();
            ++i;
        }
        this.size = i;
    }

    public Char2CharArrayMap(Map<? extends Character, ? extends Character> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<? extends Character, ? extends Character> e : m.entrySet()) {
            this.key[i] = e.getKey().charValue();
            this.value[i] = e.getValue().charValue();
            ++i;
        }
        this.size = i;
    }

    public Char2CharArrayMap(char[] key, char[] value, int size) {
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

    public Char2CharMap.FastEntrySet char2CharEntrySet() {
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
    public char get(char k) {
        char[] key = this.key;
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
        this.size = 0;
    }

    @Override
    public boolean containsKey(char k) {
        return this.findKey(k) != -1;
    }

    @Override
    public boolean containsValue(char v) {
        char[] value = this.value;
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
    public char put(char k, char v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            char oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return oldValue;
        }
        if (this.size == this.key.length) {
            char[] newKey = new char[this.size == 0 ? 2 : this.size * 2];
            char[] newValue = new char[this.size == 0 ? 2 : this.size * 2];
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
    public char remove(char k) {
        int oldPos = this.findKey(k);
        if (oldPos == -1) {
            return this.defRetValue;
        }
        char oldValue = this.value[oldPos];
        int tail = this.size - oldPos - 1;
        System.arraycopy(this.key, oldPos + 1, this.key, oldPos, tail);
        System.arraycopy(this.value, oldPos + 1, this.value, oldPos, tail);
        --this.size;
        return oldValue;
    }

    @Override
    public CharSet keySet() {
        if (this.keys == null) {
            this.keys = new KeySet();
        }
        return this.keys;
    }

    @Override
    public CharCollection values() {
        if (this.values == null) {
            this.values = new ValuesCollection();
        }
        return this.values;
    }

    public Char2CharArrayMap clone() {
        Char2CharArrayMap c;
        try {
            c = (Char2CharArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (char[])this.key.clone();
        c.value = (char[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        char[] key = this.key;
        char[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeChar(key[i]);
            s.writeChar(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new char[this.size];
        char[] key = this.key;
        this.value = new char[this.size];
        char[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readChar();
            value[i] = s.readChar();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Char2CharMap.Entry>
    implements Char2CharMap.FastEntrySet {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Char2CharMap.Entry> iterator() {
            return new ObjectIterator<Char2CharMap.Entry>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Char2CharArrayMap.this.size;
                }

                @Override
                public Char2CharMap.Entry next() {
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
                    int tail = Char2CharArrayMap.this.size-- - this.next--;
                    System.arraycopy(Char2CharArrayMap.this.key, this.next + 1, Char2CharArrayMap.this.key, this.next, tail);
                    System.arraycopy(Char2CharArrayMap.this.value, this.next + 1, Char2CharArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Char2CharArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Char2CharMap.Entry> action) {
                    int max = Char2CharArrayMap.this.size;
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
        public ObjectIterator<Char2CharMap.Entry> fastIterator() {
            return new ObjectIterator<Char2CharMap.Entry>(){
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
                    return this.next < Char2CharArrayMap.this.size;
                }

                @Override
                public Char2CharMap.Entry next() {
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
                    int tail = Char2CharArrayMap.this.size-- - this.next--;
                    System.arraycopy(Char2CharArrayMap.this.key, this.next + 1, Char2CharArrayMap.this.key, this.next, tail);
                    System.arraycopy(Char2CharArrayMap.this.value, this.next + 1, Char2CharArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Char2CharArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Char2CharMap.Entry> action) {
                    int max = Char2CharArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Char2CharMap.Entry> spliterator() {
            return new EntrySetSpliterator(0, Char2CharArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Char2CharMap.Entry> action) {
            int max = Char2CharArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Char2CharMap.Entry> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Char2CharArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Char2CharArrayMap.this.size;
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
            if (e.getValue() == null || !(e.getValue() instanceof Character)) {
                return false;
            }
            char k = ((Character)e.getKey()).charValue();
            return Char2CharArrayMap.this.containsKey(k) && Char2CharArrayMap.this.get(k) == ((Character)e.getValue()).charValue();
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
            if (e.getValue() == null || !(e.getValue() instanceof Character)) {
                return false;
            }
            char k = ((Character)e.getKey()).charValue();
            char v = ((Character)e.getValue()).charValue();
            int oldPos = Char2CharArrayMap.this.findKey(k);
            if (oldPos == -1 || v != Char2CharArrayMap.this.value[oldPos]) {
                return false;
            }
            int tail = Char2CharArrayMap.this.size - oldPos - 1;
            System.arraycopy(Char2CharArrayMap.this.key, oldPos + 1, Char2CharArrayMap.this.key, oldPos, tail);
            System.arraycopy(Char2CharArrayMap.this.value, oldPos + 1, Char2CharArrayMap.this.value, oldPos, tail);
            --Char2CharArrayMap.this.size;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Char2CharMap.Entry>
        implements ObjectSpliterator<Char2CharMap.Entry> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Char2CharMap.Entry get(int location) {
                return new MapEntry(location);
            }

            protected final EntrySetSpliterator makeForSplit(int pos, int maxPos) {
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
            return Char2CharArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(char k) {
            int oldPos = Char2CharArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Char2CharArrayMap.this.size - oldPos - 1;
            System.arraycopy(Char2CharArrayMap.this.key, oldPos + 1, Char2CharArrayMap.this.key, oldPos, tail);
            System.arraycopy(Char2CharArrayMap.this.value, oldPos + 1, Char2CharArrayMap.this.value, oldPos, tail);
            --Char2CharArrayMap.this.size;
            return true;
        }

        @Override
        public CharIterator iterator() {
            return new CharIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Char2CharArrayMap.this.size;
                }

                @Override
                public char nextChar() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Char2CharArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Char2CharArrayMap.this.size - this.pos;
                    System.arraycopy(Char2CharArrayMap.this.key, this.pos, Char2CharArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Char2CharArrayMap.this.value, this.pos, Char2CharArrayMap.this.value, this.pos - 1, tail);
                    --Char2CharArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(CharConsumer action) {
                    char[] key = Char2CharArrayMap.this.key;
                    int max = Char2CharArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public CharSpliterator spliterator() {
            return new KeySetSpliterator(0, Char2CharArrayMap.this.size);
        }

        @Override
        public void forEach(CharConsumer action) {
            char[] key = Char2CharArrayMap.this.key;
            int max = Char2CharArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Char2CharArrayMap.this.size;
        }

        @Override
        public void clear() {
            Char2CharArrayMap.this.clear();
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
                return Char2CharArrayMap.this.key[location];
            }

            @Override
            protected final KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(CharConsumer action) {
                char[] key = Char2CharArrayMap.this.key;
                int max = Char2CharArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(key[this.pos++]);
                }
            }
        }
    }

    private final class ValuesCollection
    extends AbstractCharCollection {
        private ValuesCollection() {
        }

        @Override
        public boolean contains(char v) {
            return Char2CharArrayMap.this.containsValue(v);
        }

        @Override
        public CharIterator iterator() {
            return new CharIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Char2CharArrayMap.this.size;
                }

                @Override
                public char nextChar() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Char2CharArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Char2CharArrayMap.this.size - this.pos;
                    System.arraycopy(Char2CharArrayMap.this.key, this.pos, Char2CharArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Char2CharArrayMap.this.value, this.pos, Char2CharArrayMap.this.value, this.pos - 1, tail);
                    --Char2CharArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(CharConsumer action) {
                    char[] value = Char2CharArrayMap.this.value;
                    int max = Char2CharArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public CharSpliterator spliterator() {
            return new ValuesSpliterator(0, Char2CharArrayMap.this.size);
        }

        @Override
        public void forEach(CharConsumer action) {
            char[] value = Char2CharArrayMap.this.value;
            int max = Char2CharArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Char2CharArrayMap.this.size;
        }

        @Override
        public void clear() {
            Char2CharArrayMap.this.clear();
        }

        final class ValuesSpliterator
        extends CharSpliterators.EarlyBindingSizeIndexBasedSpliterator
        implements CharSpliterator {
            ValuesSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16720;
            }

            @Override
            protected final char get(int location) {
                return Char2CharArrayMap.this.value[location];
            }

            @Override
            protected final ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(CharConsumer action) {
                char[] value = Char2CharArrayMap.this.value;
                int max = Char2CharArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Char2CharMap.Entry,
    Map.Entry<Character, Character>,
    CharCharPair {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public char getCharKey() {
            return Char2CharArrayMap.this.key[this.index];
        }

        @Override
        public char leftChar() {
            return Char2CharArrayMap.this.key[this.index];
        }

        @Override
        public char getCharValue() {
            return Char2CharArrayMap.this.value[this.index];
        }

        @Override
        public char rightChar() {
            return Char2CharArrayMap.this.value[this.index];
        }

        @Override
        public char setValue(char v) {
            char oldValue = Char2CharArrayMap.this.value[this.index];
            Char2CharArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        @Override
        public CharCharPair right(char v) {
            Char2CharArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Character getKey() {
            return Character.valueOf(Char2CharArrayMap.this.key[this.index]);
        }

        @Override
        @Deprecated
        public Character getValue() {
            return Character.valueOf(Char2CharArrayMap.this.value[this.index]);
        }

        @Override
        @Deprecated
        public Character setValue(Character v) {
            return Character.valueOf(this.setValue(v.charValue()));
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            return Char2CharArrayMap.this.key[this.index] == ((Character)e.getKey()).charValue() && Char2CharArrayMap.this.value[this.index] == ((Character)e.getValue()).charValue();
        }

        @Override
        public int hashCode() {
            return Char2CharArrayMap.this.key[this.index] ^ Char2CharArrayMap.this.value[this.index];
        }

        public String toString() {
            return Char2CharArrayMap.this.key[this.index] + "=>" + Char2CharArrayMap.this.value[this.index];
        }
    }
}

