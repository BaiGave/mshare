/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ParametricNullness;
import com.google.common.collect.TransformedIterator;
import java.util.ListIterator;

@GwtCompatible
abstract class TransformedListIterator<F, T>
extends TransformedIterator<F, T>
implements ListIterator<T> {
    TransformedListIterator(ListIterator<? extends F> backingIterator) {
        super(backingIterator);
    }

    private ListIterator<? extends F> backingIterator() {
        return (ListIterator)this.backingIterator;
    }

    @Override
    public final boolean hasPrevious() {
        return this.backingIterator().hasPrevious();
    }

    @Override
    @ParametricNullness
    public final T previous() {
        return this.transform(this.backingIterator().previous());
    }

    @Override
    public final int nextIndex() {
        return this.backingIterator().nextIndex();
    }

    @Override
    public final int previousIndex() {
        return this.backingIterator().previousIndex();
    }

    @Override
    public void set(@ParametricNullness T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(@ParametricNullness T element) {
        throw new UnsupportedOperationException();
    }
}

