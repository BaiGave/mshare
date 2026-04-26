/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.collect.ForwardingQueue;
import com.google.common.collect.ParametricNullness;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Deque;
import java.util.Iterator;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public abstract class ForwardingDeque<E>
extends ForwardingQueue<E>
implements Deque<E> {
    protected ForwardingDeque() {
    }

    @Override
    protected abstract Deque<E> delegate();

    @Override
    public void addFirst(@ParametricNullness E e) {
        this.delegate().addFirst(e);
    }

    @Override
    public void addLast(@ParametricNullness E e) {
        this.delegate().addLast(e);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return this.delegate().descendingIterator();
    }

    @Override
    @ParametricNullness
    public E getFirst() {
        return this.delegate().getFirst();
    }

    @Override
    @ParametricNullness
    public E getLast() {
        return this.delegate().getLast();
    }

    @Override
    @CanIgnoreReturnValue
    public boolean offerFirst(@ParametricNullness E e) {
        return this.delegate().offerFirst(e);
    }

    @Override
    @CanIgnoreReturnValue
    public boolean offerLast(@ParametricNullness E e) {
        return this.delegate().offerLast(e);
    }

    @Override
    public @Nullable E peekFirst() {
        return this.delegate().peekFirst();
    }

    @Override
    public @Nullable E peekLast() {
        return this.delegate().peekLast();
    }

    @Override
    @CanIgnoreReturnValue
    public @Nullable E pollFirst() {
        return this.delegate().pollFirst();
    }

    @Override
    @CanIgnoreReturnValue
    public @Nullable E pollLast() {
        return this.delegate().pollLast();
    }

    @Override
    @CanIgnoreReturnValue
    @ParametricNullness
    public E pop() {
        return this.delegate().pop();
    }

    @Override
    public void push(@ParametricNullness E e) {
        this.delegate().push(e);
    }

    @Override
    @CanIgnoreReturnValue
    @ParametricNullness
    public E removeFirst() {
        return this.delegate().removeFirst();
    }

    @Override
    @CanIgnoreReturnValue
    @ParametricNullness
    public E removeLast() {
        return this.delegate().removeLast();
    }

    @Override
    @CanIgnoreReturnValue
    public boolean removeFirstOccurrence(@Nullable Object o) {
        return this.delegate().removeFirstOccurrence(o);
    }

    @Override
    @CanIgnoreReturnValue
    public boolean removeLastOccurrence(@Nullable Object o) {
        return this.delegate().removeLastOccurrence(o);
    }
}

