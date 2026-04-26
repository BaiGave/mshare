/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.shorts;

import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterators;
import it.unimi.dsi.fastutil.shorts.AbstractShort2ShortMap;
import it.unimi.dsi.fastutil.shorts.AbstractShortCollection;
import it.unimi.dsi.fastutil.shorts.AbstractShortSet;
import it.unimi.dsi.fastutil.shorts.Short2ShortMap;
import it.unimi.dsi.fastutil.shorts.ShortArrays;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortConsumer;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import it.unimi.dsi.fastutil.shorts.ShortShortPair;
import it.unimi.dsi.fastutil.shorts.ShortSpliterator;
import it.unimi.dsi.fastutil.shorts.ShortSpliterators;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class Short2ShortArrayMap
extends AbstractShort2ShortMap
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient short[] key;
    protected transient short[] value;
    protected int size;
    protected transient Short2ShortMap.FastEntrySet entries;
    protected transient ShortSet keys;
    protected transient ShortCollection values;

    public Short2ShortArrayMap(short[] key, short[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Short2ShortArrayMap() {
        this.key = ShortArrays.EMPTY_ARRAY;
        this.value = ShortArrays.EMPTY_ARRAY;
    }

    public Short2ShortArrayMap(int capacity) {
        this.key = new short[capacity];
        this.value = new short[capacity];
    }

    public Short2ShortArrayMap(Short2ShortMap m) {
        this(m.size());
        int i = 0;
        for (Short2ShortMap.Entry e : m.short2ShortEntrySet()) {
            this.key[i] = e.getShortKey();
            this.value[i] = e.getShortValue();
            ++i;
        }
        this.size = i;
    }

    public Short2ShortArrayMap(Map<? extends Short, ? extends Short> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<? extends Short, ? extends Short> e : m.entrySet()) {
            this.key[i] = e.getKey();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Short2ShortArrayMap(short[] key, short[] value, int size) {
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

    public Short2ShortMap.FastEntrySet short2ShortEntrySet() {
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
    public short get(short k) {
        short[] key = this.key;
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
    public boolean containsKey(short k) {
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
    public short put(short k, short v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            short oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return oldValue;
        }
        if (this.size == this.key.length) {
            short[] newKey = new short[this.size == 0 ? 2 : this.size * 2];
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
    public short remove(short k) {
        int oldPos = this.findKey(k);
        if (oldPos == -1) {
            return this.defRetValue;
        }
        short oldValue = this.value[oldPos];
        int tail = this.size - oldPos - 1;
        System.arraycopy(this.key, oldPos + 1, this.key, oldPos, tail);
        System.arraycopy(this.value, oldPos + 1, this.value, oldPos, tail);
        --this.size;
        return oldValue;
    }

    @Override
    public ShortSet keySet() {
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

    public Short2ShortArrayMap clone() {
        Short2ShortArrayMap c;
        try {
            c = (Short2ShortArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (short[])this.key.clone();
        c.value = (short[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        short[] key = this.key;
        short[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeShort(key[i]);
            s.writeShort(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new short[this.size];
        short[] key = this.key;
        this.value = new short[this.size];
        short[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readShort();
            value[i] = s.readShort();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Short2ShortMap.Entry>
    implements Short2ShortMap.FastEntrySet {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Short2ShortMap.Entry> iterator() {
            return new ObjectIterator<Short2ShortMap.Entry>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Short2ShortArrayMap.this.size;
                }

                @Override
                public Short2ShortMap.Entry next() {
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
                    int tail = Short2ShortArrayMap.this.size-- - this.next--;
                    System.arraycopy(Short2ShortArrayMap.this.key, this.next + 1, Short2ShortArrayMap.this.key, this.next, tail);
                    System.arraycopy(Short2ShortArrayMap.this.value, this.next + 1, Short2ShortArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Short2ShortArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Short2ShortMap.Entry> action) {
                    int max = Short2ShortArrayMap.this.size;
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
        public ObjectIterator<Short2ShortMap.Entry> fastIterator() {
            return new ObjectIterator<Short2ShortMap.Entry>(){
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
                    return this.next < Short2ShortArrayMap.this.size;
                }

                @Override
                public Short2ShortMap.Entry next() {
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
                    int tail = Short2ShortArrayMap.this.size-- - this.next--;
                    System.arraycopy(Short2ShortArrayMap.this.key, this.next + 1, Short2ShortArrayMap.this.key, this.next, tail);
                    System.arraycopy(Short2ShortArrayMap.this.value, this.next + 1, Short2ShortArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Short2ShortArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Short2ShortMap.Entry> action) {
                    int max = Short2ShortArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Short2ShortMap.Entry> spliterator() {
            return new EntrySetSpliterator(0, Short2ShortArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Short2ShortMap.Entry> action) {
            int max = Short2ShortArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Short2ShortMap.Entry> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Short2ShortArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Short2ShortArrayMap.this.size;
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
            if (e.getValue() == null || !(e.getValue() instanceof Short)) {
                return false;
            }
            short k = (Short)e.getKey();
            return Short2ShortArrayMap.this.containsKey(k) && Short2ShortArrayMap.this.get(k) == ((Short)e.getValue()).shortValue();
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
            if (e.getValue() == null || !(e.getValue() instanceof Short)) {
                return false;
            }
            short k = (Short)e.getKey();
            short v = (Short)e.getValue();
            int oldPos = Short2ShortArrayMap.this.findKey(k);
            if (oldPos == -1 || v != Short2ShortArrayMap.this.value[oldPos]) {
                return false;
            }
            int tail = Short2ShortArrayMap.this.size - oldPos - 1;
            System.arraycopy(Short2ShortArrayMap.this.key, oldPos + 1, Short2ShortArrayMap.this.key, oldPos, tail);
            System.arraycopy(Short2ShortArrayMap.this.value, oldPos + 1, Short2ShortArrayMap.this.value, oldPos, tail);
            --Short2ShortArrayMap.this.size;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Short2ShortMap.Entry>
        implements ObjectSpliterator<Short2ShortMap.Entry> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Short2ShortMap.Entry get(int location) {
                return new MapEntry(location);
            }

            protected final EntrySetSpliterator makeForSplit(int pos, int maxPos) {
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
            return Short2ShortArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(short k) {
            int oldPos = Short2ShortArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Short2ShortArrayMap.this.size - oldPos - 1;
            System.arraycopy(Short2ShortArrayMap.this.key, oldPos + 1, Short2ShortArrayMap.this.key, oldPos, tail);
            System.arraycopy(Short2ShortArrayMap.this.value, oldPos + 1, Short2ShortArrayMap.this.value, oldPos, tail);
            --Short2ShortArrayMap.this.size;
            return true;
        }

        @Override
        public ShortIterator iterator() {
            return new ShortIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Short2ShortArrayMap.this.size;
                }

                @Override
                public short nextShort() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Short2ShortArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Short2ShortArrayMap.this.size - this.pos;
                    System.arraycopy(Short2ShortArrayMap.this.key, this.pos, Short2ShortArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Short2ShortArrayMap.this.value, this.pos, Short2ShortArrayMap.this.value, this.pos - 1, tail);
                    --Short2ShortArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(ShortConsumer action) {
                    short[] key = Short2ShortArrayMap.this.key;
                    int max = Short2ShortArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ShortSpliterator spliterator() {
            return new KeySetSpliterator(0, Short2ShortArrayMap.this.size);
        }

        @Override
        public void forEach(ShortConsumer action) {
            short[] key = Short2ShortArrayMap.this.key;
            int max = Short2ShortArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Short2ShortArrayMap.this.size;
        }

        @Override
        public void clear() {
            Short2ShortArrayMap.this.clear();
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
                return Short2ShortArrayMap.this.key[location];
            }

            @Override
            protected final KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(ShortConsumer action) {
                short[] key = Short2ShortArrayMap.this.key;
                int max = Short2ShortArrayMap.this.size;
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
            return Short2ShortArrayMap.this.containsValue(v);
        }

        @Override
        public ShortIterator iterator() {
            return new ShortIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Short2ShortArrayMap.this.size;
                }

                @Override
                public short nextShort() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Short2ShortArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Short2ShortArrayMap.this.size - this.pos;
                    System.arraycopy(Short2ShortArrayMap.this.key, this.pos, Short2ShortArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Short2ShortArrayMap.this.value, this.pos, Short2ShortArrayMap.this.value, this.pos - 1, tail);
                    --Short2ShortArrayMap.this.size;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(ShortConsumer action) {
                    short[] value = Short2ShortArrayMap.this.value;
                    int max = Short2ShortArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ShortSpliterator spliterator() {
            return new ValuesSpliterator(0, Short2ShortArrayMap.this.size);
        }

        @Override
        public void forEach(ShortConsumer action) {
            short[] value = Short2ShortArrayMap.this.value;
            int max = Short2ShortArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Short2ShortArrayMap.this.size;
        }

        @Override
        public void clear() {
            Short2ShortArrayMap.this.clear();
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
                return Short2ShortArrayMap.this.value[location];
            }

            @Override
            protected final ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(ShortConsumer action) {
                short[] value = Short2ShortArrayMap.this.value;
                int max = Short2ShortArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Short2ShortMap.Entry,
    Map.Entry<Short, Short>,
    ShortShortPair {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public short getShortKey() {
            return Short2ShortArrayMap.this.key[this.index];
        }

        @Override
        public short leftShort() {
            return Short2ShortArrayMap.this.key[this.index];
        }

        @Override
        public short getShortValue() {
            return Short2ShortArrayMap.this.value[this.index];
        }

        @Override
        public short rightShort() {
            return Short2ShortArrayMap.this.value[this.index];
        }

        @Override
        public short setValue(short v) {
            short oldValue = Short2ShortArrayMap.this.value[this.index];
            Short2ShortArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        @Override
        public ShortShortPair right(short v) {
            Short2ShortArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Short getKey() {
            return Short2ShortArrayMap.this.key[this.index];
        }

        @Override
        @Deprecated
        public Short getValue() {
            return Short2ShortArrayMap.this.value[this.index];
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
            return Short2ShortArrayMap.this.key[this.index] == (Short)e.getKey() && Short2ShortArrayMap.this.value[this.index] == (Short)e.getValue();
        }

        @Override
        public int hashCode() {
            return Short2ShortArrayMap.this.key[this.index] ^ Short2ShortArrayMap.this.value[this.index];
        }

        public String toString() {
            return Short2ShortArrayMap.this.key[this.index] + "=>" + Short2ShortArrayMap.this.value[this.index];
        }
    }
}

