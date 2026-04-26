/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Hashing;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.RegularImmutableAsList;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class RegularImmutableSet<E>
extends ImmutableSet.CachingAsList<E> {
    private static final Object[] EMPTY_ARRAY = new Object[0];
    static final RegularImmutableSet<Object> EMPTY = new RegularImmutableSet(EMPTY_ARRAY, 0, EMPTY_ARRAY, 0);
    private final transient Object[] elements;
    private final transient int hashCode;
    @VisibleForTesting
    final transient @Nullable Object[] table;
    private final transient int mask;

    RegularImmutableSet(Object[] elements, int hashCode, @Nullable Object[] table, int mask) {
        this.elements = elements;
        this.hashCode = hashCode;
        this.table = table;
        this.mask = mask;
    }

    @Override
    public boolean contains(@Nullable Object target) {
        @Nullable Object[] table = this.table;
        if (target == null || table.length == 0) {
            return false;
        }
        int i = Hashing.smearedHash(target);
        Object candidate;
        while ((candidate = table[i &= this.mask]) != null) {
            if (candidate.equals(target)) {
                return true;
            }
            ++i;
        }
        return false;
    }

    @Override
    public int size() {
        return this.elements.length;
    }

    @Override
    public UnmodifiableIterator<E> iterator() {
        return Iterators.forArray(this.elements);
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(this.elements, 1297);
    }

    @Override
    Object[] internalArray() {
        return this.elements;
    }

    @Override
    int internalArrayStart() {
        return 0;
    }

    @Override
    int internalArrayEnd() {
        return this.elements.length;
    }

    @Override
    int copyIntoArray(@Nullable Object[] dst, int offset) {
        System.arraycopy(this.elements, 0, dst, offset, this.elements.length);
        return offset + this.elements.length;
    }

    @Override
    ImmutableList<E> createAsList() {
        return this.table.length == 0 ? ImmutableList.of() : new RegularImmutableAsList(this, this.elements);
    }

    @Override
    boolean isPartialView() {
        return false;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    boolean isHashCodeFast() {
        return true;
    }

    @Override
    @J2ktIncompatible
    @GwtIncompatible
    Object writeReplace() {
        return super.writeReplace();
    }
}

