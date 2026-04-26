/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ParametricNullness;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotMock;
import java.util.Iterator;

@DoNotMock(value="Use Iterators.peekingIterator")
@GwtCompatible
public interface PeekingIterator<E>
extends Iterator<E> {
    @ParametricNullness
    public E peek();

    @Override
    @CanIgnoreReturnValue
    @ParametricNullness
    public E next();

    @Override
    public void remove();
}

