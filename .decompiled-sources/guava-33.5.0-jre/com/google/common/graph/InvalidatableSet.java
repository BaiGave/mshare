/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.graph;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.ForwardingSet;
import java.util.Set;

final class InvalidatableSet<E>
extends ForwardingSet<E> {
    private final Supplier<Boolean> validator;
    private final Set<E> delegate;
    private final Supplier<String> errorMessage;

    static <E> InvalidatableSet<E> of(Set<E> delegate, Supplier<Boolean> validator, Supplier<String> errorMessage) {
        return new InvalidatableSet<E>(Preconditions.checkNotNull(delegate), Preconditions.checkNotNull(validator), Preconditions.checkNotNull(errorMessage));
    }

    @Override
    protected Set<E> delegate() {
        this.validate();
        return this.delegate;
    }

    private InvalidatableSet(Set<E> delegate, Supplier<Boolean> validator, Supplier<String> errorMessage) {
        this.delegate = delegate;
        this.validator = validator;
        this.errorMessage = errorMessage;
    }

    @Override
    public int hashCode() {
        return this.delegate.hashCode();
    }

    private void validate() {
        if (!this.validator.get().booleanValue()) {
            throw new IllegalStateException(this.errorMessage.get());
        }
    }
}

