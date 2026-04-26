/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.longs;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.chars.AbstractCharCollection;
import it.unimi.dsi.fastutil.chars.CharArrays;
import it.unimi.dsi.fastutil.chars.CharCollection;
import it.unimi.dsi.fastutil.chars.CharConsumer;
import it.unimi.dsi.fastutil.chars.CharIterator;
import it.unimi.dsi.fastutil.chars.CharSpliterator;
import it.unimi.dsi.fastutil.chars.CharSpliterators;
import it.unimi.dsi.fastutil.longs.AbstractLong2CharMap;
import it.unimi.dsi.fastutil.longs.AbstractLongSet;
import it.unimi.dsi.fastutil.longs.Long2CharMap;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongCharPair;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSpliterator;
import it.unimi.dsi.fastutil.longs.LongSpliterators;
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
import java.util.function.LongConsumer;

public class Long2CharArrayMap
extends AbstractLong2CharMap
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient long[] key;
    protected transient char[] value;
    protected int size;
    protected transient Long2CharMap.FastEntrySet entries;
    protected transient LongSet keys;
    protected transient CharCollection values;

    public Long2CharArrayMap(long[] key, char[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Long2CharArrayMap() {
        this.key = LongArrays.EMPTY_ARRAY;
        this.value = CharArrays.EMPTY_ARRAY;
    }

    public Long2CharArrayMap(int capacity) {
        this.key = new long[capacity];
        this.value = new char[capacity];
    }

    public Long2CharArrayMap(Long2CharMap m) {
        this(m.size());
        int i = 0;
        for (Long2CharMap.Entry e : m.long2CharEntrySet()) {
            this.key[i] = e.getLongKey();
            this.value[i] = e.getCharValue();
            ++i;
        }
        this.size = i;
    }

    public Long2CharArrayMap(Map<? extends Long, ? extends Character> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<? extends Long, ? extends Character> e : m.entrySet()) {
            this.key[i] = e.getKey();
            this.value[i] = e.getValue().charValue();
            ++i;
        }
        this.size = i;
    }

    public Long2CharArrayMap(long[] key, char[] value, int size) {
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

    public Long2CharMap.FastEntrySet long2CharEntrySet() {
        if (this.entries == null) {
            this.entries = new EntrySet();
        }
        return this.entries;
    }

    private int findKey(long k) {
        long[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (key[i] != k) continue;
            return i;
        }
        return -1;
    }

    @Override
    public char get(long k) {
        long[] key = this.key;
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
    public boolean containsKey(long k) {
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
    public char put(long k, char v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            char oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return oldValue;
        }
        if (this.size == this.key.length) {
            long[] newKey = new long[this.size == 0 ? 2 : this.size * 2];
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
    public char remove(long k) {
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
    public LongSet keySet() {
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

    public Long2CharArrayMap clone() {
        Long2CharArrayMap c;
        try {
            c = (Long2CharArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (long[])this.key.clone();
        c.value = (char[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        long[] key = this.key;
        char[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeLong(key[i]);
            s.writeChar(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new long[this.size];
        long[] key = this.key;
        this.value = new char[this.size];
        char[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readLong();
            value[i] = s.readChar();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Long2CharMap.Entry>
    implements Long2CharMap.FastEntrySet {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Long2CharMap.Entry> iterator() {
            return new ObjectIterator<Long2CharMap.Entry>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Long2CharArrayMap.this.size;
                }

                @Override
                public Long2CharMap.Entry next() {
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
                    int tail = Long2CharArrayMap.this.size-- - this.next--;
                    System.arraycopy(Long2CharArrayMap.this.key, this.next + 1, Long2CharArrayMap.this.key, this.next, tail);
                    System.arraycopy(Long2CharArrayMap.this.value, this.next + 1, Long2CharArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Long2CharArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Long2CharMap.Entry> action) {
                    int max = Long2CharArrayMap.this.size;
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
        public ObjectIterator<Long2CharMap.Entry> fastIterator() {
            return new ObjectIterator<Long2CharMap.Entry>(){
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
                    return this.next < Long2CharArrayMap.this.size;
                }

                @Override
                public Long2CharMap.Entry next() {
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
                    int tail = Long2CharArrayMap.this.size-- - this.next--;
                    System.arraycopy(Long2CharArrayMap.this.key, this.next + 1, Long2CharArrayMap.this.key, this.next, tail);
                    System.arraycopy(Long2CharArrayMap.this.value, this.next + 1, Long2CharArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Long2CharArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Long2CharMap.Entry> action) {
                    int max = Long2CharArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Long2CharMap.Entry> spliterator() {
            return new EntrySetSpliterator(0, Long2CharArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Long2CharMap.Entry> action) {
            int max = Long2CharArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Long2CharMap.Entry> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Long2CharArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Long2CharArrayMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Long)) {
                return false;
            }
            if (e.getValue() == null || !(e.getValue() instanceof Character)) {
                return false;
            }
            long k = (Long)e.getKey();
            return Long2CharArrayMap.this.containsKey(k) && Long2CharArrayMap.this.get(k) == ((Character)e.getValue()).charValue();
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Long)) {
                return false;
            }
            if (e.getValue() == null || !(e.getValue() instanceof Character)) {
                return false;
            }
            long k = (Long)e.getKey();
            char v = ((Character)e.getValue()).charValue();
            int oldPos = Long2CharArrayMap.this.findKey(k);
            if (oldPos == -1 || v != Long2CharArrayMap.this.value[oldPos]) {
                return false;
            }
            int tail = Long2CharArrayMap.this.size - oldPos - 1;
            System.arraycopy(Long2CharArrayMap.this.key, oldPos + 1, Long2CharArrayMap.this.key, oldPos, tail);
            System.arraycopy(Long2CharArrayMap.this.value, oldPos + 1, Long2CharArrayMap.this.value, oldPos, tail);
            --Long2CharArrayMap.this.size;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Long2CharMap.Entry>
        implements ObjectSpliterator<Long2CharMap.Entry> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Long2CharMap.Entry get(int location) {
                return new MapEntry(location);
            }

            protected final EntrySetSpliterator makeForSplit(int pos, int maxPos) {
                return new EntrySetSpliterator(pos, maxPos);
            }
        }
    }

    private final class KeySet
    extends AbstractLongSet {
        private KeySet() {
        }

        @Override
        public boolean contains(long k) {
            return Long2CharArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(long k) {
            int oldPos = Long2CharArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Long2CharArrayMap.this.size - oldPos - 1;
            System.arraycopy(Long2CharArrayMap.this.key, oldPos + 1, Long2CharArrayMap.this.key, oldPos, tail);
            System.arraycopy(Long2CharArrayMap.this.value, oldPos + 1, Long2CharArrayMap.this.value, oldPos, tail);
            --Long2CharArrayMap.this.size;
            return true;
        }

        @Override
        public LongIterator iterator() {
            return new LongIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Long2CharArrayMap.this.size;
                }

                @Override
                public long nextLong() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Long2CharArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Long2CharArrayMap.this.size - this.pos;
                    System.arraycopy(Long2CharArrayMap.this.key, this.pos, Long2CharArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Long2CharArrayMap.this.value, this.pos, Long2CharArrayMap.this.value, this.pos - 1, tail);
                    --Long2CharArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(LongConsumer action) {
                    long[] key = Long2CharArrayMap.this.key;
                    int max = Long2CharArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public LongSpliterator spliterator() {
            return new KeySetSpliterator(0, Long2CharArrayMap.this.size);
        }

        @Override
        public void forEach(LongConsumer action) {
            long[] key = Long2CharArrayMap.this.key;
            int max = Long2CharArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Long2CharArrayMap.this.size;
        }

        @Override
        public void clear() {
            Long2CharArrayMap.this.clear();
        }

        final class KeySetSpliterator
        extends LongSpliterators.EarlyBindingSizeIndexBasedSpliterator
        implements LongSpliterator {
            KeySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16721;
            }

            @Override
            protected final long get(int location) {
                return Long2CharArrayMap.this.key[location];
            }

            @Override
            protected final KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(LongConsumer action) {
                long[] key = Long2CharArrayMap.this.key;
                int max = Long2CharArrayMap.this.size;
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
            return Long2CharArrayMap.this.containsValue(v);
        }

        @Override
        public CharIterator iterator() {
            return new CharIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Long2CharArrayMap.this.size;
                }

                @Override
                public char nextChar() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Long2CharArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Long2CharArrayMap.this.size - this.pos;
                    System.arraycopy(Long2CharArrayMap.this.key, this.pos, Long2CharArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Long2CharArrayMap.this.value, this.pos, Long2CharArrayMap.this.value, this.pos - 1, tail);
                    --Long2CharArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(CharConsumer action) {
                    char[] value = Long2CharArrayMap.this.value;
                    int max = Long2CharArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public CharSpliterator spliterator() {
            return new ValuesSpliterator(0, Long2CharArrayMap.this.size);
        }

        @Override
        public void forEach(CharConsumer action) {
            char[] value = Long2CharArrayMap.this.value;
            int max = Long2CharArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Long2CharArrayMap.this.size;
        }

        @Override
        public void clear() {
            Long2CharArrayMap.this.clear();
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
                return Long2CharArrayMap.this.value[location];
            }

            @Override
            protected final ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(CharConsumer action) {
                char[] value = Long2CharArrayMap.this.value;
                int max = Long2CharArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Long2CharMap.Entry,
    Map.Entry<Long, Character>,
    LongCharPair {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public long getLongKey() {
            return Long2CharArrayMap.this.key[this.index];
        }

        @Override
        public long leftLong() {
            return Long2CharArrayMap.this.key[this.index];
        }

        @Override
        public char getCharValue() {
            return Long2CharArrayMap.this.value[this.index];
        }

        @Override
        public char rightChar() {
            return Long2CharArrayMap.this.value[this.index];
        }

        @Override
        public char setValue(char v) {
            char oldValue = Long2CharArrayMap.this.value[this.index];
            Long2CharArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        @Override
        public LongCharPair right(char v) {
            Long2CharArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Long getKey() {
            return Long2CharArrayMap.this.key[this.index];
        }

        @Override
        @Deprecated
        public Character getValue() {
            return Character.valueOf(Long2CharArrayMap.this.value[this.index]);
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
            return Long2CharArrayMap.this.key[this.index] == (Long)e.getKey() && Long2CharArrayMap.this.value[this.index] == ((Character)e.getValue()).charValue();
        }

        @Override
        public int hashCode() {
            return HashCommon.long2int(Long2CharArrayMap.this.key[this.index]) ^ Long2CharArrayMap.this.value[this.index];
        }

        public String toString() {
            return Long2CharArrayMap.this.key[this.index] + "=>" + Long2CharArrayMap.this.value[this.index];
        }
    }
}

