/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.datafixers.functions;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.View;
import com.mojang.datafixers.functions.Functions;
import com.mojang.datafixers.functions.PointFree;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.Algebra;
import com.mojang.datafixers.types.families.ListAlgebra;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.templates.RecursivePoint;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;

final class Fold<A, B>
extends PointFree<Function<A, B>> {
    private static final Map<HmapCacheKey, IntFunction<RewriteResult<?, ?>>> HMAP_CACHE = Maps.newConcurrentMap();
    private static final Map<Pair<IntFunction<RewriteResult<?, ?>>, Integer>, RewriteResult<?, ?>> HMAP_APPLY_CACHE = Maps.newConcurrentMap();
    protected final RecursivePoint.RecursivePointType<A> aType;
    protected final RecursivePoint.RecursivePointType<B> bType;
    protected final Algebra algebra;
    protected final int index;

    public Fold(RecursivePoint.RecursivePointType<A> aType, RecursivePoint.RecursivePointType<B> bType, Algebra algebra, int index) {
        this.aType = aType;
        this.bType = bType;
        this.algebra = algebra;
        this.index = index;
    }

    @Override
    public Type<Function<A, B>> type() {
        return DSL.func(this.aType, this.bType);
    }

    @Override
    Optional<? extends PointFree<Function<A, B>>> all(PointFreeRule rule) {
        int familySize = this.aType.family().size();
        ArrayList newAlgebra = new ArrayList(familySize);
        boolean changed = false;
        for (int i = 0; i < familySize; ++i) {
            RewriteResult<?, ?> view = this.algebra.apply(i);
            PointFree<Function<?, ?>> function = view.view().function();
            PointFree<Function<?, ?>> rewrite = rule.rewriteOrNop(function);
            if (rewrite != function) {
                newAlgebra.add(Fold.cap(view, rewrite));
                changed = true;
                continue;
            }
            newAlgebra.add(view);
        }
        if (changed) {
            return Optional.of(new Fold<A, B>(this.aType, this.bType, new ListAlgebra("Rewrite all", newAlgebra), this.index));
        }
        return Optional.empty();
    }

    private static <A, B> RewriteResult<A, B> cap(RewriteResult<A, B> view, PointFree<? extends Function<?, ?>> rewrite) {
        return RewriteResult.create(new View(rewrite), view.recData());
    }

    private <FB> PointFree<Function<A, B>> cap(RewriteResult<?, FB> resResult) {
        RewriteResult<?, ?> op = this.algebra.apply(this.index);
        return Functions.comp(op.view().function(), resResult.view().function());
    }

    @Override
    public Function<DynamicOps<?>, Function<A, B>> eval() {
        return ops -> a -> {
            RecursiveTypeFamily family = this.aType.family();
            RecursiveTypeFamily newFamily = this.bType.family();
            IntFunction hmapped = HMAP_CACHE.computeIfAbsent(new HmapCacheKey(family, newFamily, this.algebra), key -> key.family().template().hmap(key.family(), key.family().fold(key.algebra(), key.newFamily())));
            RewriteResult result = HMAP_APPLY_CACHE.computeIfAbsent(Pair.of(hmapped, this.index), key -> (RewriteResult)((IntFunction)key.getFirst()).apply((Integer)key.getSecond()));
            PointFree<Function<A, B>> eval = this.cap(result);
            return eval.evalCached().apply((DynamicOps<?>)ops).apply(a);
        };
    }

    @Override
    public String toString(int level) {
        return "fold(" + String.valueOf(this.aType) + ", " + this.index + ", \n" + Fold.indent(level + 1) + this.algebra.toString(level + 1) + "\n" + Fold.indent(level) + ")";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Fold fold = (Fold)o;
        return Objects.equals(this.aType, fold.aType) && Objects.equals(this.bType, fold.bType) && Objects.equals(this.algebra, fold.algebra);
    }

    public int hashCode() {
        int result = this.aType.hashCode();
        result = 31 * result + this.bType.hashCode();
        result = 31 * result + this.algebra.hashCode();
        return result;
    }

    private record HmapCacheKey(RecursiveTypeFamily family, RecursiveTypeFamily newFamily, Algebra algebra) {
    }
}

