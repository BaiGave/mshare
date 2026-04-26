/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ForwardingCollection;
import com.google.common.collect.ParametricNullness;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.NoSuchElementException;
import java.util.Queue;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class ForwardingQueue<E>
extends ForwardingCollection<E>
implements Queue<E> {
    protected ForwardingQueue() {
    }

    @Override
    protected abstract Queue<E> delegate();

    @Override
    @CanIgnoreReturnValue
    public boolean offer(@ParametricNullness E o) {
        return this.delegate().offer(o);
    }

    @Override
    @CanIgnoreReturnValue
    public @Nullable E poll() {
        return this.delegate().poll();
    }

    @Override
    @CanIgnoreReturnValue
    @ParametricNullness
    public E remove() {
        return this.delegate().remove();
    }

    @Override
    public @Nullable E peek() {
        return this.delegate().peek();
    }

    @Override
    @ParametricNullness
    public E element() {
        return this.delegate().element();
    }

    protected boolean standardOffer(@ParametricNullness E e) {
        try {
            return this.add(e);
        }
        catch (IllegalStateException caught) {
            return false;
        }
    }

    protected @Nullable E standardPeek() {
        try {
            return this.element();
        }
        catch (NoSuchElementException caught) {
            return null;
        }
    }

    protected @Nullable E standardPoll() {
        try {
            return this.remove();
        }
        catch (NoSuchElementException caught) {
            return null;
        }
    }
}

