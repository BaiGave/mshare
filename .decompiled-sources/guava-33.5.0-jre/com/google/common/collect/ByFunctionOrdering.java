/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;
import com.google.common.collect.ParametricNullness;
import java.io.Serializable;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class ByFunctionOrdering<F, T>
extends Ordering<F>
implements Serializable {
    final Function<F, ? extends T> function;
    final Ordering<T> ordering;
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = 0L;

    ByFunctionOrdering(Function<F, ? extends T> function, Ordering<T> ordering) {
        this.function = Preconditions.checkNotNull(function);
        this.ordering = Preconditions.checkNotNull(ordering);
    }

    @Override
    public int compare(@ParametricNullness F left, @ParametricNullness F right) {
        return this.ordering.compare(this.function.apply(left), this.function.apply(right));
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof ByFunctionOrdering) {
            ByFunctionOrdering that = (ByFunctionOrdering)object;
            return this.function.equals(that.function) && this.ordering.equals(that.ordering);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.function, this.ordering);
    }

    public String toString() {
        return this.ordering + ".onResultOf(" + this.function + ")";
    }
}

