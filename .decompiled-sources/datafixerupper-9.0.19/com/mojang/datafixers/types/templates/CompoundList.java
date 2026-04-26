/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.datafixers.types.templates;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.FamilyOptic;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.optics.profunctors.Cartesian;
import com.mojang.datafixers.optics.profunctors.TraversalP;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.families.TypeFamily;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import javax.annotation.Nullable;

public record CompoundList(TypeTemplate key, TypeTemplate element) implements TypeTemplate
{
    @Override
    public int size() {
        return Math.max(this.key.size(), this.element.size());
    }

    @Override
    public TypeFamily apply(TypeFamily family) {
        return index -> DSL.compoundList(this.key.apply(family).apply(index), this.element.apply(family).apply(index));
    }

    @Override
    public <A, B> FamilyOptic<A, B> applyO(FamilyOptic<A, B> input, Type<A> aType, Type<B> bType) {
        return TypeFamily.familyOptic(i -> this.cap(this.element.applyO(input, aType, bType).apply(i)));
    }

    private <S, T, A, B> TypedOptic<?, ?, A, B> cap(TypedOptic<S, T, A, B> concreteOptic) {
        Type<Pair<String, S>> sTypeEntry = DSL.and(DSL.string(), concreteOptic.sType());
        Type<Pair<String, T>> tTypeEntry = DSL.and(DSL.string(), concreteOptic.tType());
        return new TypedOptic(TraversalP.Mu.TYPE_TOKEN, DSL.compoundList(concreteOptic.sType()), DSL.compoundList(concreteOptic.tType()), sTypeEntry, tTypeEntry, Optics.listTraversal()).compose(new TypedOptic<Pair<String, S>, Pair<String, T>, S, T>(Cartesian.Mu.TYPE_TOKEN, sTypeEntry, tTypeEntry, concreteOptic.sType(), concreteOptic.tType(), Optics.proj2())).compose(concreteOptic);
    }

    public <FT, FR> Either<TypeTemplate, Type.FieldNotFoundException> findFieldOrType(int index, @Nullable String name, Type<FT> type, Type<FR> resultType) {
        return this.element.findFieldOrType(index, name, type, resultType).mapLeft(element1 -> new CompoundList(this.key, (TypeTemplate)element1));
    }

    @Override
    public IntFunction<RewriteResult<?, ?>> hmap(TypeFamily family, IntFunction<RewriteResult<?, ?>> function) {
        return i -> {
            RewriteResult<?, ?> f1 = this.key.hmap(family, function).apply(i);
            RewriteResult<?, ?> f2 = this.element.hmap(family, function).apply(i);
            return this.cap(this.apply(family).apply(i), f1, f2);
        };
    }

    private <L, R> RewriteResult<?, ?> cap(Type<?> type, RewriteResult<L, ?> f1, RewriteResult<R, ?> f2) {
        return ((CompoundListType)type).mergeViews(f1, f2);
    }

    @Override
    public String toString() {
        return "CompoundList[" + String.valueOf(this.element) + "]";
    }

