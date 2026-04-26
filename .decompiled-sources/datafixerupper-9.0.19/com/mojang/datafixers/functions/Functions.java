/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.datafixers.functions;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.functions.Apply;
import com.mojang.datafixers.functions.Bang;
import com.mojang.datafixers.functions.Comp;
import com.mojang.datafixers.functions.Fold;
import com.mojang.datafixers.functions.FunctionWrapper;
import com.mojang.datafixers.functions.Id;
import com.mojang.datafixers.functions.In;
import com.mojang.datafixers.functions.Out;
import com.mojang.datafixers.functions.PointFree;
import com.mojang.datafixers.functions.ProfunctorTransformer;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.Algebra;
import com.mojang.datafixers.types.templates.RecursivePoint;
import com.mojang.serialization.DynamicOps;
import java.util.function.Function;

public abstract class Functions {
    public static <A, B, C> PointFree<Function<A, C>> comp(PointFree<Function<B, C>> f1, PointFree<Function<A, B>> f2) {
        if (Functions.isId(f1)) {
            return f2;
        }
        if (Functions.isId(f2)) {
            return f1;
        }
        if (f1 instanceof Comp) {
            Comp comp1 = (Comp)f1;
            if (f2 instanceof Comp) {
                Comp comp2 = (Comp)f2;
                PointFree[] functions = new PointFree[comp1.functions.length + comp2.functions.length];
                System.arraycopy(comp1.functions, 0, functions, 0, comp1.functions.length);
                System.arraycopy(comp2.functions, 0, functions, comp1.functions.length, comp2.functions.length);
                return new Comp(functions);
            }
        }
        if (f1 instanceof Comp) {
            Comp comp1 = (Comp)f1;
            PointFree[] functions = new PointFree[comp1.functions.length + 1];
            System.arraycopy(comp1.functions, 0, functions, 0, comp1.functions.length);
            functions[functions.length - 1] = f2;
            return new Comp(functions);
        }
        if (f2 instanceof Comp) {
            Comp comp2 = (Comp)f2;
            PointFree[] functions = new PointFree[1 + comp2.functions.length];
            functions[0] = f1;
            System.arraycopy(comp2.functions, 0, functions, 1, comp2.functions.length);
            return new Comp(functions);
        }
        return new Comp(f1, f2);
    }

    public static <A, B> PointFree<Function<A, B>> fun(String name, Function<DynamicOps<?>, Function<A, B>> fun, Type<A> input, Type<B> output) {
        return new FunctionWrapper<A, B>(name, fun, input, output);
    }

    public static <A, B> PointFree<B> app(PointFree<Function<A, B>> fun, PointFree<A> arg) {
        return new Apply<A, B>(fun, arg);
    }

    public static <S, T, A, B> PointFree<Function<Function<A, B>, Function<S, T>>> profunctorTransformer(TypedOptic<S, T, A, B> lens) {
        return new ProfunctorTransformer<S, T, A, B>(lens);
    }

    public static <A> Bang<A> bang(Type<A> type) {
        return new Bang<A>(type);
    }

    public static <A> PointFree<Function<A, A>> in(RecursivePoint.RecursivePointType<A> type) {
        return new In<A>(type);
    }

    public static <A> PointFree<Function<A, A>> out(RecursivePoint.RecursivePointType<A> type) {
        return new Out<A>(type);
    }

    public static <A, B> PointFree<Function<A, B>> fold(RecursivePoint.RecursivePointType<A> aType, RecursivePoint.RecursivePointType<B> bType, Algebra algebra, int index) {
        return new Fold<A, B>(aType, bType, algebra, index);
    }

    public static <A> PointFree<Function<A, A>> id(Type<A> type) {
        return new Id<A>(DSL.func(type, type));
    }

    public static boolean isId(PointFree<?> function) {
        return function instanceof Id;
    }
}

