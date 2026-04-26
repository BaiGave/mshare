/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.collect.AbstractMapBasedMultimap;
import com.google.common.collect.AbstractSetMultimap;
import com.google.common.collect.ParametricNullness;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedSetMultimap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableSet;
import java.util.SortedSet;
import org.jspecify.annotations.Nullable;

@GwtCompatible
abstract class AbstractSortedSetMultimap<K, V>
extends AbstractSetMultimap<K, V>
implements SortedSetMultimap<K, V> {
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = 430848587173315748L;

    protected AbstractSortedSetMultimap(Map<K, Collection<V>> map) {
        super(map);
    }

    @Override
    abstract SortedSet<V> createCollection();

    @Override
    SortedSet<V> createUnmodifiableEmptyCollection() {
        return this.unmodifiableCollectionSubclass((Collection)this.createCollection());
    }

    @Override
    <E> SortedSet<E> unmodifiableCollectionSubclass(Collection<E> collection) {
        if (collection instanceof NavigableSet) {
            return Sets.unmodifiableNavigableSet((NavigableSet)collection);
        }
        return Collections.unmodifiableSortedSet((SortedSet)collection);
    }

    @Override
    Collection<V> wrapCollection(@ParametricNullness K key, Collection<V> collection) {
        if (collection instanceof NavigableSet) {
            return (AbstractMapBasedMultimap)this.new AbstractMapBasedMultimap.WrappedNavigableSet(key, (NavigableSet)collection, null);
        }
        return (AbstractMapBasedMultimap)this.new AbstractMapBasedMultimap.WrappedSortedSet(key, (SortedSet)collection, null);
    }

    @Override
    public SortedSet<V> get(@ParametricNullness K key) {
        return (SortedSet)super.get((Object)key);
    }

    @Override
    @CanIgnoreReturnValue
    public SortedSet<V> removeAll(@Nullable Object key) {
        return (SortedSet)super.removeAll(key);
    }

    @Override
    @CanIgnoreReturnValue
    public SortedSet<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values) {
        return (SortedSet)super.replaceValues((Object)key, (Iterable)values);
    }

    @Override
    public Map<K, Collection<V>> asMap() {
        return super.asMap();
    }

    @Override
    public Collection<V> values() {
        return super.values();
    }
}

