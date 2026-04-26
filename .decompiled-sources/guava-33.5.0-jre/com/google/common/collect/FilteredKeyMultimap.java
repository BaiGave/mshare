/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.AbstractMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.FilteredMultimap;
import com.google.common.collect.FilteredMultimapValues;
import com.google.common.collect.ForwardingCollection;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.ForwardingSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.ParametricNullness;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;

@GwtCompatible
class FilteredKeyMultimap<K, V>
extends AbstractMultimap<K, V>
implements FilteredMultimap<K, V> {
    final Multimap<K, V> unfiltered;
    final Predicate<? super K> keyPredicate;

    FilteredKeyMultimap(Multimap<K, V> unfiltered, Predicate<? super K> keyPredicate) {
        this.unfiltered = Preconditions.checkNotNull(unfiltered);
        this.keyPredicate = Preconditions.checkNotNull(keyPredicate);
    }

    @Override
    public Multimap<K, V> unfiltered() {
        return this.unfiltered;
    }

    @Override
    public Predicate<? super Map.Entry<K, V>> entryPredicate() {
        return Maps.keyPredicateOnEntries(this.keyPredicate);
    }

    @Override
    public int size() {
        int size = 0;
        for (Collection collection : this.asMap().values()) {
            size += collection.size();
        }
        return size;
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        if (this.unfiltered.containsKey(key)) {
            Object k = key;
            return this.keyPredicate.apply(k);
        }
        return false;
    }

    @Override
    public Collection<V> removeAll(@Nullable Object key) {
        return this.containsKey(key) ? this.unfiltered.removeAll(key) : this.unmodifiableEmptyCollection();
    }

    Collection<V> unmodifiableEmptyCollection() {
        if (this.unfiltered instanceof SetMultimap) {
            return Collections.emptySet();
        }
        return Collections.emptyList();
    }

    @Override
    public void clear() {
        this.keySet().clear();
    }

    @Override
    Set<K> createKeySet() {
        return Sets.filter(this.unfiltered.keySet(), this.keyPredicate);
    }

    @Override
    public Collection<V> get(@ParametricNullness K key) {
        if (this.keyPredicate.apply(key)) {
            return this.unfiltered.get(key);
        }
        if (this.unfiltered instanceof SetMultimap) {
            return new AddRejectingSet(key);
        }
        return new AddRejectingList(key);
    }

    @Override
    Iterator<Map.Entry<K, V>> entryIterator() {
        throw new AssertionError((Object)"should never be called");
    }

    @Override
    Collection<Map.Entry<K, V>> createEntries() {
        return new Entries();
    }

    @Override
    Collection<V> createValues() {
        return new FilteredMultimapValues(this);
    }

    @Override
    Map<K, Collection<V>> createAsMap() {
        return Maps.filterKeys(this.unfiltered.asMap(), this.keyPredicate);
    }

    @Override
    Multiset<K> createKeys() {
        return Multisets.filter(this.unfiltered.keys(), this.keyPredicate);
    }

    private static final class AddRejectingSet<K, V>
    extends ForwardingSet<V> {
        @ParametricNullness
        final K key;

        AddRejectingSet(@ParametricNullness K key) {
            this.key = key;
        }

        @Override
        public boolean add(@ParametricNullness V element) {
            throw new IllegalArgumentException("Key does not satisfy predicate: " + this.key);
        }

        @Override
        public boolean addAll(Collection<? extends V> collection) {
            Preconditions.checkNotNull(collection);
            throw new IllegalArgumentException("Key does not satisfy predicate: " + this.key);
        }

        @Override
        protected Set<V> delegate() {
            return Collections.emptySet();
        }
    }

    private static final class AddRejectingList<K, V>
    extends ForwardingList<V> {
        @ParametricNullness
        final K key;

        AddRejectingList(@ParametricNullness K key) {
            this.key = key;
        }

        @Override
        public boolean add(@ParametricNullness V v) {
            this.add(0, v);
            return true;
        }

        @Override
        public void add(int index, @ParametricNullness V element) {
            Preconditions.checkPositionIndex(index, 0);
            throw new IllegalArgumentException("Key does not satisfy predicate: " + this.key);
        }

        @Override
        public boolean addAll(Collection<? extends V> collection) {
            this.addAll(0, collection);
            return true;
        }

        @Override
        @CanIgnoreReturnValue
        public boolean addAll(int index, Collection<? extends V> elements) {
            Preconditions.checkNotNull(elements);
            Preconditions.checkPositionIndex(index, 0);
            throw new IllegalArgumentException("Key does not satisfy predicate: " + this.key);
        }

        @Override
        protected List<V> delegate() {
            return Collections.emptyList();
        }
    }

    class Entries
    extends ForwardingCollection<Map.Entry<K, V>> {
        Entries() {
        }

        @Override
        protected Collection<Map.Entry<K, V>> delegate() {
            return Collections2.filter(FilteredKeyMultimap.this.unfiltered.entries(), FilteredKeyMultimap.this.entryPredicate());
        }

        @Override
        public boolean remove(@Nullable Object o) {
            Map.Entry entry;
            if (o instanceof Map.Entry && FilteredKeyMultimap.this.unfiltered.containsKey((entry = (Map.Entry)o).getKey()) && FilteredKeyMultimap.this.keyPredicate.apply(entry.getKey())) {
                return FilteredKeyMultimap.this.unfiltered.remove(entry.getKey(), entry.getValue());
            }
            return false;
        }
    }
}

