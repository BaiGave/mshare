/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.ParametricNullness;
import java.util.Iterator;

@GwtCompatible
abstract class TransformedIterator<F, T>
implements Iterator<T> {
    final Iterator<? extends F> backingIterator;

    TransformedIterator(Iterator<? extends F> backingIterator) {
        this.backingIterator = Preconditions.checkNotNull(backingIterator);
    }

    @ParametricNullness
    abstract T transform(@ParametricNullness F var1);

    @Override
    public final boolean hasNext() {
        return this.backingIterator.hasNext();
    }

    @Override
    @ParametricNullness
    public final T next() {
        return this.transform(this.backingIterator.next());
    }

    @Override
    public final void remove() {
        this.backingIterator.remove();
    }
}

