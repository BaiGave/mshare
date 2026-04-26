/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableListIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class RegularImmutableList<E>
extends ImmutableList<E> {
    static final ImmutableList<Object> EMPTY = new RegularImmutableList<Object>(new Object[0]);
    @VisibleForTesting
    final transient Object[] array;

    RegularImmutableList(Object[] array) {
        this.array = array;
    }

    @Override
    public int size() {
        return this.array.length;
    }

    @Override
    boolean isPartialView() {
        return false;
    }

    @Override
    Object[] internalArray() {
        return this.array;
    }

    @Override
    int internalArrayStart() {
        return 0;
    }

    @Override
    int internalArrayEnd() {
        return this.array.length;
    }

    @Override
    int copyIntoArray(@Nullable Object[] dst, int dstOff) {
        System.arraycopy(this.array, 0, dst, dstOff, this.array.length);
        return dstOff + this.array.length;
    }

    @Override
    public E get(int index) {
        return (E)this.array[index];
    }

    @Override
    public UnmodifiableListIterator<E> listIterator(int index) {
        return Iterators.forArrayWithPosition(this.array, index);
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(this.array, 1296);
    }

    @Override
    @J2ktIncompatible
    @GwtIncompatible
    Object writeReplace() {
        return super.writeReplace();
    }
}

