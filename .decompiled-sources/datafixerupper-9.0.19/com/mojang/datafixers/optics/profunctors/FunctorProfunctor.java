/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.datafixers.optics.profunctors;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.kinds.Kind2;

public interface FunctorProfunctor<T extends K1, P extends K2, Mu extends Mu<T>>
extends Kind2<P, Mu> {
    public static <T extends K1, P extends K2, Mu extends Mu<T>> FunctorProfunctor<T, P, Mu> unbox(App<Mu, P> proofBox) {
        return (FunctorProfunctor)proofBox;
    }

    public <A, B, F extends K1> App2<P, App<F, A>, App<F, B>> distribute(App<? extends T, F> var1, App2<P, A, B> var2);

    public static interface Mu<T extends K1>
    extends Kind2.Mu {
    }
}

