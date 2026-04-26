/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.datafixers.optics;

import com.mojang.datafixers.optics.Lens;
import com.mojang.datafixers.util.Pair;

public final class Proj2<F, G, G2>
implements Lens<Pair<F, G>, Pair<F, G2>, G, G2> {
    public static final Proj2<?, ?, ?> INSTANCE = new Proj2();

    private Proj2() {
    }

    @Override
    public G view(Pair<F, G> pair) {
        return pair.getSecond();
    }

    @Override
    public Pair<F, G2> update(G2 newValue, Pair<F, G> pair) {
        return Pair.of(pair.getFirst(), newValue);
    }

    public String toString() {
        return "\u03c02";
    }
}

