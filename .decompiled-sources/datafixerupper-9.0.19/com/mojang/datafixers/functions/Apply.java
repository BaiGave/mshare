/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.datafixers.functions;

import com.mojang.datafixers.functions.PointFree;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.types.Func;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DynamicOps;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

final class Apply<A, B>
extends PointFree<B> {
    protected final PointFree<Function<A, B>> func;
    protected final PointFree<A> arg;
    protected final Type<B> type;

    public Apply(PointFree<Function<A, B>> func, PointFree<A> arg) {
        this(func, arg, ((Func)func.type()).second());
    }

    Apply(PointFree<Function<A, B>> func, PointFree<A> arg, Type<B> type) {
        this.func = func;
        this.arg = arg;
        this.type = type;
    }

    @Override
    public Function<DynamicOps<?>, B> eval() {
        return ops -> this.func.evalCached().apply((DynamicOps<?>)ops).apply(this.arg.evalCached().apply((DynamicOps<?>)ops));
    }

    @Override
    public Type<B> type() {
        return this.type;
    }

    @Override
    public String toString(int level) {
        return "(ap " + this.func.toString(level + 1) + "\n" + Apply.indent(level + 1) + this.arg.toString(level + 1) + "\n" + Apply.indent(level) + ")";
    }

    @Override
    public Optional<? extends PointFree<B>> all(PointFreeRule rule) {
        PointFree<Function<A, B>> f = rule.rewriteOrNop(this.func);
        PointFree<A> a = rule.rewriteOrNop(this.arg);
        if (f == this.func && a == this.arg) {
            return Optional.of(this);
        }
        return Optional.of(new Apply<A, B>(f, a, this.type));
    }

    @Override
    public Optional<? extends PointFree<B>> one(PointFreeRule rule) {
        return rule.rewrite(this.func).map(f -> new Apply<A, B>(f, this.arg, this.type)).or(() -> rule.rewrite(this.arg).map(a -> new Apply<A, B>(this.func, a, this.type)));
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Apply)) {
            return false;
        }
        Apply apply = (Apply)o;
        return Objects.equals(this.func, apply.func) && Objects.equals(this.arg, apply.arg);
    }

    public int hashCode() {
        int result = this.func.hashCode();
        result = 31 * result + this.arg.hashCode();
        return result;
    }
}

