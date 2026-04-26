/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.Collections2;
import com.google.common.collect.ForwardingObject;
import com.google.common.collect.Iterators;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.ParametricNullness;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class ForwardingCollection<E>
extends ForwardingObject
implements Collection<E> {
    protected ForwardingCollection() {
    }

    @Override
    protected abstract Collection<E> delegate();

    @Override
    public Iterator<E> iterator() {
        return this.delegate().iterator();
    }

    @Override
    public int size() {
        return this.delegate().size();
    }

    @Override
    @CanIgnoreReturnValue
    public boolean removeAll(Collection<?> collection) {
        return this.delegate().removeAll(collection);
    }

    @Override
    public boolean isEmpty() {
        return this.delegate().isEmpty();
    }

    @Override
    public boolean contains(@Nullable Object object) {
        return this.delegate().contains(object);
    }

    @Override
    @CanIgnoreReturnValue
    public boolean add(@ParametricNullness E element) {
        return this.delegate().add(element);
    }

    @Override
    @CanIgnoreReturnValue
    public boolean remove(@Nullable Object object) {
        return this.delegate().remove(object);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return this.delegate().containsAll(collection);
    }

    @Override
    @CanIgnoreReturnValue
    public boolean addAll(Collection<? extends E> collection) {
        return this.delegate().addAll(collection);
    }

    @Override
    @CanIgnoreReturnValue
    public boolean retainAll(Collection<?> collection) {
        return this.delegate().retainAll(collection);
    }

    @Override
    public void clear() {
        this.delegate().clear();
    }

    @Override
    public @Nullable Object[] toArray() {
        return this.delegate().toArray();
    }

    @Override
    @CanIgnoreReturnValue
    public <T> T[] toArray(T[] array) {
        return this.delegate().toArray(array);
    }

    protected boolean standardContains(@Nullable Object object) {
        return Iterators.contains(this.iterator(), object);
    }

    protected boolean standardContainsAll(Collection<?> collection) {
        return Collections2.containsAllImpl(this, collection);
    }

    protected boolean standardAddAll(Collection<? extends E> collection) {
        return Iterators.addAll(this, collection.iterator());
    }

    protected boolean standardRemove(@Nullable Object object) {
        Iterator<E> iterator = this.iterator();
        while (iterator.hasNext()) {
            if (!Objects.equals(iterator.next(), object)) continue;
            iterator.remove();
            return true;
        }
        return false;
    }

    protected boolean standardRemoveAll(Collection<?> collection) {
        return Iterators.removeAll(this.iterator(), collection);
    }

    protected boolean standardRetainAll(Collection<?> collection) {
        return Iterators.retainAll(this.iterator(), collection);
    }

    protected void standardClear() {
        Iterators.clear(this.iterator());
    }

    protected boolean standardIsEmpty() {
        return !this.iterator().hasNext();
    }

    protected String standardToString() {
        return Collections2.toStringImpl(this);
    }

    protected @Nullable Object[] standardToArray() {
        @Nullable Object[] newArray = new Object[this.size()];
        return this.toArray(newArray);
    }

    protected <T> T[] standardToArray(T[] array) {
        return ObjectArrays.toArrayImpl(this, array);
    }
}

