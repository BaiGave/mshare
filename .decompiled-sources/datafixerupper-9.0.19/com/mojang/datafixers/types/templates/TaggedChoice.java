/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.datafixers.types.templates;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.FamilyOptic;
import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.View;
import com.mojang.datafixers.functions.Functions;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.optics.Affine;
import com.mojang.datafixers.optics.Lens;
import com.mojang.datafixers.optics.Optic;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.optics.Traversal;
import com.mojang.datafixers.optics.profunctors.AffineP;
import com.mojang.datafixers.optics.profunctors.Cartesian;
import com.mojang.datafixers.optics.profunctors.TraversalP;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.families.TypeFamily;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import javax.annotation.Nullable;

public final class TaggedChoice<K>
implements TypeTemplate {
    private final String name;
    private final Type<K> keyType;
    private final Object2ObjectMap<K, TypeTemplate> templates;
    private final Map<Pair<TypeFamily, Integer>, Type<?>> types = Maps.newConcurrentMap();
    private final int size;

    public TaggedChoice(String name, Type<K> keyType, Object2ObjectMap<K, TypeTemplate> templates) {
        this.name = name;
        this.keyType = keyType;
        this.templates = templates;
        this.size = templates.values().stream().mapToInt(TypeTemplate::size).max().orElse(0);
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public TypeFamily apply(TypeFamily family) {
        return index -> this.types.computeIfAbsent(Pair.of(family, index), key -> {
            Object2ObjectOpenHashMap types = new Object2ObjectOpenHashMap(this.templates.size());
            for (Map.Entry entry : Object2ObjectMaps.fastIterable(this.templates)) {
                types.put(entry.getKey(), ((TypeTemplate)entry.getValue()).apply((TypeFamily)key.getFirst()).apply((Integer)key.getSecond()));
            }
            return DSL.taggedChoiceType(this.name, this.keyType, types);
        });
    }

    @Override
    public <A, B> FamilyOptic<A, B> applyO(FamilyOptic<A, B> input, Type<A> aType, Type<B> bType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <A, B> Either<TypeTemplate, Type.FieldNotFoundException> findFieldOrType(int index, @Nullable String name, Type<A> type, Type<B> resultType) {
        return Either.right(new Type.FieldNotFoundException("Not implemented"));
    }

    @Override
    public IntFunction<RewriteResult<?, ?>> hmap(TypeFamily family, IntFunction<RewriteResult<?, ?>> function) {
        return index -> {
            RewriteResult result = RewriteResult.nop((TaggedChoiceType)this.apply(family).apply(index));
            for (Map.Entry entry : this.templates.entrySet()) {
                RewriteResult<?, ?> elementResult = ((TypeTemplate)entry.getValue()).hmap(family, function).apply(index);
                result = TaggedChoiceType.elementResult(entry.getKey(), (TaggedChoiceType)result.view().newType(), elementResult).compose(result);
            }
            return result;
        };
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TaggedChoice)) {
            return false;
        }
        TaggedChoice other = (TaggedChoice)obj;
        return Objects.equals(this.name, other.name) && Objects.equals(this.keyType, other.keyType) && Objects.equals(this.templates, other.templates);
    }

    public int hashCode() {
        int result = this.name.hashCode();
        result = 31 * result + this.keyType.hashCode();
        result = 31 * result + this.templates.hashCode();
        return result;
    }

    public String toString() {
        return "TaggedChoice[" + this.name + ", " + Joiner.on(", ").withKeyValueSeparator(" -> ").join(this.templates) + "]";
    }

    public static final class TaggedChoiceType<K>
    extends Type<Pair<K, ?>> {
        private final String name;
        private final Type<K> keyType;
        protected final Object2ObjectMap<K, Type<?>> types;
        private final int hashCode;

        public TaggedChoiceType(String name, Type<K> keyType, Object2ObjectMap<K, Type<?>> types) {
            this.name = name;
            this.keyType = keyType;
            this.types = types;
            this.hashCode = Objects.hash(name, keyType, types);
        }

        @Override
        public RewriteResult<Pair<K, ?>, ?> all(TypeRewriteRule rule, boolean recurse, boolean checkIndex) {
            Object2ObjectOpenHashMap results = new Object2ObjectOpenHashMap(this.types.size());
            for (Map.Entry entry : Object2ObjectMaps.fastIterable(this.types)) {
                Optional result = rule.rewrite((Type)entry.getValue());
                if (!result.isPresent() || result.get().view().isNop()) continue;
                results.put(entry.getKey(), result.get());
            }
            if (results.isEmpty()) {
                return RewriteResult.nop(this);
            }
            if (results.size() == 1) {
                Map.Entry entry = (Map.Entry)results.entrySet().iterator().next();
                return TaggedChoiceType.elementResult(entry.getKey(), this, (RewriteResult)entry.getValue());
            }
            Object2ObjectOpenHashMap newTypes = new Object2ObjectOpenHashMap(this.types);
            BitSet bitSet = new BitSet();
            for (Map.Entry entry : Object2ObjectMaps.fastIterable(results)) {
                newTypes.put(entry.getKey(), ((RewriteResult)entry.getValue()).view().newType());
                bitSet.or(((RewriteResult)entry.getValue()).recData());
            }
            return RewriteResult.create(View.create(Functions.fun("TaggedChoiceTypeRewriteResult " + results.size(), new RewriteFunc(results), this, DSL.taggedChoiceType(this.name, this.keyType, newTypes))), bitSet);
        }

        public static <K, FT, FR> RewriteResult<Pair<K, ?>, Pair<K, ?>> elementResult(K key, TaggedChoiceType<K> type, RewriteResult<FT, FR> result) {
            return TaggedChoiceType.opticView(type, result, TypedOptic.tagged(type, key, result.view().type(), result.view().newType()));
        }

        @Override
        public Optional<RewriteResult<Pair<K, ?>, ?>> one(TypeRewriteRule rule) {
            for (Map.Entry entry : this.types.entrySet()) {
                Optional elementResult = rule.rewrite((Type)entry.getValue());
                if (!elementResult.isPresent()) continue;
                return Optional.of(TaggedChoiceType.elementResult(entry.getKey(), this, elementResult.get()));
            }
            return Optional.empty();
        }

        @Override
        public Type<?> updateMu(RecursiveTypeFamily newFamily) {
            Object2ObjectOpenHashMap newTypes = new Object2ObjectOpenHashMap(this.types.size());
            for (Object2ObjectMap.Entry entry : Object2ObjectMaps.fastIterable(this.types)) {
                newTypes.put(entry.getKey(), ((Type)entry.getValue()).updateMu(newFamily));
            }
            return DSL.taggedChoiceType(this.name, this.keyType, newTypes);
        }

        @Override
        public TypeTemplate buildTemplate() {
            Object2ObjectOpenHashMap templates = new Object2ObjectOpenHashMap(this.types.size());
            for (Object2ObjectMap.Entry entry : Object2ObjectMaps.fastIterable(this.types)) {
                templates.put(entry.getKey(), ((Type)entry.getValue()).template());
            }
            return DSL.taggedChoice(this.name, this.keyType, templates);
        }

        @Override
        protected Codec<Pair<K, ?>> buildCodec() {
            return this.keyType.codec().partialDispatch(this.name, pair -> DataResult.success(pair.getFirst()), key -> this.getMapCodec(key).map(codec -> TaggedChoiceType.asEntryPair(key, codec)));
        }

        private static <K, V> MapCodec<Pair<K, V>> asEntryPair(K key, MapCodec<V> valueCodec) {
            return valueCodec.xmap(value -> Pair.of(key, value), Pair::getSecond);
        }

        private DataResult<? extends MapCodec<?>> getMapCodec(K key) {
            return Optional.ofNullable((Type)this.types.get(key)).map(type -> DataResult.success(MapCodec.assumeMapUnsafe(type.codec()))).orElseGet(() -> DataResult.error(() -> "Unsupported key: " + String.valueOf(key)));
        }

        @Override
        public Optional<Type<?>> findFieldTypeOpt(String name) {
            return this.types.values().stream().map(t -> t.findFieldTypeOpt(name)).filter(Optional::isPresent).findFirst().flatMap(Function.identity());
        }

        @Override
        public Optional<Pair<K, ?>> point(DynamicOps<?> ops) {
            return this.types.entrySet().stream().map(e -> ((Type)e.getValue()).point(ops).map(value -> Pair.of(e.getKey(), value))).filter(Optional::isPresent).findFirst().flatMap(Function.identity()).map(p -> p);
        }

        public Optional<Typed<Pair<K, ?>>> point(DynamicOps<?> ops, K key, Object value) {
            if (!this.types.containsKey(key)) {
                return Optional.empty();
            }
            return Optional.of(new Typed<Pair<K, Object>>(this, ops, Pair.of(key, value)));
        }

        @Override
        public <FT, FR> Either<TypedOptic<Pair<K, ?>, ?, FT, FR>, Type.FieldNotFoundException> findTypeInChildren(Type<FT> type, Type<FR> resultType, Type.TypeMatcher<FT, FR> matcher, boolean recurse) {
            Optic optic;
            TypeToken<Cartesian.Mu> bound;
            final Map optics = this.types.entrySet().stream().map(e -> Pair.of(e.getKey(), ((Type)e.getValue()).findType(type, resultType, matcher, recurse))).filter(e -> ((Either)e.getSecond()).left().isPresent()).map(e -> e.mapSecond(o -> (TypedOptic)o.left().get())).collect(Pair.toMap());
            if (optics.isEmpty()) {
                return Either.right(new Type.FieldNotFoundException("Not found in any choices"));
            }
            if (optics.size() == 1) {
                Map.Entry entry = optics.entrySet().iterator().next();
                return Either.left(this.cap(this, entry.getKey(), (TypedOptic)entry.getValue()));
            }
            HashSet<TypeToken<? extends K1>> bounds = Sets.newHashSet();
            optics.values().forEach(o -> bounds.addAll(o.bounds()));
            if (TypedOptic.instanceOf(bounds, Cartesian.Mu.TYPE_TOKEN) && optics.size() == this.types.size()) {
                bound = Cartesian.Mu.TYPE_TOKEN;
                optic = new Lens<Pair<K, ?>, Pair<K, ?>, FT, FR>(){

                    @Override
                    public FT view(Pair<K, ?> s) {
                        TypedOptic optic = (TypedOptic)optics.get(s.getFirst());
                        return this.capView(s, optic);
                    }

                    private <S, T> FT capView(Pair<K, ?> s, TypedOptic<S, T, FT, FR> optic) {
                        return Optics.toLens(optic.upCast(Cartesian.Mu.TYPE_TOKEN).orElseThrow(IllegalArgumentException::new)).view(s.getSecond());
                    }

                    @Override
                    public Pair<K, ?> update(FR b, Pair<K, ?> s) {
                        TypedOptic optic = (TypedOptic)optics.get(s.getFirst());
                        return this.capUpdate(b, s, optic);
                    }

                    private <S, T> Pair<K, ?> capUpdate(FR b, Pair<K, ?> s, TypedOptic<S, T, FT, FR> optic) {
                        return Pair.of(s.getFirst(), Optics.toLens(optic.upCast(Cartesian.Mu.TYPE_TOKEN).orElseThrow(IllegalArgumentException::new)).update(b, s.getSecond()));
                    }
                };
            } else if (TypedOptic.instanceOf(bounds, AffineP.Mu.TYPE_TOKEN)) {
                bound = AffineP.Mu.TYPE_TOKEN;
                optic = new Affine<Pair<K, ?>, Pair<K, ?>, FT, FR>(){

                    @Override
                    public Either<Pair<K, ?>, FT> preview(Pair<K, ?> s) {
                        if (!optics.containsKey(s.getFirst())) {
                            return Either.left(s);
                        }
                        TypedOptic optic = (TypedOptic)optics.get(s.getFirst());
                        return this.capPreview(s, optic);
                    }

                    private <S, T> Either<Pair<K, ?>, FT> capPreview(Pair<K, ?> s, TypedOptic<S, T, FT, FR> optic) {
                        return Optics.toAffine(optic.upCast(AffineP.Mu.TYPE_TOKEN).orElseThrow(IllegalArgumentException::new)).preview(s.getSecond()).mapLeft(t -> Pair.of(s.getFirst(), t));
                    }

                    @Override
                    public Pair<K, ?> set(FR b, Pair<K, ?> s) {
                        if (!optics.containsKey(s.getFirst())) {
                            return s;
                        }
                        TypedOptic optic = (TypedOptic)optics.get(s.getFirst());
                        return this.capSet(b, s, optic);
                    }

                    private <S, T> Pair<K, ?> capSet(FR b, Pair<K, ?> s, TypedOptic<S, T, FT, FR> optic) {
                        return Pair.of(s.getFirst(), Optics.toAffine(optic.upCast(AffineP.Mu.TYPE_TOKEN).orElseThrow(IllegalArgumentException::new)).set(b, s.getSecond()));
                    }
                };
            } else if (TypedOptic.instanceOf(bounds, TraversalP.Mu.TYPE_TOKEN)) {
                bound = TraversalP.Mu.TYPE_TOKEN;
                optic = new Traversal<Pair<K, ?>, Pair<K, ?>, FT, FR>(){

                    @Override
                    public <F extends K1> FunctionType<Pair<K, ?>, App<F, Pair<K, ?>>> wander(Applicative<F, ?> applicative, FunctionType<FT, App<F, FR>> input) {
                        return pair -> {
                            if (!optics.containsKey(pair.getFirst())) {
                                return applicative.point(pair);
                            }
                            TypedOptic optic = (TypedOptic)optics.get(pair.getFirst());
                            return this.capTraversal(applicative, input, (Pair)pair, optic);
                        };
                    }

                    private <S, T, F extends K1> App<F, Pair<K, ?>> capTraversal(Applicative<F, ?> applicative, FunctionType<FT, App<F, FR>> input, Pair<K, ?> pair, TypedOptic<S, T, FT, FR> optic) {
                        Traversal traversal = Optics.toTraversal(optic.upCast(TraversalP.Mu.TYPE_TOKEN).orElseThrow(IllegalArgumentException::new));
                        return applicative.ap(value -> Pair.of(pair.getFirst(), value), traversal.wander(applicative, input).apply(pair.getSecond()));
                    }
                };
            } else {
                throw new IllegalStateException("Could not merge TaggedChoiceType optics, unknown bound: " + Arrays.toString(bounds.toArray()));
            }
            Object2ObjectOpenHashMap newTypes = new Object2ObjectOpenHashMap(this.types);
            for (Object2ObjectMap.Entry entry : Object2ObjectMaps.fastIterable(newTypes)) {
                TypedOptic typeOptic = (TypedOptic)optics.get(entry.getKey());
                if (typeOptic == null) continue;
                entry.setValue(typeOptic.tType());
            }
            return Either.left(new TypedOptic(bound, this, DSL.taggedChoiceType(this.name, this.keyType, newTypes), type, resultType, optic));
        }

        private <S, T, FT, FR> TypedOptic<Pair<K, ?>, Pair<K, ?>, FT, FR> cap(TaggedChoiceType<K> choiceType, K key, TypedOptic<S, T, FT, FR> optic) {
            return TypedOptic.tagged(choiceType, key, optic.sType(), optic.tType()).compose(optic);
        }

        @Override
        public Optional<TaggedChoiceType<?>> findChoiceType(String name, int index) {
            if (Objects.equals(name, this.name)) {
                return Optional.of(this);
            }
            return Optional.empty();
        }

        @Override
        public Optional<Type<?>> findCheckedType(int index) {
            return this.types.values().stream().map(type -> type.findCheckedType(index)).filter(Optional::isPresent).findFirst().flatMap(Function.identity());
        }

        @Override
        public boolean equals(Object obj, boolean ignoreRecursionPoints, boolean checkIndex) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TaggedChoiceType)) {
                return false;
            }
            TaggedChoiceType other = (TaggedChoiceType)obj;
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            if (!this.keyType.equals(other.keyType, ignoreRecursionPoints, checkIndex)) {
                return false;
            }
            if (this.types.size() != other.types.size()) {
                return false;
            }
            for (Map.Entry entry : this.types.entrySet()) {
                if (((Type)entry.getValue()).equals(other.types.get(entry.getKey()), ignoreRecursionPoints, checkIndex)) continue;
                return false;
            }
            return true;
        }

        public int hashCode() {
            return this.hashCode;
        }

        public String toString() {
            return "TaggedChoiceType[" + this.name + ", " + Joiner.on(", \n").withKeyValueSeparator(" -> ").join(this.types) + "]\n";
        }

        public String getName() {
            return this.name;
        }

        public Type<K> getKeyType() {
            return this.keyType;
        }

        public boolean hasType(K key) {
            return this.types.containsKey(key);
        }

        public Map<K, Type<?>> types() {
            return this.types;
        }

        private static final class RewriteFunc<K>
        implements Function<DynamicOps<?>, Function<Pair<K, ?>, Pair<K, ?>>> {
            private final Map<K, ? extends RewriteResult<?, ?>> results;

            public RewriteFunc(Map<K, ? extends RewriteResult<?, ?>> results) {
                this.results = results;
            }

            @Override
            public FunctionType<Pair<K, ?>, Pair<K, ?>> apply(DynamicOps<?> ops) {
                return input -> {
                    RewriteResult<?, ?> result = this.results.get(input.getFirst());
                    if (result == null) {
                        return input;
                    }
                    return this.capRuleApply(ops, (Pair<K, ?>)input, result);
                };
            }

            private <A, B> Pair<K, B> capRuleApply(DynamicOps<?> ops, Pair<K, ?> input, RewriteResult<A, B> result) {
                return input.mapSecond(v -> result.view().function().evalCached().apply(ops).apply(v));
            }

            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || this.getClass() != o.getClass()) {
                    return false;
                }
                RewriteFunc that = (RewriteFunc)o;
                return Objects.equals(this.results, that.results);
            }

            public int hashCode() {
                return this.results.hashCode();
            }
        }
    }
}

