/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.datafixers;

import com.mojang.datafixers.View;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.RecursivePoint;
import java.util.BitSet;
import java.util.Objects;

public record RewriteResult<A, B>(View<A, B> view, BitSet recData) {
    public static <A, B> RewriteResult<A, B> create(View<A, B> view, BitSet recData) {
        return new RewriteResult<A, B>(view, recData);
    }

    public static <A> RewriteResult<A, A> nop(Type<A> type) {
        return new RewriteResult<A, A>(View.nopView(type), new BitSet());
    }

    public <C> RewriteResult<C, B> compose(RewriteResult<C, A> that) {
        BitSet newData;
        if (this.view.type() instanceof RecursivePoint.RecursivePointType && that.view.type() instanceof RecursivePoint.RecursivePointType) {
            newData = (BitSet)this.recData.clone();
            newData.or(that.recData);
        } else {
            newData = this.recData;
        }
        return RewriteResult.create(this.view.compose(that.view), newData);
    }

    @Override
    public String toString() {
        return "RR[" + String.valueOf(this.view) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        RewriteResult that = (RewriteResult)o;
        return Objects.equals(this.view, that.view);
    }

    @Override
    public int hashCode() {
        return this.view.hashCode();
    }
}

