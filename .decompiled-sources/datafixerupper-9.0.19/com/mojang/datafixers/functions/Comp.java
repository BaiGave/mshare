/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.datafixers.functions;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.functions.PointFree;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.types.Func;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DynamicOps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

final class Comp<A, B>
extends PointFree<Function<A, B>> {
    protected final PointFree<? extends Function<?, ?>>[] functions;
    private final Type<Function<A, B>> type;

    protected Comp(PointFree<? extends Function<?, ?>> ... functions) {
        this.functions = functions;
        PointFree<Function<?, ?>> first = functions[0];
        PointFree<Function<?, ?>> last = functions[functions.length - 1];
        this.type = DSL.func(((Func)last.type()).first(), ((Func)first.type()).second());
    }

    protected Comp(PointFree<? extends Function<?, ?>>[] functions, Type<Function<A, B>> type) {
        this.functions = functions;
        this.type = type;
    }

    @Override
    public Type<Function<A, B>> type() {
        return this.type;
    }

    @Override
    public String toString(int level) {
        String content = Arrays.stream(this.functions).map(function -> function.toString(level + 1)).collect(Collectors.joining("\n" + Comp.indent(level + 1) + "\u25e6\n" + Comp.indent(level + 1)));
        return "(\n" + Comp.indent(level + 1) + content + "\n" + Comp.indent(level) + ")";
    }

    @Override
    public Optional<? extends PointFree<Function<A, B>>> all(PointFreeRule rule) {
        ArrayList newFunctions = new ArrayList(this.functions.length);
        boolean rewritten = false;
        for (PointFree<Function<?, ?>> pointFree : this.functions) {
            PointFree<? extends Function<?, ?>> rewrite = rule.rewriteOrNop(pointFree);
            if (rewrite != pointFree) {
                rewritten = true;
                if (rewrite instanceof Comp) {
                    Comp comp = (Comp)rewrite;
                    Collections.addAll(newFunctions, comp.functions);
                    continue;
                }
                newFunctions.add(rewrite);
                continue;
            }
            newFunctions.add(pointFree);
        }
        return Optional.of(rewritten ? new Comp<A, B>((PointFree[])newFunctions.toArray(PointFree[]::new), this.type) : this);
    }

    @Override
    public Optional<? extends PointFree<Function<A, B>>> one(PointFreeRule rule) {
        for (int i = 0; i < this.functions.length; ++i) {
            PointFree<? extends Function<?, ?>>[] newFunctions;
            PointFree<Function<?, ?>> function = this.functions[i];
            Optional<PointFree<Function<?, ?>>> rewrite = rule.rewrite(function);
            if (!rewrite.isPresent()) continue;
            PointFree<? extends Function<?, ?>> pointFree = rewrite.get();
            if (pointFree instanceof Comp) {
                Comp comp = (Comp)pointFree;
                newFunctions = new PointFree[this.functions.length - 1 + comp.functions.length];
                System.arraycopy(this.functions, 0, newFunctions, 0, i);
                System.arraycopy(comp.functions, 0, newFunctions, i, comp.functions.length);
                System.arraycopy(this.functions, i + 1, newFunctions, i + comp.functions.length, this.functions.length - i - 1);
                return Optional.of(new Comp<A, B>(newFunctions, this.type));
            }
            newFunctions = Arrays.copyOf(this.functions, this.functions.length);
            newFunctions[i] = rewrite.get();
            return Optional.of(new Comp<A, B>(newFunctions, this.type));
        }
        return Optional.empty();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Comp comp = (Comp)o;
        return Arrays.equals(this.functions, comp.functions);
    }

    public int hashCode() {
        return Arrays.hashCode(this.functions);
    }

    @Override
    public Function<DynamicOps<?>, Function<A, B>> eval() {
        return ops -> input -> {
            Object value = input;
            for (int i = this.functions.length - 1; i >= 0; --i) {
                PointFree<Function<?, ?>> f = this.functions[i];
                value = Comp.applyUnchecked(f.evalCached().apply((DynamicOps<?>)ops), value);
            }
            return value;
        };
    }

    private static <A, B> B applyUnchecked(Function<A, B> function, Object input) {
        return function.apply(input);
    }
}

