/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.objects;

import it.unimi.dsi.fastutil.booleans.AbstractBooleanCollection;
import it.unimi.dsi.fastutil.booleans.BooleanArrays;
import it.unimi.dsi.fastutil.booleans.BooleanCollection;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.booleans.BooleanIterator;
import it.unimi.dsi.fastutil.booleans.BooleanSpliterator;
import it.unimi.dsi.fastutil.booleans.BooleanSpliterators;
import it.unimi.dsi.fastutil.objects.AbstractObject2BooleanMap;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
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

public class Object2BooleanArrayMap<K>
extends AbstractObject2BooleanMap<K>
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    protected transient Object[] key;
    protected transient boolean[] value;
    protected int size;
    protected transient Object2BooleanMap.FastEntrySet<K> entries;
    protected transient ObjectSet<K> keys;
    protected transient BooleanCollection values;

    public Object2BooleanArrayMap(Object[] key, boolean[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Object2BooleanArrayMap() {
        this.key = ObjectArrays.EMPTY_ARRAY;
        this.value = BooleanArrays.EMPTY_ARRAY;
    }

    public Object2BooleanArrayMap(int capacity) {
        this.key = new Object[capacity];
        this.value = new boolean[capacity];
    }

    public Object2BooleanArrayMap(Object2BooleanMap<K> m) {
        this(m.size());
        int i = 0;
        for (Object2BooleanMap.Entry entry : m.object2BooleanEntrySet()) {
            this.key[i] = entry.getKey();
            this.value[i] = entry.getBooleanValue();
            ++i;
        }
        this.size = i;
    }

    public Object2BooleanArrayMap(Map<? extends K, ? extends Boolean> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<K, Boolean> e : m.entrySet()) {
            this.key[i] = e.getKey();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Object2BooleanArrayMap(Object[] key, boolean[] value, int size) {
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

    public Object2BooleanMap.FastEntrySet<K> object2BooleanEntrySet() {
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
    public boolean getBoolean(Object k) {
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
    public boolean containsValue(boolean v) {
        boolean[] value = this.value;
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
    public boolean put(K k, boolean v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            boolean oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return oldValue;
        }
        if (this.size == this.key.length) {
            Object[] newKey = new Object[this.size == 0 ? 2 : this.size * 2];
            boolean[] newValue = new boolean[this.size == 0 ? 2 : this.size * 2];
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
    public boolean removeBoolean(Object k) {
        int oldPos = this.findKey(k);
        if (oldPos == -1) {
            return this.defRetValue;
        }
        boolean oldValue = this.value[oldPos];
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
    public BooleanCollection values() {
        if (this.values == null) {
            this.values = new ValuesCollection();
        }
        return this.values;
    }

    public Object2BooleanArrayMap<K> clone() {
        Object2BooleanArrayMap c;
        try {
            c = (Object2BooleanArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (Object[])this.key.clone();
        c.value = (boolean[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        Object[] key = this.key;
        boolean[] value = this.value;
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeObject(key[i]);
            s.writeBoolean(value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new Object[this.size];
        Object[] key = this.key;
        this.value = new boolean[this.size];
        boolean[] value = this.value;
        for (int i = 0; i < this.size; ++i) {
            key[i] = s.readObject();
            value[i] = s.readBoolean();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Object2BooleanMap.Entry<K>>
    implements Object2BooleanMap.FastEntrySet<K> {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Object2BooleanMap.Entry<K>> iterator() {
            return new ObjectIterator<Object2BooleanMap.Entry<K>>(){
                private MapEntry entry;
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Object2BooleanArrayMap.this.size;
                }

                @Override
                public Object2BooleanMap.Entry<K> next() {
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
                    int tail = Object2BooleanArrayMap.this.size-- - this.next--;
                    System.arraycopy(Object2BooleanArrayMap.this.key, this.next + 1, Object2BooleanArrayMap.this.key, this.next, tail);
                    System.arraycopy(Object2BooleanArrayMap.this.value, this.next + 1, Object2BooleanArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Object2BooleanArrayMap.this.key[Object2BooleanArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Object2BooleanArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Object2BooleanMap.Entry<K>> action) {
                    int max = Object2BooleanArrayMap.this.size;
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
        public ObjectIterator<Object2BooleanMap.Entry<K>> fastIterator() {
            return new ObjectIterator<Object2BooleanMap.Entry<K>>(){
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
                    return this.next < Object2BooleanArrayMap.this.size;
                }

                @Override
                public Object2BooleanMap.Entry<K> next() {
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
                    int tail = Object2BooleanArrayMap.this.size-- - this.next--;
                    System.arraycopy(Object2BooleanArrayMap.this.key, this.next + 1, Object2BooleanArrayMap.this.key, this.next, tail);
                    System.arraycopy(Object2BooleanArrayMap.this.value, this.next + 1, Object2BooleanArrayMap.this.value, this.next, tail);
                    this.entry.index = -1;
                    Object2BooleanArrayMap.this.key[Object2BooleanArrayMap.this.size] = null;
                }

                @Override
                public int skip(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                    }
                    n = Math.min(n, Object2BooleanArrayMap.this.size - this.next);
                    this.next += n;
                    if (n != 0) {
                        this.curr = this.next - 1;
                    }
                    return n;
                }

                @Override
                public void forEachRemaining(Consumer<? super Object2BooleanMap.Entry<K>> action) {
                    int max = Object2BooleanArrayMap.this.size;
                    while (this.next < max) {
                        ++this.next;
                        this.entry.index = this.curr = this.curr;
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Object2BooleanMap.Entry<K>> spliterator() {
            return new EntrySetSpliterator(0, Object2BooleanArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Object2BooleanMap.Entry<K>> action) {
            int max = Object2BooleanArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new MapEntry(i));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Object2BooleanMap.Entry<K>> action) {
            MapEntry entry = new MapEntry();
            int i = 0;
            int max = Object2BooleanArrayMap.this.size;
            while (i < max) {
                entry.index = i++;
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Object2BooleanArrayMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getValue() == null || !(e.getValue() instanceof Boolean)) {
                return false;
            }
            Object k = e.getKey();
            return Object2BooleanArrayMap.this.containsKey(k) && Object2BooleanArrayMap.this.getBoolean(k) == ((Boolean)e.getValue()).booleanValue();
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getValue() == null || !(e.getValue() instanceof Boolean)) {
                return false;
            }
            Object k = e.getKey();
            boolean v = (Boolean)e.getValue();
            int oldPos = Object2BooleanArrayMap.this.findKey(k);
            if (oldPos == -1 || v != Object2BooleanArrayMap.this.value[oldPos]) {
                return false;
            }
            int tail = Object2BooleanArrayMap.this.size - oldPos - 1;
            System.arraycopy(Object2BooleanArrayMap.this.key, oldPos + 1, Object2BooleanArrayMap.this.key, oldPos, tail);
            System.arraycopy(Object2BooleanArrayMap.this.value, oldPos + 1, Object2BooleanArrayMap.this.value, oldPos, tail);
            --Object2BooleanArrayMap.this.size;
            Object2BooleanArrayMap.this.key[Object2BooleanArrayMap.this.size] = null;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Object2BooleanMap.Entry<K>>
        implements ObjectSpliterator<Object2BooleanMap.Entry<K>> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Object2BooleanMap.Entry<K> get(int location) {
                return new MapEntry(location);
            }

            protected final it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap$EntrySet.EntrySetSpliterator makeForSplit(int pos, int maxPos) {
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
            return Object2BooleanArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(Object k) {
            int oldPos = Object2BooleanArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Object2BooleanArrayMap.this.size - oldPos - 1;
            System.arraycopy(Object2BooleanArrayMap.this.key, oldPos + 1, Object2BooleanArrayMap.this.key, oldPos, tail);
            System.arraycopy(Object2BooleanArrayMap.this.value, oldPos + 1, Object2BooleanArrayMap.this.value, oldPos, tail);
            --Object2BooleanArrayMap.this.size;
            Object2BooleanArrayMap.this.key[Object2BooleanArrayMap.this.size] = null;
            return true;
        }

        @Override
        public ObjectIterator<K> iterator() {
            return new ObjectIterator<K>(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Object2BooleanArrayMap.this.size;
                }

                @Override
                public K next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Object2BooleanArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Object2BooleanArrayMap.this.size - this.pos;
                    System.arraycopy(Object2BooleanArrayMap.this.key, this.pos, Object2BooleanArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Object2BooleanArrayMap.this.value, this.pos, Object2BooleanArrayMap.this.value, this.pos - 1, tail);
                    --Object2BooleanArrayMap.this.size;
                    --this.pos;
                    Object2BooleanArrayMap.this.key[Object2BooleanArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(Consumer<? super K> action) {
                    Object[] key = Object2BooleanArrayMap.this.key;
                    int max = Object2BooleanArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<K> spliterator() {
            return new KeySetSpliterator(0, Object2BooleanArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super K> action) {
            Object[] key = Object2BooleanArrayMap.this.key;
            int max = Object2BooleanArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(key[i]);
            }
        }

        @Override
        public int size() {
            return Object2BooleanArrayMap.this.size;
        }

        @Override
        public void clear() {
            Object2BooleanArrayMap.this.clear();
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
                return Object2BooleanArrayMap.this.key[location];
            }

            protected final it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap$KeySet.KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(Consumer<? super K> action) {
                Object[] key = Object2BooleanArrayMap.this.key;
                int max = Object2BooleanArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(key[this.pos++]);
                }
            }
        }
    }

    private final class ValuesCollection
    extends AbstractBooleanCollection {
        private ValuesCollection() {
        }

        @Override
        public boolean contains(boolean v) {
            return Object2BooleanArrayMap.this.containsValue(v);
        }

        @Override
        public BooleanIterator iterator() {
            return new BooleanIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Object2BooleanArrayMap.this.size;
                }

                @Override
                public boolean nextBoolean() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Object2BooleanArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Object2BooleanArrayMap.this.size - this.pos;
                    System.arraycopy(Object2BooleanArrayMap.this.key, this.pos, Object2BooleanArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Object2BooleanArrayMap.this.value, this.pos, Object2BooleanArrayMap.this.value, this.pos - 1, tail);
                    --Object2BooleanArrayMap.this.size;
                    --this.pos;
                    Object2BooleanArrayMap.this.key[Object2BooleanArrayMap.this.size] = null;
                }

                @Override
                public void forEachRemaining(BooleanConsumer action) {
                    boolean[] value = Object2BooleanArrayMap.this.value;
                    int max = Object2BooleanArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public BooleanSpliterator spliterator() {
            return new ValuesSpliterator(0, Object2BooleanArrayMap.this.size);
        }

        @Override
        public void forEach(BooleanConsumer action) {
            boolean[] value = Object2BooleanArrayMap.this.value;
            int max = Object2BooleanArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(value[i]);
            }
        }

        @Override
        public int size() {
            return Object2BooleanArrayMap.this.size;
        }

        @Override
        public void clear() {
            Object2BooleanArrayMap.this.clear();
        }

        final class ValuesSpliterator
        extends BooleanSpliterators.EarlyBindingSizeIndexBasedSpliterator
        implements BooleanSpliterator {
            ValuesSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16720;
            }

            @Override
            protected final boolean get(int location) {
                return Object2BooleanArrayMap.this.value[location];
            }

            protected final it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap$ValuesCollection.ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(BooleanConsumer action) {
                boolean[] value = Object2BooleanArrayMap.this.value;
                int max = Object2BooleanArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(value[this.pos++]);
                }
            }
        }
    }

    private final class MapEntry
    implements Object2BooleanMap.Entry<K>,
    Map.Entry<K, Boolean>,
    ObjectBooleanPair<K> {
        int index;

        MapEntry() {
        }

        MapEntry(int index) {
            this.index = index;
        }

        @Override
        public K getKey() {
            return Object2BooleanArrayMap.this.key[this.index];
        }

        @Override
        public K left() {
            return Object2BooleanArrayMap.this.key[this.index];
        }

        @Override
        public boolean getBooleanValue() {
            return Object2BooleanArrayMap.this.value[this.index];
        }

        @Override
        public boolean rightBoolean() {
            return Object2BooleanArrayMap.this.value[this.index];
        }

        @Override
        public boolean setValue(boolean v) {
            boolean oldValue = Object2BooleanArrayMap.this.value[this.index];
            Object2BooleanArrayMap.this.value[this.index] = v;
            return oldValue;
        }

        @Override
        public ObjectBooleanPair<K> right(boolean v) {
            Object2BooleanArrayMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Boolean getValue() {
            return Object2BooleanArrayMap.this.value[this.index];
        }

        @Override
        @Deprecated
        public Boolean setValue(Boolean v) {
            return this.setValue((boolean)v);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            return Objects.equals(Object2BooleanArrayMap.this.key[this.index], e.getKey()) && Object2BooleanArrayMap.this.value[this.index] == (Boolean)e.getValue();
        }

        @Override
        public int hashCode() {
            return (Object2BooleanArrayMap.this.key[this.index] == null ? 0 : Object2BooleanArrayMap.this.key[this.index].hashCode()) ^ (Object2BooleanArrayMap.this.value[this.index] ? 1231 : 1237);
        }

        public String toString() {
            return Object2BooleanArrayMap.this.key[this.index] + "=>" + Object2BooleanArrayMap.this.value[this.index];
        }
    }
}

