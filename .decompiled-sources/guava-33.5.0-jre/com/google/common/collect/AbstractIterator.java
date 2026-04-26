/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.NullnessCasts;
import com.google.common.collect.ParametricNullness;
import com.google.common.collect.UnmodifiableIterator;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.NoSuchElementException;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class AbstractIterator<T>
extends UnmodifiableIterator<T> {
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
    @CanIgnoreReturnValue
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

    @ParametricNullness
    public final T peek() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }
        return NullnessCasts.uncheckedCastNullableTToT(this.next);
    }

    private static enum State {
        READY,
        NOT_READY,
        DONE,
        FAILED;

    }
}

