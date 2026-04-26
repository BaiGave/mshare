/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.datafixers.types.templates;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.FamilyOptic;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.families.TypeFamily;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import java.util.function.IntFunction;
import javax.annotation.Nullable;

public record Check(String name, int index, TypeTemplate element) implements TypeTemplate
{
    @Override
    public int size() {
        return Math.max(this.index + 1, this.element.size());
    }

    @Override
    public TypeFamily apply(final TypeFamily family) {
        return new TypeFamily(){

            @Override
            public Type<?> apply(int index) {
                if (index < 0) {
                    throw new IndexOutOfBoundsException();
                }
                return new CheckType(Check.this.name, index, Check.this.index, Check.this.element.apply(family).apply(index));
            }
        };
    }

    @Override
    public <A, B> FamilyOptic<A, B> applyO(FamilyOptic<A, B> input, Type<A> aType, Type<B> bType) {
        return TypeFamily.familyOptic(i -> this.element.applyO(input, aType, bType).apply(i));
    }

    public <FT, FR> Either<TypeTemplate, Type.FieldNotFoundException> findFieldOrType(int index, @Nullable String name, Type<FT> type, Type<FR> resultType) {
        if (index == this.index) {
            return this.element.findFieldOrType(index, name, type, resultType);
        }
        return Either.right(new Type.FieldNotFoundException("Not a matching index"));
    }

    @Override
    public IntFunction<RewriteResult<?, ?>> hmap(TypeFamily family, IntFunction<RewriteResult<?, ?>> function) {
        return index -> {
            RewriteResult<?, ?> elementResult = this.element.hmap(family, function).apply(index);
            return this.cap(family, index, elementResult);
        };
    }

    private <A> RewriteResult<?, ?> cap(TypeFamily family, int index, RewriteResult<A, ?> elementResult) {
        return CheckType.fix((CheckType)this.apply(family).apply(index), elementResult);
    }

    @Override
    public String toString() {
        return "Tag[" + this.name + ", " + this.index + ": " + String.valueOf(this.element) + "]";
    }

    public static final class CheckType<A>
    extends Type<A> {
        private final String name;
        private final int index;
        private final int expectedIndex;
        private final Type<A> delegate;

        public CheckType(String name, int index, int expectedIndex, Type<A> delegate) {
            this.name = name;
            this.index = index;
            this.expectedIndex = expectedIndex;
            this.delegate = delegate;
        }

        @Override
        protected Codec<A> buildCodec() {
            return Codec.of(this.delegate.codec(), this::read);
        }

        private <T> DataResult<Pair<A, T>> read(DynamicOps<T> ops, T input) {
            if (this.index != this.expectedIndex) {
                return DataResult.error(() -> "Index mismatch: " + this.index + " != " + this.expectedIndex);
            }
            return this.delegate.codec().decode(ops, input);
        }

        public static <A, B> RewriteResult<A, ?> fix(CheckType<A> type, RewriteResult<A, B> instance) {
            if (instance.view().isNop()) {
                return RewriteResult.nop(type);
            }
            return CheckType.opticView(type, instance, CheckType.wrapOptic(type, TypedOptic.adapter(instance.view().type(), instance.view().newType())));
        }

        @Override
        public RewriteResult<A, ?> all(TypeRewriteRule rule, boolean recurse, boolean checkIndex) {
            if (checkIndex && this.index != this.expectedIndex) {
                return RewriteResult.nop(this);
            }
            return CheckType.fix(this, this.delegate.rewriteOrNop(rule));
        }

        @Override
        public Optional<RewriteResult<A, ?>> everywhere(TypeRewriteRule rule, PointFreeRule optimizationRule, boolean recurse, boolean checkIndex) {
            if (checkIndex && this.index != this.expectedIndex) {
                return Optional.empty();
            }
            return super.everywhere(rule, optimizationRule, recurse, checkIndex);
        }

        @Override
        public Optional<RewriteResult<A, ?>> one(TypeRewriteRule rule) {
            return rule.rewrite(this.delegate).map(view -> CheckType.fix(this, view));
        }

        @Override
        public Type<?> updateMu(RecursiveTypeFamily newFamily) {
            return new CheckType(this.name, this.index, this.expectedIndex, this.delegate.updateMu(newFamily));
        }

        @Override
        public TypeTemplate buildTemplate() {
            return DSL.check(this.name, this.expectedIndex, this.delegate.template());
        }

        @Override
        public Optional<TaggedChoice.TaggedChoiceType<?>> findChoiceType(String name, int index) {
            if (index == this.expectedIndex) {
                return this.delegate.findChoiceType(name, index);
            }
            return Optional.empty();
        }

        @Override
        public Optional<Type<?>> findCheckedType(int index) {
            if (index == this.expectedIndex) {
                return Optional.of(this.delegate);
            }
            return Optional.empty();
        }

        @Override
        public Optional<Type<?>> findFieldTypeOpt(String name) {
            if (this.index == this.expectedIndex) {
                return this.delegate.findFieldTypeOpt(name);
            }
            return Optional.empty();
        }

        @Override
        public Optional<A> point(DynamicOps<?> ops) {
            if (this.index == this.expectedIndex) {
                return this.delegate.point(ops);
            }
            return Optional.empty();
        }

        @Override
        public <FT, FR> Either<TypedOptic<A, ?, FT, FR>, Type.FieldNotFoundException> findTypeInChildren(Type<FT> type, Type<FR> resultType, Type.TypeMatcher<FT, FR> matcher, boolean recurse) {
            if (this.index != this.expectedIndex) {
                return Either.right(new Type.FieldNotFoundException("Incorrect index in CheckType"));
            }
            return this.delegate.findType(type, resultType, matcher, recurse).mapLeft(optic -> CheckType.wrapOptic(this, optic));
        }

        protected static <A, B, FT, FR> TypedOptic<A, B, FT, FR> wrapOptic(CheckType<A> type, TypedOptic<A, B, FT, FR> optic) {
            return optic.castOuter(type, new CheckType<B>(type.name, type.index, type.expectedIndex, optic.tType()));
        }

        public String toString() {
            return "TypeTag[" + this.index + "~" + this.expectedIndex + "][" + this.name + ": " + String.valueOf(this.delegate) + "]";
        }

        @Override
        public boolean equals(Object obj, boolean ignoreRecursionPoints, boolean checkIndex) {
            if (!(obj instanceof CheckType)) {
                return false;
            }
            CheckType type = (CheckType)obj;
            if (this.index == type.index && this.expectedIndex == type.expectedIndex) {
                if (!checkIndex) {
                    return true;
                }
                if (this.delegate.equals(type.delegate, ignoreRecursionPoints, checkIndex)) {
                    return true;
                }
            }
            return false;
        }

        public int hashCode() {
            int result = this.index;
            result = 31 * result + this.expectedIndex;
            result = 31 * result + this.delegate.hashCode();
            return result;
        }
    }
}

