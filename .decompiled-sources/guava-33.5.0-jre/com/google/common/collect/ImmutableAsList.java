/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import org.jspecify.annotations.Nullable;

@GwtCompatible
abstract class ImmutableAsList<E>
extends ImmutableList<E> {
    ImmutableAsList() {
    }

    abstract ImmutableCollection<E> delegateCollection();

    @Override
    public boolean contains(@Nullable Object target) {
        return this.delegateCollection().contains(target);
    }

    @Override
    public int size() {
        return this.delegateCollection().size();
    }

    @Override
    public boolean isEmpty() {
        return this.delegateCollection().isEmpty();
    }

    @Override
    boolean isPartialView() {
        return this.delegateCollection().isPartialView();
    }

    @GwtIncompatible
    @J2ktIncompatible
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Use SerializedForm");
    }

    @Override
    @GwtIncompatible
    @J2ktIncompatible
    Object writeReplace() {
        return new SerializedForm(this.delegateCollection());
    }

    @GwtIncompatible
    @J2ktIncompatible
    private static final class SerializedForm
    implements Serializable {
        final ImmutableCollection<?> collection;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        SerializedForm(ImmutableCollection<?> collection) {
            this.collection = collection;
        }

        Object readResolve() {
            return this.collection.asList();
        }
    }
}

