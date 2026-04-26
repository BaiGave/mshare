/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.chars;

import it.unimi.dsi.fastutil.bytes.AbstractByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteConsumer;
import it.unimi.dsi.fastutil.bytes.ByteIterator;
import it.unimi.dsi.fastutil.bytes.ByteSpliterator;
import it.unimi.dsi.fastutil.bytes.ByteSpliterators;
import it.unimi.dsi.fastutil.chars.AbstractChar2ByteMap;
import it.unimi.dsi.fastutil.chars.AbstractCharSet;
import it.unimi.dsi.fastutil.chars.Char2ByteMap;
import it.unimi.dsi.fastutil.chars.CharArrays;
import it.unimi.dsi.fastutil.chars.CharBytePair;
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

public class Char2ByteArrayMap
extends AbstractChar2ByteMap
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient char[] key;
    protected transient byte[] value;
    protected int size;
    protected transient Char2ByteMap.FastEntrySet entries;
    protected transient CharSet keys;
    protected transient ByteCollection values;

    public Char2ByteArrayMap(char[] key, byte[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Char2ByteArrayMap() {
        this.key = CharArrays.EMPTY_ARRAY;
        this.value = ByteArrays.EMPTY_ARRAY;
    }

    public Char2ByteArrayMap(int capacity) {
        this.key = new char[capacity];
        this.value = new byte[capacity];
    }

    public Char2ByteArrayMap(Char2ByteMap m) {
        this(m.size());
        int i = 0;
        for (Char2ByteMap.Entry e : m.char2ByteEntrySet()) {
            this.key[i] = e.getCharKey();
            this.value[i] = e.getByteValue();
            ++i;
        }
        this.size = i;
    }

    public Char2ByteArrayMap(Map<? extends Character, ? extends Byte> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<? extends Character, ? extends Byte> e : m.entrySet()) {
            this.key[i] = e.getKey().charValue();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Char2ByteArrayMap(char[] key, byte[] value, int size) {
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

    public Char2ByteMap.FastEntrySet char2ByteEntrySet() {
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
    public byte get(char k) {
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
    public byte put(char k, byte v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            byte oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return oldValue;
        }
        if (this.size == this.key.length) {
            char[] newKey = new char[this.size == 0 ? 2 : this.size * 2];
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
    public byte remove(char k) {
        int oldPos = this.findKey(k);
        if (oldPos == -1) {
            return this.defRetValue;
        }
        byte oldValue = this.value[oldPos];
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
    public ByteCollection values() {
        if (this.values == null) {
            this.values = new ValuesCollection();
        }
        return this.values;
    }

    public Char2ByteArrayMap clone() {
        Char2ByteArrayMap c;
        try {
            c = (Char2ByteArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (char[])this.key.clone();
        c.value = (byte[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        char[] key = this.key;
        byte[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeChar(key[i]);
            s.writeByte(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new char[this.size];
        char[] key = this.key;
        this.value = new byte[this.size];
        byte[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readChar();
            value[i] = s.readByte();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Char2ByteMap.Entry>
    implements Char2ByteMap.FastEntrySet {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Char2ByteMap.Entry> iterator() {
            return new ObjectIterator<Char2ByteMap.Entry>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Char2ByteArrayMap.this.size;
                }

                @Override
                public Char2ByteMap.Entry next() {
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
                    int tail = Char2ByteArrayMap.this.size-- - this.next--;
                    System.arraycopy(Char2ByteArrayMap.this.key, this.next + 1, Char2ByteArrayMap.this.key, this.next, tail);
                    System.arraycopy(Char2ByteArrayMap.this.value, this.next + 1, Char2ByteArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Char2ByteArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Char2ByteMap.Entry> action) {
                    int max = Char2ByteArrayMap.this.size;
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
        public ObjectIterator<Char2ByteMap.Entry> fastIterator() {
            return new ObjectIterator<Char2ByteMap.Entry>(){
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
                    return this.next < Char2ByteArrayMap.this.size;
                }

                @Override
                public Char2ByteMap.Entry next() {
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
                    int tail = Char2ByteArrayMap.this.size-- - this.next--;
                    System.arraycopy(Char2ByteArrayMap.this.key, this.next + 1, Char2ByteArrayMap.this.key, this.next, tail);
                    System.arraycopy(Char2ByteArrayMap.this.value, this.next + 1, Char2ByteArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Char2ByteArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Char2ByteMap.Entry> action) {
                    int max = Char2ByteArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Char2ByteMap.Entry> spliterator() {
            return new EntrySetSpliterator(0, Char2ByteArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Char2ByteMap.Entry> action) {
            int max = Char2ByteArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Char2ByteMap.Entry> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Char2ByteArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Char2ByteArrayMap.this.size;
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
            if (e.getValue() == null || !(e.getValue() instanceof Byte)) {
                return false;
            }
            char k = ((Character)e.getKey()).charValue();
            return Char2ByteArrayMap.this.containsKey(k) && Char2ByteArrayMap.this.get(k) == ((Byte)e.getValue()).byteValue();
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
            if (e.getValue() == null || !(e.getValue() instanceof Byte)) {
                return false;
            }
            char k = ((Character)e.getKey()).charValue();
            byte v = (Byte)e.getValue();
            int oldPos = Char2ByteArrayMap.this.findKey(k);
            if (oldPos == -1 || v != Char2ByteArrayMap.this.value[oldPos]) {
                return false;
            }
            int tail = Char2ByteArrayMap.this.size - oldPos - 1;
            System.arraycopy(Char2ByteArrayMap.this.key, oldPos + 1, Char2ByteArrayMap.this.key, oldPos, tail);
            System.arraycopy(Char2ByteArrayMap.this.value, oldPos + 1, Char2ByteArrayMap.this.value, oldPos, tail);
            --Char2ByteArrayMap.this.size;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Char2ByteMap.Entry>
        implements ObjectSpliterator<Char2ByteMap.Entry> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Char2ByteMap.Entry get(int location) {
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
            return Char2ByteArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(char k) {
            int oldPos = Char2ByteArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Char2ByteArrayMap.this.size - oldPos - 1;
            System.arraycopy(Char2ByteArrayMap.this.key, oldPos + 1, Char2ByteArrayMap.this.key, oldPos, tail);
            System.arraycopy(Char2ByteArrayMap.this.value, oldPos + 1, Char2ByteArrayMap.this.value, oldPos, tail);
            --Char2ByteArrayMap.this.size;
            return true;
        }

        @Override
        public CharIterator iterator() {
            return new CharIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Char2ByteArrayMap.this.size;
                }

                @Override
                public char nextChar() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Char2ByteArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Char2ByteArrayMap.this.size - this.pos;
                    System.arraycopy(Char2ByteArrayMap.this.key, this.pos, Char2ByteArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Char2ByteArrayMap.this.value, this.pos, Char2ByteArrayMap.this.value, this.pos - 1, tail);
                    --Char2ByteArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(CharConsumer action) {
                    char[] key = Char2ByteArrayMap.this.key;
                    int max = Char2ByteArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public CharSpliterator spliterator() {
            return new KeySetSpliterator(0, Char2ByteArrayMap.this.size);
        }

        @Override
        public void forEach(CharConsumer action) {
            char[] key = Char2ByteArrayMap.this.key;
            int max = Char2ByteArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Char2ByteArrayMap.this.size;
        }

        @Override
        public void clear() {
            Char2ByteArrayMap.this.clear();
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
                return Char2ByteArrayMap.this.key[location];
            }

            @Override
            protected final KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(CharConsumer action) {
                char[] key = Char2ByteArrayMap.this.key;
                int max = Char2ByteArrayMap.this.size;
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
            return Char2ByteArrayMap.this.containsValue(v);
        }

        @Override
        public ByteIterator iterator() {
            return new ByteIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Char2ByteArrayMap.this.size;
                }

                @Override
                public byte nextByte() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Char2ByteArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Char2ByteArrayMap.this.size - this.pos;
                    System.arraycopy(Char2ByteArrayMap.this.key, this.pos, Char2ByteArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Char2ByteArrayMap.this.value, this.pos, Char2ByteArrayMap.this.value, this.pos - 1, tail);
                    --Char2ByteArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(ByteConsumer action) {
                    byte[] value = Char2ByteArrayMap.this.value;
                    int max = Char2ByteArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ByteSpliterator spliterator() {
            return new ValuesSpliterator(0, Char2ByteArrayMap.this.size);
        }

        @Override
        public void forEach(ByteConsumer action) {
            byte[] value = Char2ByteArrayMap.this.value;
            int max = Char2ByteArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Char2ByteArrayMap.this.size;
        }

        @Override
        public void clear() {
            Char2ByteArrayMap.this.clear();
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
                return Char2ByteArrayMap.this.value[location];
            }

            @Override
            protected final ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(ByteConsumer action) {
                byte[] value = Char2ByteArrayMap.this.value;
                int max = Char2ByteArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Char2ByteMap.Entry,
    Map.Entry<Character, Byte>,
    CharBytePair {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public char getCharKey() {
            return Char2ByteArrayMap.this.key[this.index];
        }

        @Override
        public char leftChar() {
            return Char2ByteArrayMap.this.key[this.index];
        }

        @Override
        public byte getByteValue() {
            return Char2ByteArrayMap.this.value[this.index];
        }

        @Override
        public byte rightByte() {
            return Char2ByteArrayMap.this.value[this.index];
        }

        @Override
        public byte setValue(byte v) {
            byte oldValue = Char2ByteArrayMap.this.value[this.index];
            Char2ByteArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        @Override
        public CharBytePair right(byte v) {
            Char2ByteArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Character getKey() {
            return Character.valueOf(Char2ByteArrayMap.this.key[this.index]);
        }

        @Override
        @Deprecated
        public Byte getValue() {
            return Char2ByteArrayMap.this.value[this.index];
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
            return Char2ByteArrayMap.this.key[this.index] == ((Character)e.getKey()).charValue() && Char2ByteArrayMap.this.value[this.index] == (Byte)e.getValue();
        }

        @Override
        public int hashCode() {
            return Char2ByteArrayMap.this.key[this.index] ^ Char2ByteArrayMap.this.value[this.index];
        }

        public String toString() {
            return Char2ByteArrayMap.this.key[this.index] + "=>" + Char2ByteArrayMap.this.value[this.index];
        }
    }
}

