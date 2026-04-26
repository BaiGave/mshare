/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.datafixers.types.templates;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.FamilyOptic;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.optics.profunctors.Profunctor;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.families.TypeFamily;
import com.mojang.datafixers.types.templates.Const;
import com.mojang.datafixers.types.templates.RecursivePoint;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import javax.annotation.Nullable;

public record Tag(String name, TypeTemplate element) implements TypeTemplate
{
    @Override
    public int size() {
        return this.element.size();
    }

    @Override
    public TypeFamily apply(final TypeFamily family) {
        return new TypeFamily(){

            @Override
            public Type<?> apply(int index) {
                return DSL.field(Tag.this.name, Tag.this.element.apply(family).apply(index));
            }
        };
    }

    @Override
    public <A, B> FamilyOptic<A, B> applyO(FamilyOptic<A, B> input, Type<A> aType, Type<B> bType) {
        return TypeFamily.familyOptic(i -> this.element.applyO(input, aType, bType).apply(i));
    }

    public <FT, FR> Either<TypeTemplate, Type.FieldNotFoundException> findFieldOrType(int index, @Nullable String name, Type<FT> type, Type<FR> resultType) {
        if (!Objects.equals(name, this.name)) {
            return Either.right(new Type.FieldNotFoundException("Names don't match"));
        }
        if (this.element instanceof Const) {
            Const c = (Const)this.element;
            if (Objects.equals(type, c.type())) {
                return Either.left(new Tag(name, new Const(resultType)));
            }
            return Either.right(new Type.FieldNotFoundException("don't match"));
        }
        if (Objects.equals(type, resultType)) {
            return Either.left(this);
        }
        if (type instanceof RecursivePoint.RecursivePointType && this.element instanceof RecursivePoint && ((RecursivePoint)this.element).index() == ((RecursivePoint.RecursivePointType)type).index()) {
            if (resultType instanceof RecursivePoint.RecursivePointType) {
                if (((RecursivePoint.RecursivePointType)resultType).index() == ((RecursivePoint)this.element).index()) {
                    return Either.left(this);
                }
            } else {
                return Either.left(DSL.constType(resultType));
            }
        }
        return Either.right(new Type.FieldNotFoundException("Recursive field"));
    }

    @Override
    public IntFunction<RewriteResult<?, ?>> hmap(TypeFamily family, IntFunction<RewriteResult<?, ?>> function) {
        return this.element.hmap(family, function);
    }

    @Override
    public String toString() {
        return "NameTag[" + this.name + ": " + String.valueOf(this.element) + "]";
    }

    public static final class TagType<A>
    extends Type<A> {
        protected final String name;
        protected final Type<A> element;

        public TagType(String name, Type<A> element) {
            this.name = name;
            this.element = element;
        }

        @Override
        public RewriteResult<A, ?> all(TypeRewriteRule rule, boolean recurse, boolean checkIndex) {
            return this.wrap(this.element.rewriteOrNop(rule));
        }

        private <B> RewriteResult<A, B> wrap(RewriteResult<A, B> instance) {
            if (instance.view().isNop()) {
                return instance;
            }
            TagType<B> output = DSL.field(this.name, instance.view().newType());
            return TagType.opticView(this, instance, new TypedOptic(Profunctor.Mu.TYPE_TOKEN, this, output, instance.view().type(), instance.view().newType(), Optics.id()));
        }

        @Override
        public Optional<RewriteResult<A, ?>> one(TypeRewriteRule rule) {
            Optional<RewriteResult<A, ?>> view = rule.rewrite(this.element);
            return view.map(this::wrap);
        }

        @Override
        public Type<?> updateMu(RecursiveTypeFamily newFamily) {
            return DSL.field(this.name, this.element.updateMu(newFamily));
        }

        @Override
        public TypeTemplate buildTemplate() {
            return DSL.field(this.name, this.element.template());
        }

        @Override
        protected Codec<A> buildCodec() {
            return ((MapCodec)this.element.codec().fieldOf(this.name)).codec();
        }

        public String toString() {
            return "Tag[\"" + this.name + "\", " + String.valueOf(this.element) + "]";
        }

        @Override
        public boolean equals(Object o, boolean ignoreRecursionPoints, boolean checkIndex) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            TagType tagType = (TagType)o;
            return Objects.equals(this.name, tagType.name) && this.element.equals(tagType.element, ignoreRecursionPoints, checkIndex);
        }

        public int hashCode() {
            int result = this.name.hashCode();
            result = 31 * result + this.element.hashCode();
            return result;
        }

        @Override
        public Optional<Type<?>> findFieldTypeOpt(String name) {
            if (Objects.equals(name, this.name)) {
                return Optional.of(this.element);
            }
            return Optional.empty();
        }

        @Override
        public Optional<A> point(DynamicOps<?> ops) {
            return this.element.point(ops);
        }

        @Override
        public <FT, FR> Either<TypedOptic<A, ?, FT, FR>, Type.FieldNotFoundException> findTypeInChildren(Type<FT> type, Type<FR> resultType, Type.TypeMatcher<FT, FR> matcher, boolean recurse) {
            return this.element.findType(type, resultType, matcher, recurse).mapLeft(this::wrapOptic);
        }

        private <B, FT, FR> TypedOptic<A, B, FT, FR> wrapOptic(TypedOptic<A, B, FT, FR> optic) {
            return optic.castOuter(DSL.field(this.name, optic.sType()), DSL.field(this.name, optic.tType()));
        }

        public String name() {
            return this.name;
        }

        public Type<A> element() {
            return this.element;
        }
    }
}

