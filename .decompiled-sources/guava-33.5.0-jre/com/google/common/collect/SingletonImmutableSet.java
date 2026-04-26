/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class SingletonImmutableSet<E>
extends ImmutableSet<E> {
    final transient E element;

    SingletonImmutableSet(E element) {
        this.element = Preconditions.checkNotNull(element);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean contains(@Nullable Object target) {
        return this.element.equals(target);
    }

    @Override
    public UnmodifiableIterator<E> iterator() {
        return Iterators.singletonIterator(this.element);
    }

    @Override
    public ImmutableList<E> asList() {
        return ImmutableList.of(this.element);
    }

    @Override
    boolean isPartialView() {
        return false;
    }

    @Override
    int copyIntoArray(@Nullable Object[] dst, int offset) {
        dst[offset] = this.element;
        return offset + 1;
    }

    @Override
    public final int hashCode() {
        return this.element.hashCode();
    }

    @Override
    public String toString() {
        return '[' + this.element.toString() + ']';
    }

    @Override
    @J2ktIncompatible
    @GwtIncompatible
    Object writeReplace() {
        return super.writeReplace();
    }
}

