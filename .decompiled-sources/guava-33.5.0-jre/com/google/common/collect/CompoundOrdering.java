/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.ParametricNullness;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class CompoundOrdering<T>
extends Ordering<T>
implements Serializable {
    final Comparator<? super T>[] comparators;
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = 0L;

    CompoundOrdering(Comparator<? super T> primary, Comparator<? super T> secondary) {
        this.comparators = new Comparator[]{primary, secondary};
    }

    CompoundOrdering(Iterable<? extends Comparator<? super T>> comparators) {
        this.comparators = Iterables.toArray(comparators, new Comparator[0]);
    }

    @Override
    public int compare(@ParametricNullness T left, @ParametricNullness T right) {
        for (int i = 0; i < this.comparators.length; ++i) {
            int result = this.comparators[i].compare(left, right);
            if (result == 0) continue;
            return result;
        }
        return 0;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof CompoundOrdering) {
            CompoundOrdering that = (CompoundOrdering)object;
            return Arrays.equals(this.comparators, that.comparators);
        }
        return false;
    }

    public int hashCode() {
        return Arrays.hashCode(this.comparators);
    }

    public String toString() {
        return "Ordering.compound(" + Arrays.toString(this.comparators) + ")";
    }
}

