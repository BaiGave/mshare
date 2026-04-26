/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.primitives.Ints;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class JdkBackedImmutableMultiset<E>
extends ImmutableMultiset<E> {
    private final Map<E, Integer> delegateMap;
    private final ImmutableList<Multiset.Entry<E>> entries;
    private final long size;
    @LazyInit
    private transient @Nullable ImmutableSet<E> elementSet;

    static <E> ImmutableMultiset<E> create(Collection<? extends Multiset.Entry<? extends E>> entries) {
        Object[] entriesArray = entries.toArray(new Multiset.Entry[0]);
        HashMap delegateMap = Maps.newHashMapWithExpectedSize(entriesArray.length);
        long size = 0L;
        for (int i = 0; i < entriesArray.length; ++i) {
            Multiset.Entry entry = entriesArray[i];
            int count = entry.getCount();
            size += (long)count;
            Object element = Preconditions.checkNotNull(entry.getElement());
            delegateMap.put(element, count);
            if (entry instanceof Multisets.ImmutableEntry) continue;
            entriesArray[i] = Multisets.immutableEntry(element, count);
        }
        return new JdkBackedImmutableMultiset(delegateMap, ImmutableList.asImmutableList(entriesArray), size);
    }

    private JdkBackedImmutableMultiset(Map<E, Integer> delegateMap, ImmutableList<Multiset.Entry<E>> entries, long size) {
        this.delegateMap = delegateMap;
        this.entries = entries;
        this.size = size;
    }

    @Override
    public int count(@Nullable Object element) {
        return this.delegateMap.getOrDefault(element, 0);
    }

    @Override
    public ImmutableSet<E> elementSet() {
        ImmutableSet<E> result = this.elementSet;
        return result == null ? (this.elementSet = new ImmutableMultiset.ElementSet<E>(this.entries, this)) : result;
    }

    @Override
    Multiset.Entry<E> getEntry(int index) {
        return (Multiset.Entry)this.entries.get(index);
    }

    @Override
    boolean isPartialView() {
        return false;
    }

    @Override
    public int size() {
        return Ints.saturatedCast(this.size);
    }

    @Override
    @J2ktIncompatible
    @GwtIncompatible
    Object writeReplace() {
        return super.writeReplace();
    }
}

