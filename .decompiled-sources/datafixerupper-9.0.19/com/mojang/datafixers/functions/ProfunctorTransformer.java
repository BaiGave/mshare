/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.datafixers.functions;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.functions.PointFree;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DynamicOps;
import java.util.Objects;
import java.util.function.Function;

final class ProfunctorTransformer<S, T, A, B>
extends PointFree<Function<Function<A, B>, Function<S, T>>> {
    protected final TypedOptic<S, T, A, B> optic;

    public ProfunctorTransformer(TypedOptic<S, T, A, B> optic) {
        this.optic = optic;
    }

    public <S2, T2> ProfunctorTransformer<S2, T2, A, B> castOuterUnchecked(Type<S2> sType, Type<T2> tType) {
        return new ProfunctorTransformer<S2, T2, A, B>(this.optic.castOuterUnchecked(sType, tType));
    }

    @Override
    public Type<Function<Function<A, B>, Function<S, T>>> type() {
        return DSL.func(DSL.func(this.optic.aType(), this.optic.bType()), DSL.func(this.optic.sType(), this.optic.tType()));
    }

    @Override
    public String toString(int level) {
        return "Optic[" + String.valueOf(this.optic) + "]";
    }

    @Override
    public Function<DynamicOps<?>, Function<Function<A, B>, Function<S, T>>> eval() {
        Function func = this.optic.upCast(FunctionType.Instance.Mu.TYPE_TOKEN).orElseThrow().eval(FunctionType.Instance.INSTANCE);
        Function<Function, Function> unwrappedFunction = input -> FunctionType.unbox((App2)func.apply(FunctionType.create(input)));
        return ops -> unwrappedFunction;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ProfunctorTransformer that = (ProfunctorTransformer)o;
        return Objects.equals(this.optic, that.optic);
    }

    public int hashCode() {
        return this.optic.hashCode();
    }
}

