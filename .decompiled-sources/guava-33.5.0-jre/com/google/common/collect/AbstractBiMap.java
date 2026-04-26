/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ForwardingMapEntry;
import com.google.common.collect.ForwardingSet;
import com.google.common.collect.Maps;
import com.google.common.collect.NullnessCasts;
import com.google.common.collect.ParametricNullness;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.google.j2objc.annotations.RetainedWith;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import org.jspecify.annotations.Nullable;

@GwtCompatible
abstract class AbstractBiMap<K, V>
extends ForwardingMap<K, V>
implements BiMap<K, V>,
Serializable {
    private transient Map<K, V> delegate;
    @RetainedWith
    transient AbstractBiMap<V, K> inverse;
    @LazyInit
    private transient @Nullable Set<K> keySet;
    @LazyInit
    private transient @Nullable Set<V> valueSet;
    @LazyInit
    private transient @Nullable Set<Map.Entry<K, V>> entrySet;
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = 0L;

    AbstractBiMap(Map<K, V> forward, Map<V, K> backward) {
        this.setDelegates(forward, backward);
    }

    private AbstractBiMap(Map<K, V> backward, AbstractBiMap<V, K> forward) {
        this.delegate = backward;
        this.inverse = forward;
    }

    @Override
    protected Map<K, V> delegate() {
        return this.delegate;
    }

    @CanIgnoreReturnValue
    @ParametricNullness
    K checkKey(@ParametricNullness K key) {
        return key;
    }

    @CanIgnoreReturnValue
    @ParametricNullness
    V checkValue(@ParametricNullness V value) {
        return value;
    }

    void setDelegates(Map<K, V> forward, Map<V, K> backward) {
        Preconditions.checkState(this.delegate == null);
        Preconditions.checkState(this.inverse == null);
        Preconditions.checkArgument(forward.isEmpty());
        Preconditions.checkArgument(backward.isEmpty());
        Preconditions.checkArgument(forward != backward);
        this.delegate = forward;
        this.inverse = this.makeInverse(backward);
    }

    AbstractBiMap<V, K> makeInverse(Map<V, K> backward) {
        return new Inverse<V, K>(backward, this);
    }

    void setInverse(AbstractBiMap<V, K> inverse) {
        this.inverse = inverse;
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        return this.inverse.containsKey(value);
    }

    @Override
    @CanIgnoreReturnValue
    public @Nullable V put(@ParametricNullness K key, @ParametricNullness V value) {
        return this.putInBothMaps(key, value, false);
    }

    @Override
    @CanIgnoreReturnValue
    public @Nullable V forcePut(@ParametricNullness K key, @ParametricNullness V value) {
        return this.putInBothMaps(key, value, true);
    }

    private @Nullable V putInBothMaps(@ParametricNullness K key, @ParametricNullness V value, boolean force) {
        this.checkKey(key);
        this.checkValue(value);
        boolean containedKey = this.containsKey(key);
        if (containedKey && Objects.equals(value, this.get(key))) {
            return value;
        }
        if (force) {
            this.inverse().remove(value);
        } else {
            Preconditions.checkArgument(!this.containsValue(value), "value already present: %s", value);
        }
        V oldValue = this.delegate.put(key, value);
        this.updateInverseMap(key, containedKey, oldValue, value);
        return oldValue;
    }

    private void updateInverseMap(@ParametricNullness K key, boolean containedKey, @Nullable V oldValue, @ParametricNullness V newValue) {
        if (containedKey) {
            this.removeFromInverseMap(NullnessCasts.uncheckedCastNullableTToT(oldValue));
        }
        this.inverse.delegate.put(newValue, key);
    }

    @Override
    @CanIgnoreReturnValue
    public @Nullable V remove(@Nullable Object key) {
        return this.containsKey(key) ? (V)this.removeFromBothMaps(key) : null;
    }

    @CanIgnoreReturnValue
    @ParametricNullness
    private V removeFromBothMaps(@Nullable Object key) {
        V oldValue = NullnessCasts.uncheckedCastNullableTToT(this.delegate.remove(key));
        this.removeFromInverseMap(oldValue);
        return oldValue;
    }

    private void removeFromInverseMap(@ParametricNullness V oldValue) {
        this.inverse.delegate.remove(oldValue);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        this.delegate.replaceAll(function);
        this.inverse.delegate.clear();
        Map.Entry<K, V> broken = null;
        Iterator<Map.Entry<K, V>> itr = this.delegate.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<K, V> entry = itr.next();
            K k = entry.getKey();
            V v = entry.getValue();
            V conflict = this.inverse.delegate.putIfAbsent(v, k);
            if (conflict == null) continue;
            broken = entry;
            itr.remove();
        }
        if (broken != null) {
            throw new IllegalArgumentException("value already present: " + broken.getValue());
        }
    }

    @Override
    public void clear() {
        this.delegate.clear();
        this.inverse.delegate.clear();
    }

    @Override
    public BiMap<V, K> inverse() {
        return this.inverse;
    }

    @Override
    public Set<K> keySet() {
        KeySet result = this.keySet;
        return result == null ? (this.keySet = new KeySet()) : result;
    }

    @Override
    public Set<V> values() {
        ValueSet result = this.valueSet;
        return result == null ? (this.valueSet = new ValueSet()) : result;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        EntrySet result = this.entrySet;
        return result == null ? (this.entrySet = new EntrySet()) : result;
    }

    Iterator<Map.Entry<K, V>> entrySetIterator() {
        final Iterator<Map.Entry<K, V>> iterator = this.delegate.entrySet().iterator();
        return new Iterator<Map.Entry<K, V>>(){
            @Nullable Map.Entry<K, V> entry;
            final /* synthetic */ AbstractBiMap this$0;
            {
                this.this$0 = this$0;
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Map.Entry<K, V> next() {
                this.entry = (Map.Entry)iterator.next();
                return this.this$0.new BiMapEntry(this.entry);
            }

            @Override
            public void remove() {
                if (this.entry == null) {
                    throw new IllegalStateException("no calls to next() since the last call to remove()");
                }
                Object value = this.entry.getValue();
                iterator.remove();
                this.this$0.removeFromInverseMap(value);
                this.entry = null;
            }
        };
    }

    private static final class Inverse<K, V>
    extends AbstractBiMap<K, V> {
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        Inverse(Map<K, V> backward, AbstractBiMap<V, K> forward) {
            super(backward, forward);
        }

        @Override
        @ParametricNullness
        K checkKey(@ParametricNullness K key) {
            return this.inverse.checkValue(key);
        }

        @Override
        @ParametricNullness
        V checkValue(@ParametricNullness V value) {
            return this.inverse.checkKey(value);
        }

        @GwtIncompatible
        @J2ktIncompatible
        private void writeObject(ObjectOutputStream stream) throws IOException {
            stream.defaultWriteObject();
            stream.writeObject(this.inverse());
        }

        @GwtIncompatible
        @J2ktIncompatible
        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            this.setInverse((AbstractBiMap)Objects.requireNonNull(stream.readObject()));
        }

        @GwtIncompatible
        @J2ktIncompatible
        Object readResolve() {
            return this.inverse().inverse();
        }
    }

    private final class KeySet
    extends ForwardingSet<K> {
        private KeySet() {
        }

        @Override
        protected Set<K> delegate() {
            return AbstractBiMap.this.delegate.keySet();
        }

        @Override
        public void clear() {
            AbstractBiMap.this.clear();
        }

        @Override
        public boolean remove(@Nullable Object key) {
            if (!this.contains(key)) {
                return false;
            }
            AbstractBiMap.this.removeFromBothMaps(key);
            return true;
        }

        @Override
        public boolean removeAll(Collection<?> keysToRemove) {
            return this.standardRemoveAll(keysToRemove);
        }

        @Override
        public boolean retainAll(Collection<?> keysToRetain) {
            return this.standardRetainAll(keysToRetain);
        }

        @Override
        public Iterator<K> iterator() {
            return Maps.keyIterator(AbstractBiMap.this.entrySet().iterator());
        }
    }

    private final class ValueSet
    extends ForwardingSet<V> {
        final Set<V> valuesDelegate;

        private ValueSet() {
            this.valuesDelegate = AbstractBiMap.this.inverse.keySet();
        }

        @Override
        protected Set<V> delegate() {
            return this.valuesDelegate;
        }

        @Override
        public Iterator<V> iterator() {
            return Maps.valueIterator(AbstractBiMap.this.entrySet().iterator());
        }

        @Override
        public @Nullable Object[] toArray() {
            return this.standardToArray();
        }

        @Override
        public <T> T[] toArray(T[] array) {
            return this.standardToArray(array);
        }

        @Override
        public String toString() {
            return this.standardToString();
        }
    }

    private final class EntrySet
    extends ForwardingSet<Map.Entry<K, V>> {
        final Set<Map.Entry<K, V>> esDelegate;

        private EntrySet() {
            this.esDelegate = AbstractBiMap.this.delegate.entrySet();
        }

        @Override
        protected Set<Map.Entry<K, V>> delegate() {
            return this.esDelegate;
        }

        @Override
        public void clear() {
            AbstractBiMap.this.clear();
        }

        @Override
        public boolean remove(@Nullable Object object) {
            if (!this.esDelegate.contains(object) || !(object instanceof Map.Entry)) {
                return false;
            }
            Map.Entry entry = (Map.Entry)object;
            AbstractBiMap.this.inverse.delegate.remove(entry.getValue());
            this.esDelegate.remove(entry);
            return true;
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return AbstractBiMap.this.entrySetIterator();
        }

        @Override
        public @Nullable Object[] toArray() {
            return this.standardToArray();
        }

        @Override
        public <T> T[] toArray(T[] array) {
            return this.standardToArray(array);
        }

        @Override
        public boolean contains(@Nullable Object o) {
            return Maps.containsEntryImpl(this.delegate(), o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return this.standardContainsAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return this.standardRemoveAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return this.standardRetainAll(c);
        }
    }

    private final class BiMapEntry
    extends ForwardingMapEntry<K, V> {
        private final Map.Entry<K, V> delegate;

        BiMapEntry(Map.Entry<K, V> delegate) {
            this.delegate = delegate;
        }

        @Override
        protected Map.Entry<K, V> delegate() {
            return this.delegate;
        }

        @Override
        public V setValue(V value) {
            AbstractBiMap.this.checkValue(value);
            Preconditions.checkState(AbstractBiMap.this.entrySet().contains(this), "entry no longer in map");
            if (Objects.equals(value, this.getValue())) {
                return value;
            }
            Preconditions.checkArgument(!AbstractBiMap.this.containsValue(value), "value already present: %s", value);
            Object oldValue = this.delegate.setValue(value);
            Preconditions.checkState(Objects.equals(value, AbstractBiMap.this.get(this.getKey())), "entry no longer in map");
            AbstractBiMap.this.updateInverseMap(this.getKey(), true, oldValue, value);
            return oldValue;
        }
    }
}

