/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.bytes;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.bytes.AbstractByte2LongMap;
import it.unimi.dsi.fastutil.bytes.AbstractByteSet;
import it.unimi.dsi.fastutil.bytes.Byte2LongMap;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.bytes.ByteConsumer;
import it.unimi.dsi.fastutil.bytes.ByteIterator;
import it.unimi.dsi.fastutil.bytes.ByteLongPair;
import it.unimi.dsi.fastutil.bytes.ByteSet;
import it.unimi.dsi.fastutil.bytes.ByteSpliterator;
import it.unimi.dsi.fastutil.bytes.ByteSpliterators;
import it.unimi.dsi.fastutil.longs.AbstractLongCollection;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
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

public class Byte2LongArrayMap
extends AbstractByte2LongMap
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient byte[] key;
    protected transient long[] value;
    protected int size;
    protected transient Byte2LongMap.FastEntrySet entries;
    protected transient ByteSet keys;
    protected transient LongCollection values;

    public Byte2LongArrayMap(byte[] key, long[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Byte2LongArrayMap() {
        this.key = ByteArrays.EMPTY_ARRAY;
        this.value = LongArrays.EMPTY_ARRAY;
    }

    public Byte2LongArrayMap(int capacity) {
        this.key = new byte[capacity];
        this.value = new long[capacity];
    }

    public Byte2LongArrayMap(Byte2LongMap m) {
        this(m.size());
        int i = 0;
        for (Byte2LongMap.Entry e : m.byte2LongEntrySet()) {
            this.key[i] = e.getByteKey();
            this.value[i] = e.getLongValue();
            ++i;
        }
        this.size = i;
    }

    public Byte2LongArrayMap(Map<? extends Byte, ? extends Long> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<? extends Byte, ? extends Long> e : m.entrySet()) {
            this.key[i] = e.getKey();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Byte2LongArrayMap(byte[] key, long[] value, int size) {
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

    public Byte2LongMap.FastEntrySet byte2LongEntrySet() {
        if (this.entries == null) {
            this.entries = new EntrySet();
        }
        return this.entries;
    }

    private int findKey(byte k) {
        byte[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (key[i] != k) continue;
            return i;
        }
        return -1;
    }

    @Override
    public long get(byte k) {
        byte[] key = this.key;
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
    public boolean containsKey(byte k) {
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
    public long put(byte k, long v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            long oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return oldValue;
        }
        if (this.size == this.key.length) {
            byte[] newKey = new byte[this.size == 0 ? 2 : this.size * 2];
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
    public long remove(byte k) {
        int oldPos = this.findKey(k);
        if (oldPos == -1) {
            return this.defRetValue;
        }
        long oldValue = this.value[oldPos];
        int tail = this.size - oldPos - 1;
        System.arraycopy(this.key, oldPos + 1, this.key, oldPos, tail);
        System.arraycopy(this.value, oldPos + 1, this.value, oldPos, tail);
        --this.size;
        return oldValue;
    }

    @Override
    public ByteSet keySet() {
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

    public Byte2LongArrayMap clone() {
        Byte2LongArrayMap c;
        try {
            c = (Byte2LongArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (byte[])this.key.clone();
        c.value = (long[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        byte[] key = this.key;
        long[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeByte(key[i]);
            s.writeLong(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new byte[this.size];
        byte[] key = this.key;
        this.value = new long[this.size];
        long[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readByte();
            value[i] = s.readLong();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Byte2LongMap.Entry>
    implements Byte2LongMap.FastEntrySet {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Byte2LongMap.Entry> iterator() {
            return new ObjectIterator<Byte2LongMap.Entry>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Byte2LongArrayMap.this.size;
                }

                @Override
                public Byte2LongMap.Entry next() {
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
                    int tail = Byte2LongArrayMap.this.size-- - this.next--;
                    System.arraycopy(Byte2LongArrayMap.this.key, this.next + 1, Byte2LongArrayMap.this.key, this.next, tail);
                    System.arraycopy(Byte2LongArrayMap.this.value, this.next + 1, Byte2LongArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Byte2LongArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Byte2LongMap.Entry> action) {
                    int max = Byte2LongArrayMap.this.size;
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
        public ObjectIterator<Byte2LongMap.Entry> fastIterator() {
            return new ObjectIterator<Byte2LongMap.Entry>(){
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
                    return this.next < Byte2LongArrayMap.this.size;
                }

                @Override
                public Byte2LongMap.Entry next() {
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
                    int tail = Byte2LongArrayMap.this.size-- - this.next--;
                    System.arraycopy(Byte2LongArrayMap.this.key, this.next + 1, Byte2LongArrayMap.this.key, this.next, tail);
                    System.arraycopy(Byte2LongArrayMap.this.value, this.next + 1, Byte2LongArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Byte2LongArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Byte2LongMap.Entry> action) {
                    int max = Byte2LongArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Byte2LongMap.Entry> spliterator() {
            return new EntrySetSpliterator(0, Byte2LongArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Byte2LongMap.Entry> action) {
            int max = Byte2LongArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Byte2LongMap.Entry> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Byte2LongArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Byte2LongArrayMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Byte)) {
                return false;
            }
            if (e.getValue() == null || !(e.getValue() instanceof Long)) {
                return false;
            }
            byte k = (Byte)e.getKey();
            return Byte2LongArrayMap.this.containsKey(k) && Byte2LongArrayMap.this.get(k) == ((Long)e.getValue()).longValue();
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Byte)) {
                return false;
            }
            if (e.getValue() == null || !(e.getValue() instanceof Long)) {
                return false;
            }
            byte k = (Byte)e.getKey();
            long v = (Long)e.getValue();
            int oldPos = Byte2LongArrayMap.this.findKey(k);
            if (oldPos == -1 || v != Byte2LongArrayMap.this.value[oldPos]) {
                return false;
            }
            int tail = Byte2LongArrayMap.this.size - oldPos - 1;
            System.arraycopy(Byte2LongArrayMap.this.key, oldPos + 1, Byte2LongArrayMap.this.key, oldPos, tail);
            System.arraycopy(Byte2LongArrayMap.this.value, oldPos + 1, Byte2LongArrayMap.this.value, oldPos, tail);
            --Byte2LongArrayMap.this.size;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Byte2LongMap.Entry>
        implements ObjectSpliterator<Byte2LongMap.Entry> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Byte2LongMap.Entry get(int location) {
                return new MapEntry(location);
            }

            protected final EntrySetSpliterator makeForSplit(int pos, int maxPos) {
                return new EntrySetSpliterator(pos, maxPos);
            }
        }
    }

    private final class KeySet
    extends AbstractByteSet {
        private KeySet() {
        }

        @Override
        public boolean contains(byte k) {
            return Byte2LongArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(byte k) {
            int oldPos = Byte2LongArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Byte2LongArrayMap.this.size - oldPos - 1;
            System.arraycopy(Byte2LongArrayMap.this.key, oldPos + 1, Byte2LongArrayMap.this.key, oldPos, tail);
            System.arraycopy(Byte2LongArrayMap.this.value, oldPos + 1, Byte2LongArrayMap.this.value, oldPos, tail);
            --Byte2LongArrayMap.this.size;
            return true;
        }

        @Override
        public ByteIterator iterator() {
            return new ByteIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Byte2LongArrayMap.this.size;
                }

                @Override
                public byte nextByte() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Byte2LongArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Byte2LongArrayMap.this.size - this.pos;
                    System.arraycopy(Byte2LongArrayMap.this.key, this.pos, Byte2LongArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Byte2LongArrayMap.this.value, this.pos, Byte2LongArrayMap.this.value, this.pos - 1, tail);
                    --Byte2LongArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(ByteConsumer action) {
                    byte[] key = Byte2LongArrayMap.this.key;
                    int max = Byte2LongArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ByteSpliterator spliterator() {
            return new KeySetSpliterator(0, Byte2LongArrayMap.this.size);
        }

        @Override
        public void forEach(ByteConsumer action) {
            byte[] key = Byte2LongArrayMap.this.key;
            int max = Byte2LongArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Byte2LongArrayMap.this.size;
        }

        @Override
        public void clear() {
            Byte2LongArrayMap.this.clear();
        }

        final class KeySetSpliterator
        extends ByteSpliterators.EarlyBindingSizeIndexBasedSpliterator
        implements ByteSpliterator {
            KeySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16721;
            }

            @Override
            protected final byte get(int location) {
                return Byte2LongArrayMap.this.key[location];
            }

            @Override
            protected final KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(ByteConsumer action) {
                byte[] key = Byte2LongArrayMap.this.key;
                int max = Byte2LongArrayMap.this.size;
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
            return Byte2LongArrayMap.this.containsValue(v);
        }

        @Override
        public LongIterator iterator() {
            return new LongIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Byte2LongArrayMap.this.size;
                }

                @Override
                public long nextLong() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Byte2LongArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Byte2LongArrayMap.this.size - this.pos;
                    System.arraycopy(Byte2LongArrayMap.this.key, this.pos, Byte2LongArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Byte2LongArrayMap.this.value, this.pos, Byte2LongArrayMap.this.value, this.pos - 1, tail);
                    --Byte2LongArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(LongConsumer action) {
                    long[] value = Byte2LongArrayMap.this.value;
                    int max = Byte2LongArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public LongSpliterator spliterator() {
            return new ValuesSpliterator(0, Byte2LongArrayMap.this.size);
        }

        @Override
        public void forEach(LongConsumer action) {
            long[] value = Byte2LongArrayMap.this.value;
            int max = Byte2LongArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Byte2LongArrayMap.this.size;
        }

        @Override
        public void clear() {
            Byte2LongArrayMap.this.clear();
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
                return Byte2LongArrayMap.this.value[location];
            }

            @Override
            protected final ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(LongConsumer action) {
                long[] value = Byte2LongArrayMap.this.value;
                int max = Byte2LongArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Byte2LongMap.Entry,
    Map.Entry<Byte, Long>,
    ByteLongPair {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public byte getByteKey() {
            return Byte2LongArrayMap.this.key[this.index];
        }

        @Override
        public byte leftByte() {
            return Byte2LongArrayMap.this.key[this.index];
        }

        @Override
        public long getLongValue() {
            return Byte2LongArrayMap.this.value[this.index];
        }

        @Override
        public long rightLong() {
            return Byte2LongArrayMap.this.value[this.index];
        }

        @Override
        public long setValue(long v) {
            long oldValue = Byte2LongArrayMap.this.value[this.index];
            Byte2LongArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        @Override
        public ByteLongPair right(long v) {
            Byte2LongArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Byte getKey() {
            return Byte2LongArrayMap.this.key[this.index];
        }

        @Override
        @Deprecated
        public Long getValue() {
            return Byte2LongArrayMap.this.value[this.index];
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
            return Byte2LongArrayMap.this.key[this.index] == (Byte)e.getKey() && Byte2LongArrayMap.this.value[this.index] == (Long)e.getValue();
        }

        @Override
        public int hashCode() {
            return Byte2LongArrayMap.this.key[this.index] ^ HashCommon.long2int(Byte2LongArrayMap.this.value[this.index]);
        }

        public String toString() {
            return Byte2LongArrayMap.this.key[this.index] + "=>" + Byte2LongArrayMap.this.value[this.index];
        }
    }
}

