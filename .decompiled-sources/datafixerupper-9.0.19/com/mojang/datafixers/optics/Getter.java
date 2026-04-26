/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.datafixers.optics;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.Optic;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.optics.profunctors.GetterP;
import java.util.function.Function;
import java.util.function.Supplier;

interface Getter<S, T, A, B>
extends App2<Mu<A, B>, S, T>,
Optic<GetterP.Mu, S, T, A, B> {
    public static <S, T, A, B> Getter<S, T, A, B> unbox(App2<Mu<A, B>, S, T> box) {
        return (Getter)box;
    }

    public A get(S var1);

    @Override
    default public <P extends K2> FunctionType<App2<P, A, B>, App2<P, S, T>> eval(App<? extends GetterP.Mu, P> proof) {
        GetterP ops = GetterP.unbox(proof);
        return input -> ops.lmap(ops.secondPhantom(input), this::get);
    }

    public static final class Instance<A2, B2>
    implements GetterP<Mu<A2, B2>, GetterP.Mu> {
        @Override
        public <A, B, C, D> FunctionType<App2<Mu<A2, B2>, A, B>, App2<Mu<A2, B2>, C, D>> dimap(Function<C, A> g, Function<B, D> h) {
            return input -> Optics.getter(g.andThen(Getter.unbox(input)::get));
        }

        @Override
        public <A, B, C, D> FunctionType<Supplier<App2<Mu<A2, B2>, A, B>>, App2<Mu<A2, B2>, C, D>> cimap(Function<C, A> g, Function<D, B> h) {
            return input -> Optics.getter(g.andThen(Getter.unbox((App2)input.get())::get));
        }
    }

    public static final class Mu<A, B>
    implements K2 {
    }
}

