/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ParametricNullness;
import com.google.common.collect.UnmodifiableIterator;
import com.google.errorprone.annotations.DoNotCall;
import java.util.ListIterator;

@GwtCompatible
public abstract class UnmodifiableListIterator<E>
extends UnmodifiableIterator<E>
implements ListIterator<E> {
    protected UnmodifiableListIterator() {
    }

    @Override
    @Deprecated
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final void add(@ParametricNullness E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final void set(@ParametricNullness E e) {
        throw new UnsupportedOperationException();
    }
}