    public static final class CompoundListType<K, V>
    extends Type<List<Pair<K, V>>> {
        protected final Type<K> key;
        protected final Type<V> element;

        public CompoundListType(Type<K> key, Type<V> element) {
            this.key = key;
            this.element = element;
        }

        @Override
        public RewriteResult<List<Pair<K, V>>, ?> all(TypeRewriteRule rule, boolean recurse, boolean checkIndex) {
            return this.mergeViews(this.key.rewriteOrNop(rule), this.element.rewriteOrNop(rule));
        }

        public <K2, V2> RewriteResult<List<Pair<K, V>>, ?> mergeViews(RewriteResult<K, K2> leftView, RewriteResult<V, V2> rightView) {
            RewriteResult<List<Pair<K, V>>, List<Pair<K2, V>>> v1 = CompoundListType.fixKeys(this, this.key, this.element, leftView);
            RewriteResult<List<Pair<K, V>>, List<Pair<K, V2>>> v2 = CompoundListType.fixValues(v1.view().newType(), leftView.view().newType(), this.element, rightView);
            return v2.compose(v1);
        }

        @Override
        public Optional<RewriteResult<List<Pair<K, V>>, ?>> one(TypeRewriteRule rule) {
            return DataFixUtils.or(rule.rewrite(this.key).map(v -> CompoundListType.fixKeys(this, this.key, this.element, v)), () -> rule.rewrite(this.element).map(v -> CompoundListType.fixValues(this, this.key, this.element, v)));
        }

        private static <K, V, K2> RewriteResult<List<Pair<K, V>>, List<Pair<K2, V>>> fixKeys(Type<List<Pair<K, V>>> type, Type<K> first, Type<V> second, RewriteResult<K, K2> view) {
            return CompoundListType.opticView(type, view, TypedOptic.compoundListKeys(first, view.view().newType(), second));
        }

        private static <K, V, V2> RewriteResult<List<Pair<K, V>>, List<Pair<K, V2>>> fixValues(Type<List<Pair<K, V>>> type, Type<K> first, Type<V> second, RewriteResult<V, V2> view) {
            return CompoundListType.opticView(type, view, TypedOptic.compoundListElements(first, second, view.view().newType()));
        }

        @Override
        public Type<?> updateMu(RecursiveTypeFamily newFamily) {
            return DSL.compoundList(this.key.updateMu(newFamily), this.element.updateMu(newFamily));
        }

        @Override
        public TypeTemplate buildTemplate() {
            return new CompoundList(this.key.template(), this.element.template());
        }

        @Override
        public Optional<List<Pair<K, V>>> point(DynamicOps<?> ops) {
            return Optional.of(ImmutableList.of());
        }

        @Override
        public <FT, FR> Either<TypedOptic<List<Pair<K, V>>, ?, FT, FR>, Type.FieldNotFoundException> findTypeInChildren(Type<FT> type, Type<FR> resultType, Type.TypeMatcher<FT, FR> matcher, boolean recurse) {
            Either<TypedOptic<K, ?, FT, FR>, Type.FieldNotFoundException> firstFieldLens = this.key.findType(type, resultType, matcher, recurse);
            return firstFieldLens.map(this::capLeft, r -> {
                Either secondFieldLens = this.element.findType(type, resultType, matcher, recurse);
                return secondFieldLens.mapLeft(this::capRight);
            });
        }

        private <FT, K2, FR> Either<TypedOptic<List<Pair<K, V>>, ?, FT, FR>, Type.FieldNotFoundException> capLeft(TypedOptic<K, K2, FT, FR> optic) {
            return Either.left(TypedOptic.compoundListKeys(optic.sType(), optic.tType(), this.element).compose(optic));
        }

        private <FT, V2, FR> TypedOptic<List<Pair<K, V>>, ?, FT, FR> capRight(TypedOptic<V, V2, FT, FR> optic) {
            return TypedOptic.compoundListElements(this.key, optic.sType(), optic.tType()).compose(optic);
        }

        @Override
        protected Codec<List<Pair<K, V>>> buildCodec() {
            return Codec.compoundList(this.key.codec(), this.element.codec());
        }

        public String toString() {
            return "CompoundList[" + String.valueOf(this.key) + " -> " + String.valueOf(this.element) + "]";
        }

        @Override
        public boolean equals(Object obj, boolean ignoreRecursionPoints, boolean checkIndex) {
            if (!(obj instanceof CompoundListType)) {
                return false;
            }
            CompoundListType that = (CompoundListType)obj;
            return this.key.equals(that.key, ignoreRecursionPoints, checkIndex) && this.element.equals(that.element, ignoreRecursionPoints, checkIndex);
        }

        public int hashCode() {
            int result = this.key.hashCode();
            result = 31 * result + this.element.hashCode();
            return result;
        }

        public Type<K> getKey() {
            return this.key;
        }

        public Type<V> getElement() {
            return this.element;
        }
    }
}

