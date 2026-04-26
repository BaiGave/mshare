/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.collect.AbstractMapBasedMultimap;
import com.google.common.collect.ParametricNullness;
import com.google.common.collect.SetMultimap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;

@GwtCompatible
abstract class AbstractSetMultimap<K, V>
extends AbstractMapBasedMultimap<K, V>
implements SetMultimap<K, V> {
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = 7431625294878419160L;

    protected AbstractSetMultimap(Map<K, Collection<V>> map) {
        super(map);
    }

    @Override
    abstract Set<V> createCollection();

    @Override
    Set<V> createUnmodifiableEmptyCollection() {
        return Collections.emptySet();
    }

    @Override
    <E> Collection<E> unmodifiableCollectionSubclass(Collection<E> collection) {
        return Collections.unmodifiableSet((Set)collection);
    }

    @Override
    Collection<V> wrapCollection(@ParametricNullness K key, Collection<V> collection) {
        return new AbstractMapBasedMultimap.WrappedSet(key, (Set)collection);
    }

    @Override
    public Set<V> get(@ParametricNullness K key) {
        return (Set)super.get(key);
    }

    @Override
    public Set<Map.Entry<K, V>> entries() {
        return (Set)super.entries();
    }

    @Override
    @CanIgnoreReturnValue
    public Set<V> removeAll(@Nullable Object key) {
        return (Set)super.removeAll(key);
    }

    @Override
    @CanIgnoreReturnValue
    public Set<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values) {
        return (Set)super.replaceValues(key, values);
    }

    @Override
    public Map<K, Collection<V>> asMap() {
        return super.asMap();
    }

    @Override
    @CanIgnoreReturnValue
    public boolean put(@ParametricNullness K key, @ParametricNullness V value) {
        return super.put(key, value);
    }

    @Override
    public boolean equals(@Nullable Object object) {
        return super.equals(object);
    }
}

