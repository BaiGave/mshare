/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractMultimap;
import com.google.common.collect.Iterators;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.collect.ParametricNullness;
import com.google.common.collect.Platform;
import com.google.common.collect.Sets;
import com.google.common.collect.TransformedListIterator;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.j2objc.annotations.Weak;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public class LinkedListMultimap<K, V>
extends AbstractMultimap<K, V>
implements ListMultimap<K, V>,
Serializable {
    private transient @Nullable Node<K, V> head;
    private transient @Nullable Node<K, V> tail;
    private transient Map<K, KeyList<K, V>> keyToKeyList;
    private transient int size;
    private transient int modCount;
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = 0L;

    public static <K, V> LinkedListMultimap<K, V> create() {
        return new LinkedListMultimap<K, V>();
    }

    public static <K, V> LinkedListMultimap<K, V> create(int expectedKeys) {
        return new LinkedListMultimap<K, V>(expectedKeys);
    }

    public static <K, V> LinkedListMultimap<K, V> create(Multimap<? extends K, ? extends V> multimap) {
        return new LinkedListMultimap<K, V>(multimap);
    }

    LinkedListMultimap() {
        this(12);
    }

    private LinkedListMultimap(int expectedKeys) {
        this.keyToKeyList = Platform.newHashMapWithExpectedSize(expectedKeys);
    }

    private LinkedListMultimap(Multimap<? extends K, ? extends V> multimap) {
        this(multimap.keySet().size());
        this.putAll((Multimap)multimap);
    }

    @CanIgnoreReturnValue
    private Node<K, V> addNode(@ParametricNullness K key, @ParametricNullness V value, @Nullable Node<K, V> nextSibling) {
        Node<K, V> node = new Node<K, V>(key, value);
        if (this.head == null) {
            this.tail = node;
            this.head = this.tail;
            this.keyToKeyList.put(key, new KeyList<K, V>(node));
            ++this.modCount;
        } else if (nextSibling == null) {
            Objects.requireNonNull(this.tail).next = node;
            node.previous = this.tail;
            this.tail = node;
            KeyList<K, V> keyList = this.keyToKeyList.get(key);
            if (keyList == null) {
                keyList = new KeyList<K, V>(node);
                this.keyToKeyList.put(key, keyList);
                ++this.modCount;
            } else {
                ++keyList.count;
                Node keyTail = keyList.tail;
                keyTail.nextSibling = node;
                node.previousSibling = keyTail;
                keyList.tail = node;
            }
        } else {
            KeyList<K, V> keyList = Objects.requireNonNull(this.keyToKeyList.get(key));
            ++keyList.count;
            node.previous = nextSibling.previous;
            node.previousSibling = nextSibling.previousSibling;
            node.next = nextSibling;
            node.nextSibling = nextSibling;
            if (nextSibling.previousSibling == null) {
                keyList.head = node;
            } else {
                nextSibling.previousSibling.nextSibling = node;
            }
            if (nextSibling.previous == null) {
                this.head = node;
            } else {
                nextSibling.previous.next = node;
            }
            nextSibling.previous = node;
            nextSibling.previousSibling = node;
        }
        ++this.size;
        return node;
    }

    private void removeNode(Node<K, V> node) {
        if (node.previous != null) {
            node.previous.next = node.next;
        } else {
            this.head = node.next;
        }
        if (node.next != null) {
            node.next.previous = node.previous;
        } else {
            this.tail = node.previous;
        }
        if (node.previousSibling == null && node.nextSibling == null) {
            KeyList<K, V> keyList = Objects.requireNonNull(this.keyToKeyList.remove(node.getKey()));
            keyList.count = 0;
            ++this.modCount;
        } else {
            KeyList<K, V> keyList = Objects.requireNonNull(this.keyToKeyList.get(node.getKey()));
            --keyList.count;
            if (node.previousSibling == null) {
                keyList.head = Objects.requireNonNull(node.nextSibling);
            } else {
                node.previousSibling.nextSibling = node.nextSibling;
            }
            if (node.nextSibling == null) {
                keyList.tail = Objects.requireNonNull(node.previousSibling);
            } else {
                node.nextSibling.previousSibling = node.previousSibling;
            }
        }
        --this.size;
    }

    private void removeAllNodes(@ParametricNullness K key) {
        Iterators.clear(new ValueForKeyIterator(key));
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.head == null;
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        return this.keyToKeyList.containsKey(key);
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        return this.values().contains(value);
    }

    @Override
    @CanIgnoreReturnValue
    public boolean put(@ParametricNullness K key, @ParametricNullness V value) {
        this.addNode(key, value, null);
        return true;
    }

    @Override
    @CanIgnoreReturnValue
    public List<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values) {
        List<V> oldValues = this.getCopy(key);
        ValueForKeyIterator keyValues = new ValueForKeyIterator(key);
        Iterator<V> newValues = values.iterator();
        while (keyValues.hasNext() && newValues.hasNext()) {
            keyValues.next();
            keyValues.set(newValues.next());
        }
        while (keyValues.hasNext()) {
            keyValues.next();
            keyValues.remove();
        }
        while (newValues.hasNext()) {
            keyValues.add(newValues.next());
        }
        return oldValues;
    }

    private List<V> getCopy(@ParametricNullness K key) {
        return Collections.unmodifiableList(Lists.newArrayList(new ValueForKeyIterator(key)));
    }

    @Override
    @CanIgnoreReturnValue
    public List<V> removeAll(@Nullable Object key) {
        Object castKey = key;
        List<V> oldValues = this.getCopy(castKey);
        this.removeAllNodes(castKey);
        return oldValues;
    }

    @Override
    public void clear() {
        this.head = null;
        this.tail = null;
        this.keyToKeyList.clear();
        this.size = 0;
        ++this.modCount;
    }

    @Override
    public List<V> get(final @ParametricNullness K key) {
        return new AbstractSequentialList<V>(this){
            final /* synthetic */ LinkedListMultimap this$0;
            {
                this.this$0 = this$0;
            }

            @Override
            public int size() {
                KeyList keyList = (KeyList)this.this$0.keyToKeyList.get(key);
                return keyList == null ? 0 : keyList.count;
            }

            @Override
            public ListIterator<V> listIterator(int index) {
                return this.this$0.new ValueForKeyIterator(key, index);
            }
        };
    }

    @Override
    Set<K> createKeySet() {
        final class KeySetImpl
        extends Sets.ImprovedAbstractSet<K> {
            KeySetImpl() {
            }

            @Override
            public int size() {
                return LinkedListMultimap.this.keyToKeyList.size();
            }

            @Override
            public Iterator<K> iterator() {
                return new DistinctKeyIterator();
            }

            @Override
            public boolean contains(@Nullable Object key) {
                return LinkedListMultimap.this.containsKey(key);
            }

            @Override
            public boolean remove(@Nullable Object o) {
                return !LinkedListMultimap.this.removeAll(o).isEmpty();
            }
        }
        return new KeySetImpl();
    }

    @Override
    Multiset<K> createKeys() {
        return new Multimaps.Keys(this);
    }

    @Override
    public List<V> values() {
        return (List)super.values();
    }

    @Override
    List<V> createValues() {
        final class ValuesImpl
        extends AbstractSequentialList<V> {
            ValuesImpl() {
            }

            @Override
            public int size() {
                return LinkedListMultimap.this.size;
            }

            @Override
            public ListIterator<V> listIterator(int index) {
                final NodeIterator nodeItr = new NodeIterator(index);
                return new TransformedListIterator<Map.Entry<K, V>, V>(this, nodeItr){
                    final /* synthetic */ ValuesImpl this$1;
                    {
                        this.this$1 = this$1;
                        super(backingIterator);
                    }

                    @Override
                    @ParametricNullness
                    V transform(Map.Entry<K, V> entry) {
                        return entry.getValue();
                    }

                    @Override
                    public void set(@ParametricNullness V value) {
                        nodeItr.setValue(value);
                    }
                };
            }
        }
        return new ValuesImpl();
    }

    @Override
    public List<Map.Entry<K, V>> entries() {
        return (List)super.entries();
    }

    @Override
    List<Map.Entry<K, V>> createEntries() {
        final class EntriesImpl
        extends AbstractSequentialList<Map.Entry<K, V>> {
            EntriesImpl() {
            }

            @Override
            public int size() {
                return LinkedListMultimap.this.size;
            }

            @Override
            public ListIterator<Map.Entry<K, V>> listIterator(int index) {
                return new NodeIterator(index);
            }

            @Override
            public void forEach(Consumer<? super Map.Entry<K, V>> action) {
                Preconditions.checkNotNull(action);
                Node node = LinkedListMultimap.this.head;
                while (node != null) {
                    action.accept(node);
                    node = node.next;
                }
            }
        }
        return new EntriesImpl();
    }

    @Override
    Iterator<Map.Entry<K, V>> entryIterator() {
        throw new AssertionError((Object)"should never be called");
    }

    @Override
    Map<K, Collection<V>> createAsMap() {
        return new Multimaps.AsMap(this);
    }

    @GwtIncompatible
    @J2ktIncompatible
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(this.size());
        for (Map.Entry entry : this.entries()) {
            stream.writeObject(entry.getKey());
            stream.writeObject(entry.getValue());
        }
    }

    @GwtIncompatible
    @J2ktIncompatible
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.keyToKeyList = new LinkedHashMap<K, KeyList<K, V>>();
        int size = stream.readInt();
        for (int i = 0; i < size; ++i) {
            Object key = stream.readObject();
            Object value = stream.readObject();
            this.put(key, value);
        }
    }

    static final class Node<K, V>
    extends AbstractMap.SimpleEntry<K, V> {
        @Nullable Node<K, V> next;
        @Weak
        @Nullable Node<K, V> previous;
        @Nullable Node<K, V> nextSibling;
        @Weak
        @Nullable Node<K, V> previousSibling;

        Node(@ParametricNullness K key, @ParametricNullness V value) {
            super(key, value);
        }
    }

    private static final class KeyList<K, V> {
        Node<K, V> head;
        Node<K, V> tail;
        int count;

        KeyList(Node<K, V> firstNode) {
            this.head = firstNode;
            this.tail = firstNode;
            firstNode.previousSibling = null;
            firstNode.nextSibling = null;
            this.count = 1;
        }
    }

    private final class ValueForKeyIterator
    implements ListIterator<V> {
        @ParametricNullness
        final K key;
        int nextIndex;
        @Nullable Node<K, V> next;
        @Nullable Node<K, V> current;
        @Nullable Node<K, V> previous;

        ValueForKeyIterator(K key) {
            this.key = key;
            KeyList keyList = (KeyList)LinkedListMultimap.this.keyToKeyList.get(key);
            this.next = keyList == null ? null : keyList.head;
        }

        ValueForKeyIterator(K key, int index) {
            KeyList keyList = (KeyList)LinkedListMultimap.this.keyToKeyList.get(key);
            int size = keyList == null ? 0 : keyList.count;
            Preconditions.checkPositionIndex(index, size);
            if (index >= size / 2) {
                this.previous = keyList == null ? null : keyList.tail;
                this.nextIndex = size;
                while (index++ < size) {
                    this.previous();
                }
            } else {
                Node node = this.next = keyList == null ? null : keyList.head;
                while (index-- > 0) {
                    this.next();
                }
            }
            this.key = key;
            this.current = null;
        }

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        @Override
        @CanIgnoreReturnValue
        @ParametricNullness
        public V next() {
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            this.current = this.next;
            this.previous = this.current;
            this.next = this.next.nextSibling;
            ++this.nextIndex;
            return this.current.getValue();
        }

        @Override
        public boolean hasPrevious() {
            return this.previous != null;
        }

        @Override
        @CanIgnoreReturnValue
        @ParametricNullness
        public V previous() {
            if (this.previous == null) {
                throw new NoSuchElementException();
            }
            this.current = this.previous;
            this.next = this.current;
            this.previous = this.previous.previousSibling;
            --this.nextIndex;
            return this.current.getValue();
        }

        @Override
        public int nextIndex() {
            return this.nextIndex;
        }

        @Override
        public int previousIndex() {
            return this.nextIndex - 1;
        }

        @Override
        public void remove() {
            Preconditions.checkState(this.current != null, "no calls to next() since the last call to remove()");
            if (this.current != this.next) {
                this.previous = this.current.previousSibling;
                --this.nextIndex;
            } else {
                this.next = this.current.nextSibling;
            }
            LinkedListMultimap.this.removeNode(this.current);
            this.current = null;
        }

        @Override
        public void set(@ParametricNullness V value) {
            Preconditions.checkState(this.current != null);
            this.current.setValue(value);
        }

        @Override
        public void add(@ParametricNullness V value) {
            this.previous = LinkedListMultimap.this.addNode(this.key, value, this.next);
            ++this.nextIndex;
            this.current = null;
        }
    }

    private final class DistinctKeyIterator
    implements Iterator<K> {
        final Set<K> seenKeys;
        @Nullable Node<K, V> next;
        @Nullable Node<K, V> current;
        int expectedModCount;

        private DistinctKeyIterator() {
            this.seenKeys = Sets.newHashSetWithExpectedSize(LinkedListMultimap.this.keySet().size());
            this.next = LinkedListMultimap.this.head;
            this.expectedModCount = LinkedListMultimap.this.modCount;
        }

        private void checkForConcurrentModification() {
            if (LinkedListMultimap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public boolean hasNext() {
            this.checkForConcurrentModification();
            return this.next != null;
        }

        @Override
        @ParametricNullness
        public K next() {
            this.checkForConcurrentModification();
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            this.current = this.next;
            this.seenKeys.add(this.current.getKey());
            do {
                this.next = this.next.next;
            } while (this.next != null && !this.seenKeys.add(this.next.getKey()));
            return this.current.getKey();
        }

        @Override
        public void remove() {
            this.checkForConcurrentModification();
            Preconditions.checkState(this.current != null, "no calls to next() since the last call to remove()");
            LinkedListMultimap.this.removeAllNodes(this.current.getKey());
            this.current = null;
            this.expectedModCount = LinkedListMultimap.this.modCount;
        }
    }

    private final class NodeIterator
    implements ListIterator<Map.Entry<K, V>> {
        int nextIndex;
        @Nullable Node<K, V> next;
        @Nullable Node<K, V> current;
        @Nullable Node<K, V> previous;
        int expectedModCount;

        NodeIterator(int index) {
            this.expectedModCount = LinkedListMultimap.this.modCount;
            int size = LinkedListMultimap.this.size();
            Preconditions.checkPositionIndex(index, size);
            if (index >= size / 2) {
                this.previous = LinkedListMultimap.this.tail;
                this.nextIndex = size;
                while (index++ < size) {
                    this.previous();
                }
            } else {
                this.next = LinkedListMultimap.this.head;
                while (index-- > 0) {
                    this.next();
                }
            }
            this.current = null;
        }

        private void checkForConcurrentModification() {
            if (LinkedListMultimap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public boolean hasNext() {
            this.checkForConcurrentModification();
            return this.next != null;
        }

        @Override
        @CanIgnoreReturnValue
        public Node<K, V> next() {
            this.checkForConcurrentModification();
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            this.current = this.next;
            this.previous = this.current;
            this.next = this.next.next;
            ++this.nextIndex;
            return this.current;
        }

        @Override
        public void remove() {
            this.checkForConcurrentModification();
            Preconditions.checkState(this.current != null, "no calls to next() since the last call to remove()");
            if (this.current != this.next) {
                this.previous = this.current.previous;
                --this.nextIndex;
            } else {
                this.next = this.current.next;
            }
            LinkedListMultimap.this.removeNode(this.current);
            this.current = null;
            this.expectedModCount = LinkedListMultimap.this.modCount;
        }

        @Override
        public boolean hasPrevious() {
            this.checkForConcurrentModification();
            return this.previous != null;
        }

        @Override
        @CanIgnoreReturnValue
        public Node<K, V> previous() {
            this.checkForConcurrentModification();
            if (this.previous == null) {
                throw new NoSuchElementException();
            }
            this.current = this.previous;
            this.next = this.current;
            this.previous = this.previous.previous;
            --this.nextIndex;
            return this.current;
        }

        @Override
        public int nextIndex() {
            return this.nextIndex;
        }

        @Override
        public int previousIndex() {
            return this.nextIndex - 1;
        }

        @Override
        public void set(Map.Entry<K, V> e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(Map.Entry<K, V> e) {
            throw new UnsupportedOperationException();
        }

        void setValue(@ParametricNullness V value) {
            Preconditions.checkState(this.current != null);
            this.current.setValue(value);
        }
    }
}

