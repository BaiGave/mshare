/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;
import com.google.common.collect.ReverseNaturalOrdering;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.io.Serializable;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class NaturalOrdering
extends Ordering<Comparable<?>>
implements Serializable {
    static final NaturalOrdering INSTANCE = new NaturalOrdering();
    @LazyInit
    private transient @Nullable Ordering<@Nullable Comparable<?>> nullsFirst;
    @LazyInit
    private transient @Nullable Ordering<@Nullable Comparable<?>> nullsLast;
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = 0L;

    @Override
    public int compare(Comparable<?> left, Comparable<?> right) {
        Preconditions.checkNotNull(left);
        Preconditions.checkNotNull(right);
        return left.compareTo(right);
    }

    @Override
    public <S extends Comparable<?>> Ordering<@Nullable S> nullsFirst() {
        Ordering<@Nullable Comparable<Object>> result = this.nullsFirst;
        if (result == null) {
            result = this.nullsFirst = super.nullsFirst();
        }
        return result;
    }

    @Override
    public <S extends Comparable<?>> Ordering<@Nullable S> nullsLast() {
        Ordering<@Nullable Comparable<Object>> result = this.nullsLast;
        if (result == null) {
            result = this.nullsLast = super.nullsLast();
        }
        return result;
    }

    @Override
    public <S extends Comparable<?>> Ordering<S> reverse() {
        return ReverseNaturalOrdering.INSTANCE;
    }

    private Object readResolve() {
        return INSTANCE;
    }

    public String toString() {
        return "Ordering.natural()";
    }

    private NaturalOrdering() {
    }
}

