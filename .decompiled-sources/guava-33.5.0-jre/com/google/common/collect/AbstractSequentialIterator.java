/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.UnmodifiableIterator;
import java.util.NoSuchElementException;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class AbstractSequentialIterator<T>
extends UnmodifiableIterator<T> {
    private @Nullable T nextOrNull;

    protected AbstractSequentialIterator(@Nullable T firstOrNull) {
        this.nextOrNull = firstOrNull;
    }

    protected abstract @Nullable T computeNext(T var1);

    @Override
    public final boolean hasNext() {
        return this.nextOrNull != null;
    }

    @Override
    public final T next() {
        if (this.nextOrNull == null) {
            throw new NoSuchElementException();
        }
        T oldNext = this.nextOrNull;
        this.nextOrNull = this.computeNext(oldNext);
        return oldNext;
    }
}

