/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.IndexedImmutableSet;
import java.util.Set;
import org.jspecify.annotations.Nullable;

@GwtIncompatible
final class JdkBackedImmutableSet<E>
extends IndexedImmutableSet<E> {
    private final Set<?> delegate;
    private final ImmutableList<E> delegateList;

    JdkBackedImmutableSet(Set<?> delegate, ImmutableList<E> delegateList) {
        this.delegate = delegate;
        this.delegateList = delegateList;
    }

    @Override
    E get(int index) {
        return this.delegateList.get(index);
    }

    @Override
    public boolean contains(@Nullable Object object) {
        return this.delegate.contains(object);
    }

    @Override
    boolean isPartialView() {
        return false;
    }

    @Override
    public int size() {
        return this.delegateList.size();
    }

    @Override
    @J2ktIncompatible
    @GwtIncompatible
    Object writeReplace() {
        return super.writeReplace();
    }
}

