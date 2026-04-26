/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ForwardingIterator;
import com.google.common.collect.ParametricNullness;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ListIterator;

@GwtCompatible
public abstract class ForwardingListIterator<E>
extends ForwardingIterator<E>
implements ListIterator<E> {
    protected ForwardingListIterator() {
    }

    @Override
    protected abstract ListIterator<E> delegate();

    @Override
    public void add(@ParametricNullness E element) {
        this.delegate().add(element);
    }

    @Override
    public boolean hasPrevious() {
        return this.delegate().hasPrevious();
    }

    @Override
    public int nextIndex() {
        return this.delegate().nextIndex();
    }

    @Override
    @CanIgnoreReturnValue
    @ParametricNullness
    public E previous() {
        return this.delegate().previous();
    }

    @Override
    public int previousIndex() {
        return this.delegate().previousIndex();
    }

    @Override
    public void set(@ParametricNullness E element) {
        this.delegate().set(element);
    }
}

