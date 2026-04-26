/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.datafixers.functions;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.functions.Apply;
import com.mojang.datafixers.functions.Bang;
import com.mojang.datafixers.functions.Comp;
import com.mojang.datafixers.functions.Fold;
import com.mojang.datafixers.functions.Functions;
import com.mojang.datafixers.functions.PointFree;
import com.mojang.datafixers.functions.ProfunctorTransformer;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.types.Func;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.constant.EmptyPart;
import com.mojang.datafixers.types.families.ListAlgebra;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.templates.Product;
import com.mojang.datafixers.types.templates.Sum;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface PointFreeRule {
    public <A> Optional<? extends PointFree<A>> rewrite(PointFree<A> var1);

    default public <A> PointFree<A> rewriteOrNop(PointFree<A> expr) {
        return DataFixUtils.orElse(this.rewrite(expr), expr);
    }

    public static PointFreeRule nop() {
        return Nop.INSTANCE;
    }

    public static PointFreeRule seq(PointFreeRule ... rules) {
        return new Seq(rules);
    }

    public static PointFreeRule choice(PointFreeRule ... rules) {
        if (rules.length == 1) {
            return rules[0];
        }
        if (rules.length == 2) {
            return new Choice2(rules[0], rules[1]);
        }
        return new Choice(rules);
    }

    public static PointFreeRule all(PointFreeRule rule) {
        return new All(rule);
    }

    public static PointFreeRule one(PointFreeRule rule) {
        return new One(rule);
    }

    public static PointFreeRule once(PointFreeRule rule) {
        return new Once(rule);
    }

    public static PointFreeRule many(PointFreeRule rule) {
        return new Many(rule);
    }

    public static PointFreeRule everywhere(PointFreeRule topDown, PointFreeRule bottomUp) {
        return new Everywhere(topDown, bottomUp);
    }

    public static enum Nop implements PointFreeRule,
    Supplier<PointFreeRule>
    {
        INSTANCE;


        public <A> Optional<PointFree<A>> rewrite(PointFree<A> expr) {
            return Optional.of(expr);
        }

        @Override
        public PointFreeRule get() {
            return this;
        }
    }

    public record Seq(PointFreeRule[] rules) implements PointFreeRule
    {
        @Override
        public <A> Optional<? extends PointFree<A>> rewrite(PointFree<A> expr) {
            PointFree<A> result = expr;
            for (PointFreeRule rule : this.rules) {
                result = rule.rewriteOrNop(result);
            }
            return Optional.of(result);
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Seq)) return false;
            Seq that = (Seq)obj;
            if (!Arrays.equals(this.rules, that.rules)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.rules);
        }
    }

    public record Choice2(PointFreeRule first, PointFreeRule second) implements PointFreeRule
    {
        @Override
        public <A> Optional<? extends PointFree<A>> rewrite(PointFree<A> expr) {
            Optional<PointFree<A>> view = this.first.rewrite(expr);
            if (view.isPresent()) {
                return view;
            }
            return this.second.rewrite(expr);
        }
    }

    public record Choice(PointFreeRule[] rules) implements PointFreeRule
    {
        @Override
        public <A> Optional<? extends PointFree<A>> rewrite(PointFree<A> expr) {
            for (PointFreeRule rule : this.rules) {
                Optional<PointFree<A>> view = rule.rewrite(expr);
                if (!view.isPresent()) continue;
                return view;
            }
            return Optional.empty();
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Choice)) return false;
            Choice that = (Choice)obj;
            if (!Arrays.equals(this.rules, that.rules)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.rules);
        }
    }

    public record All(PointFreeRule rule) implements PointFreeRule
    {
        @Override
        public <A> Optional<? extends PointFree<A>> rewrite(PointFree<A> expr) {
            return expr.all(this.rule);
        }
    }

    public record One(PointFreeRule rule) implements PointFreeRule
    {
        @Override
        public <A> Optional<? extends PointFree<A>> rewrite(PointFree<A> expr) {
            return expr.one(this.rule);
        }
    }

    public record Once(PointFreeRule rule) implements PointFreeRule
    {
        @Override
        public <A> Optional<? extends PointFree<A>> rewrite(PointFree<A> expr) {
            Optional<PointFree<A>> view = this.rule.rewrite(expr);
            if (view.isPresent()) {
                return view;
            }
            return expr.one(this);
        }
    }

    public record Many(PointFreeRule rule) implements PointFreeRule
    {
        @Override
        public <A> Optional<? extends PointFree<A>> rewrite(PointFree<A> expr) {
            Optional<PointFree<Object>> result = Optional.of(expr);
            Optional newResult;
            while (!(newResult = result.flatMap(this.rule::rewrite)).isEmpty()) {
                result = newResult;
            }
            return result;
        }
    }

    public record Everywhere(PointFreeRule topDown, PointFreeRule bottomUp) implements PointFreeRule
    {
        @Override
        public <A> Optional<? extends PointFree<A>> rewrite(PointFree<A> expr) {
            PointFree<A> topDown = this.topDown.rewriteOrNop(expr);
            PointFree<A> all = DataFixUtils.orElse(topDown.all(this), topDown);
            PointFree<A> bottomUp = this.bottomUp.rewriteOrNop(all);
            return Optional.of(bottomUp);
        }
    }

    public static enum CataFuseDifferent implements CompRewrite
    {
        INSTANCE;


        @Override
        public Optional<? extends PointFree<? extends Function<?, ?>>> doRewrite(PointFree<? extends Function<?, ?>> first, PointFree<? extends Function<?, ?>> second) {
            if (first instanceof Fold) {
                Fold firstFold = (Fold)first;
                if (second instanceof Fold) {
                    Fold secondFold = (Fold)second;
                    RecursiveTypeFamily family = firstFold.aType.family();
                    if (firstFold.index == secondFold.index && Objects.equals(family, secondFold.aType.family())) {
                        RewriteResult<?, ?> secondAlgFunc;
                        RewriteResult<?, ?> firstAlgFunc;
                        int i;
                        RecursiveTypeFamily newFamily = firstFold.bType.family();
                        ArrayList<RewriteResult<?, ?>> newAlgebra = Lists.newArrayList();
                        BitSet firstModifies = new BitSet(family.size());
                        BitSet secondModifies = new BitSet(family.size());
                        for (i = 0; i < family.size(); ++i) {
                            firstAlgFunc = firstFold.algebra.apply(i);
                            secondAlgFunc = secondFold.algebra.apply(i);
                            boolean firstId = firstAlgFunc.view().isNop();
                            boolean secondId = secondAlgFunc.view().isNop();
                            if (!firstId && !secondId) {
                                return Optional.empty();
                            }
                            firstModifies.set(i, !firstId);
                            secondModifies.set(i, !secondId);
                        }
                        for (i = 0; i < family.size(); ++i) {
                            firstAlgFunc = firstFold.algebra.apply(i);
                            secondAlgFunc = secondFold.algebra.apply(i);
                            if (firstAlgFunc.recData().intersects(secondModifies) || secondAlgFunc.recData().intersects(firstModifies)) {
                                return Optional.empty();
                            }
                            if (firstAlgFunc.view().isNop()) {
                                newAlgebra.add(secondAlgFunc);
                                continue;
                            }
                            newAlgebra.add(firstAlgFunc);
                        }
                        ListAlgebra algebra = new ListAlgebra("FusedDifferent", newAlgebra);
                        return Optional.of(family.fold(algebra, newFamily).apply(firstFold.index).view().function());
                    }
                }
            }
            return Optional.empty();
        }
    }

    public static enum CataFuseSame implements CompRewrite
    {
        INSTANCE;


        @Override
        public Optional<? extends PointFree<? extends Function<?, ?>>> doRewrite(PointFree<? extends Function<?, ?>> first, PointFree<? extends Function<?, ?>> second) {
            if (first instanceof Fold) {
                Fold firstFold = (Fold)first;
                if (second instanceof Fold) {
                    Fold secondFold = (Fold)second;
                    RecursiveTypeFamily family = firstFold.aType.family();
                    if (firstFold.index == secondFold.index && Objects.equals(family, secondFold.aType.family())) {
                        RecursiveTypeFamily newFamily = firstFold.bType.family();
                        ArrayList<RewriteResult<?, ?>> newAlgebra = Lists.newArrayList();
                        boolean foundOne = false;
                        for (int i = 0; i < family.size(); ++i) {
                            RewriteResult<?, ?> firstAlgFunc = firstFold.algebra.apply(i);
                            RewriteResult<?, ?> secondAlgFunc = secondFold.algebra.apply(i);
                            boolean firstId = firstAlgFunc.view().isNop();
                            boolean secondId = secondAlgFunc.view().isNop();
                            if (firstId && secondId) {
                                newAlgebra.add(firstAlgFunc);
                                continue;
                            }
                            if (!(foundOne || firstId || secondId)) {
                                newAlgebra.add(this.getCompose(firstAlgFunc, secondAlgFunc));
                                foundOne = true;
                                continue;
                            }
                            return Optional.empty();
                        }
                        ListAlgebra algebra = new ListAlgebra("FusedSame", newAlgebra);
                        return Optional.of(family.fold(algebra, newFamily).apply(firstFold.index).view().function());
                    }
                }
            }
            return Optional.empty();
        }

        private <B> RewriteResult<?, ?> getCompose(RewriteResult<B, ?> firstAlgFunc, RewriteResult<?, ?> secondAlgFunc) {
            return firstAlgFunc.compose(secondAlgFunc);
        }
    }

    public static enum LensComp implements CompRewrite
    {
        INSTANCE;


        @Override
        public Optional<? extends PointFree<? extends Function<?, ?>>> doRewrite(PointFree<? extends Function<?, ?>> first, PointFree<? extends Function<?, ?>> second) {
            if (first instanceof Apply) {
                Apply applyFirst = (Apply)first;
                if (second instanceof Apply) {
                    Apply applySecond = (Apply)second;
                    PointFree firstFunc = applyFirst.func;
                    PointFree secondFunc = applySecond.func;
                    if (firstFunc instanceof ProfunctorTransformer) {
                        ProfunctorTransformer transformerFirst = (ProfunctorTransformer)firstFunc;
                        if (secondFunc instanceof ProfunctorTransformer) {
                            List<TypedOptic.Element<?, ?, ?, ?>> decomposedSecond;
                            ProfunctorTransformer transformerSecond = (ProfunctorTransformer)secondFunc;
                            List<TypedOptic.Element<?, ?, ?, ?>> decomposedFirst = transformerFirst.optic.elements();
                            int prefixSize = LensComp.findCommonPrefix(decomposedFirst, decomposedSecond = transformerSecond.optic.elements());
                            if (prefixSize == 0) {
                                return Optional.empty();
                            }
                            if (prefixSize == decomposedFirst.size() && prefixSize == decomposedSecond.size()) {
                                return Optional.of(this.capApp(transformerFirst.optic, this.capComp(applyFirst.arg, applySecond.arg)));
                            }
                            Sets.SetView<TypeToken<? extends K1>> bounds = Sets.union(transformerFirst.optic.bounds(), transformerSecond.optic.bounds());
                            TypedOptic prefix = new TypedOptic(bounds, decomposedFirst.subList(0, prefixSize));
                            PointFree firstFork = this.capApp(new TypedOptic(bounds, decomposedFirst.subList(prefixSize, decomposedFirst.size())), applyFirst.arg);
                            PointFree secondFork = this.capApp(new TypedOptic(bounds, decomposedSecond.subList(prefixSize, decomposedSecond.size())), applySecond.arg);
                            return Optional.of(this.capApp(prefix, this.capComp(firstFork, secondFork)));
                        }
                    }
                }
            }
            return Optional.empty();
        }

        private static int findCommonPrefix(List<? extends TypedOptic.Element<?, ?, ?, ?>> first, List<? extends TypedOptic.Element<?, ?, ?, ?>> second) {
            int size = Math.min(first.size(), second.size());
            for (int i = 0; i < size; ++i) {
                if (first.get(i).optic().equals(second.get(i).optic())) continue;
                return i;
            }
            return size;
        }

        private <A, B, C> PointFree<Function<A, C>> capComp(PointFree<?> f1, PointFree<?> f2) {
            return Functions.comp(f1, f2);
        }

        private <R, A, B, S, T> PointFree<R> capApp(TypedOptic<S, T, A, B> optic, PointFree<?> f) {
            if (optic.elements().isEmpty()) {
                return f;
            }
            return Functions.app(new ProfunctorTransformer<S, T, A, B>(optic), f);
        }
    }

    public static enum SortInj implements CompRewrite
    {
        INSTANCE;


        @Override
        public Optional<? extends PointFree<? extends Function<?, ?>>> doRewrite(PointFree<? extends Function<?, ?>> first, PointFree<? extends Function<?, ?>> second) {
            if (first instanceof Apply) {
                Apply applyFirst = (Apply)first;
                if (second instanceof Apply) {
                    Apply applySecond = (Apply)second;
                    PointFree firstFunc = applyFirst.func;
                    PointFree secondFunc = applySecond.func;
                    if (firstFunc instanceof ProfunctorTransformer) {
                        ProfunctorTransformer firstOptic = (ProfunctorTransformer)firstFunc;
                        if (secondFunc instanceof ProfunctorTransformer) {
                            ProfunctorTransformer secondOptic = (ProfunctorTransformer)secondFunc;
                            if (!Optics.isInj2(firstOptic.optic.outermost())) {
                                return Optional.empty();
                            }
                            if (!Optics.isInj1(secondOptic.optic.outermost())) {
                                return Optional.empty();
                            }
                            return Optional.of((PointFree)this.cap(applyFirst, applySecond));
                        }
                    }
                }
            }
            return Optional.empty();
        }

        private <R, A, A2, B, B2> R cap(Apply<?, ?> first, Apply<?, ?> second) {
            ProfunctorTransformer firstFunc = (ProfunctorTransformer)first.func;
            ProfunctorTransformer secondFunc = (ProfunctorTransformer)second.func;
            PointFree firstArg = first.arg;
            PointFree secondArg = second.arg;
            Func firstType = (Func)first.type;
            Func secondType = (Func)second.type;
            Sum.SumType input = (Sum.SumType)secondType.first();
            Sum.SumType output = (Sum.SumType)firstType.second();
            return (R)new Comp(new Apply(secondFunc.castOuterUnchecked(DSL.or(output.first(), input.second()), output), secondArg), new Apply(firstFunc.castOuterUnchecked(input, DSL.or(output.first(), input.second())), firstArg));
        }
    }

    public static enum SortProj implements CompRewrite
    {
        INSTANCE;


        @Override
        public Optional<? extends PointFree<? extends Function<?, ?>>> doRewrite(PointFree<? extends Function<?, ?>> first, PointFree<? extends Function<?, ?>> second) {
            if (first instanceof Apply) {
                Apply applyFirst = (Apply)first;
                if (second instanceof Apply) {
                    Apply applySecond = (Apply)second;
                    PointFree firstFunc = applyFirst.func;
                    PointFree secondFunc = applySecond.func;
                    if (firstFunc instanceof ProfunctorTransformer) {
                        ProfunctorTransformer firstOptic = (ProfunctorTransformer)firstFunc;
                        if (secondFunc instanceof ProfunctorTransformer) {
                            ProfunctorTransformer secondOptic = (ProfunctorTransformer)secondFunc;
                            if (!Optics.isProj2(firstOptic.optic.outermost())) {
                                return Optional.empty();
                            }
                            if (!Optics.isProj1(secondOptic.optic.outermost())) {
                                return Optional.empty();
                            }
                            return Optional.of((PointFree)this.cap(applyFirst, applySecond));
                        }
                    }
                }
            }
            return Optional.empty();
        }

        private <R, A, A2, B, B2> R cap(Apply<?, ?> first, Apply<?, ?> second) {
            ProfunctorTransformer firstFunc = (ProfunctorTransformer)first.func;
            ProfunctorTransformer secondFunc = (ProfunctorTransformer)second.func;
            PointFree firstArg = first.arg;
            PointFree secondArg = second.arg;
            Func firstType = (Func)first.type;
            Func secondType = (Func)second.type;
            Product.ProductType input = (Product.ProductType)secondType.first();
            Product.ProductType output = (Product.ProductType)firstType.second();
            return (R)new Comp(new Apply(secondFunc.castOuterUnchecked(DSL.and(output.first(), input.second()), output), secondArg), new Apply(firstFunc.castOuterUnchecked(input, DSL.and(output.first(), input.second())), firstArg));
        }
    }

    public static interface CompRewrite
    extends PointFreeRule {
        public static CompRewrite together(CompRewrite ... rules) {
            return (first, second) -> {
                for (CompRewrite rule : rules) {
                    Optional<PointFree<Function<?, ?>>> view = rule.doRewrite(first, second);
                    if (!view.isPresent()) continue;
                    return view;
                }
                return Optional.empty();
            };
        }

        @Override
        default public <A> Optional<? extends PointFree<A>> rewrite(PointFree<A> expr) {
            if (expr instanceof Comp) {
                Comp comp = (Comp)expr;
                return this.rewrite(comp.functions).map(rewrite -> {
                    if (((PointFree[])rewrite).length == 1) {
                        return rewrite[0];
                    }
                    return new Comp((PointFree<? extends Function<?, ?>>)rewrite);
                });
            }
            return Optional.empty();
        }

        private Optional<PointFree<? extends Function<?, ?>>[]> rewrite(PointFree<? extends Function<?, ?>>[] functions) {
            ArrayDeque<PointFree> result = new ArrayDeque<PointFree>(functions.length);
            boolean rewritten = false;
            ArrayDeque queue = new ArrayDeque(functions.length);
            Collections.addAll(queue, functions);
            while (!queue.isEmpty()) {
                Optional rewrite;
                PointFree next = (PointFree)queue.removeFirst();
                PointFree last = (PointFree)result.peekLast();
                Optional<Object> optional = rewrite = last != null ? this.doRewrite(last, next) : Optional.empty();
                if (rewrite.isPresent()) {
                    result.removeLast();
                    CompRewrite.addFirst(queue, (PointFree)rewrite.get());
                    rewritten = true;
                    continue;
                }
                result.add(next);
            }
            return rewritten ? Optional.of((PointFree[])result.toArray(PointFree[]::new)) : Optional.empty();
        }

        private static void addFirst(Deque<PointFree<? extends Function<?, ?>>> queue, PointFree<? extends Function<?, ?>> function) {
            if (function instanceof Comp) {
                Comp comp = (Comp)function;
                for (int i = comp.functions.length - 1; i >= 0; --i) {
                    queue.addFirst(comp.functions[i]);
                }
            } else {
                queue.addFirst(function);
            }
        }

        public Optional<? extends PointFree<? extends Function<?, ?>>> doRewrite(PointFree<? extends Function<?, ?>> var1, PointFree<? extends Function<?, ?>> var2);
    }

    public static enum AppNest implements PointFreeRule
    {
        INSTANCE;


        @Override
        public <A> Optional<? extends PointFree<A>> rewrite(PointFree<A> expr) {
            if (expr instanceof Apply) {
                Apply applyFirst = (Apply)expr;
                PointFree pointFree = applyFirst.arg;
                if (pointFree instanceof Apply) {
                    Apply applySecond = (Apply)pointFree;
                    return Optional.of(Functions.app(this.compose(applyFirst.func, applySecond.func), applySecond.arg));
                }
            }
            return Optional.empty();
        }

        private <A, B, C> PointFree<Function<A, C>> compose(PointFree<? extends Function<?, ?>> first, PointFree<? extends Function<?, ?>> second) {
            if (first instanceof ProfunctorTransformer) {
                ProfunctorTransformer firstOptic = (ProfunctorTransformer)first;
                if (second instanceof ProfunctorTransformer) {
                    ProfunctorTransformer secondOptic = (ProfunctorTransformer)second;
                    return (PointFree)this.cap(firstOptic, secondOptic);
                }
            }
            return Functions.comp(first, second);
        }

        private <R, X, Y, S, T, A, B> R cap(ProfunctorTransformer<X, Y, ?, ?> first, ProfunctorTransformer<S, T, A, B> second) {
            ProfunctorTransformer<X, Y, ?, ?> firstCasted = first;
            return (R)Functions.profunctorTransformer(firstCasted.optic.compose(second.optic));
        }
    }

    public static enum LensAppId implements PointFreeRule
    {
        INSTANCE;


        @Override
        public <A> Optional<? extends PointFree<A>> rewrite(PointFree<A> expr) {
            if (expr instanceof Apply) {
                Apply apply = (Apply)expr;
                PointFree func = apply.func;
                if (func instanceof ProfunctorTransformer && Functions.isId(apply.arg)) {
                    return Optional.of(Functions.id(((Func)apply.type()).first()));
                }
            }
            return Optional.empty();
        }
    }

    public static enum BangEta implements PointFreeRule
    {
        INSTANCE;


        @Override
        public <A> Optional<? extends PointFree<A>> rewrite(PointFree<A> expr) {
            Func func;
            if (expr instanceof Bang) {
                return Optional.empty();
            }
            Type<A> type = expr.type();
            if (type instanceof Func && (func = (Func)type).second() instanceof EmptyPart) {
                return Optional.of(Functions.bang(func.first()));
            }
            return Optional.empty();
        }
    }
}

