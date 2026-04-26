/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.NullnessCasts;
import com.google.common.base.ParametricNullness;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jspecify.annotations.Nullable;

@GwtCompatible
abstract class AbstractIterator<T>
implements Iterator<T> {
    private State state = State.NOT_READY;
    private @Nullable T next;

    protected AbstractIterator() {
    }

    protected abstract @Nullable T computeNext();

    @CanIgnoreReturnValue
    protected final @Nullable T endOfData() {
        this.state = State.DONE;
        return null;
    }

    @Override
    public final boolean hasNext() {
        Preconditions.checkState(this.state != State.FAILED);
        switch (this.state.ordinal()) {
            case 2: {
                return false;
            }
            case 0: {
                return true;
            }
        }
        return this.tryToComputeNext();
    }

    private boolean tryToComputeNext() {
        this.state = State.FAILED;
        this.next = this.computeNext();
        if (this.state != State.DONE) {
            this.state = State.READY;
            return true;
        }
        return false;
    }

    @Override
    @ParametricNullness
    public final T next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }
        this.state = State.NOT_READY;
        T result = NullnessCasts.uncheckedCastNullableTToT(this.next);
        this.next = null;
        return result;
    }

    @Override
    public final void remove() {
        throw new UnsupportedOperationException();
    }

    private static enum State {
        READY,
        NOT_READY,
        DONE,
        FAILED;

    }
}

