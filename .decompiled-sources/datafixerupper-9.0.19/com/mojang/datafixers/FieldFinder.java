/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.datafixers;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.optics.profunctors.Cartesian;
import com.mojang.datafixers.optics.profunctors.Profunctor;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.Tag;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import javax.annotation.Nullable;

public final class FieldFinder<FT>
implements OpticFinder<FT> {
    @Nullable
    private final String name;
    private final Type<FT> type;

    public FieldFinder(@Nullable String name, Type<FT> type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public Type<FT> type() {
        return this.type;
    }

    @Override
    public <A, FR> Either<TypedOptic<A, ?, FT, FR>, Type.FieldNotFoundException> findType(Type<A> containerType, Type<FR> resultType, boolean recurse) {
        return containerType.findTypeCached(this.type, resultType, new Matcher<FT, FR>(this.name, this.type, resultType), recurse);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FieldFinder)) {
            return false;
        }
        FieldFinder that = (FieldFinder)o;
        return Objects.equals(this.name, that.name) && Objects.equals(this.type, that.type);
    }

    public int hashCode() {
        int result = this.name != null ? this.name.hashCode() : 0;
        result = 31 * result + this.type.hashCode();
        return result;
    }

    private static final class Matcher<FT, FR>
    implements Type.TypeMatcher<FT, FR> {
        private final Type<FR> resultType;
        @Nullable
        private final String name;
        private final Type<FT> type;

        public Matcher(@Nullable String name, Type<FT> type, Type<FR> resultType) {
            this.resultType = resultType;
            this.name = name;
            this.type = type;
        }

        @Override
        public <S> Either<TypedOptic<S, ?, FT, FR>, Type.FieldNotFoundException> match(Type<S> targetType) {
            TaggedChoice.TaggedChoiceType choiceType;
            if (this.name == null && this.type.equals(targetType, true, false)) {
                return Either.left(new TypedOptic<S, FR, S, FR>(Profunctor.Mu.TYPE_TOKEN, targetType, this.resultType, targetType, this.resultType, Optics.id()));
            }
            if (targetType instanceof Tag.TagType) {
                Tag.TagType tagType = (Tag.TagType)targetType;
                if (!Objects.equals(tagType.name(), this.name)) {
                    return Either.right(new Type.FieldNotFoundException(String.format("Not found: \"%s\" (in type: %s)", this.name, targetType)));
                }
                if (!Objects.equals(this.type, tagType.element())) {
                    return Either.right(new Type.FieldNotFoundException(String.format("Type error for field \"%s\": expected type: %s, actual type: %s)", this.name, this.type, tagType.element())));
                }
                return Either.left(new TypedOptic(Profunctor.Mu.TYPE_TOKEN, tagType, DSL.field(tagType.name(), this.resultType), this.type, this.resultType, Optics.id()));
            }
            if (targetType instanceof TaggedChoice.TaggedChoiceType && Objects.equals(this.name, (choiceType = (TaggedChoice.TaggedChoiceType)targetType).getName())) {
                if (!Objects.equals(this.type, choiceType.getKeyType())) {
                    return Either.right(new Type.FieldNotFoundException(String.format("Type error for field \"%s\": expected type: %s, actual type: %s)", this.name, this.type, choiceType.getKeyType())));
                }
                if (!Objects.equals(this.type, this.resultType)) {
                    return Either.right(new Type.FieldNotFoundException("TaggedChoiceType key type change is unsupported."));
                }
                return Either.left(this.capChoice(choiceType));
            }
            return Either.right(new Type.Continue());
        }

        private <V> TypedOptic<Pair<FT, V>, ?, FT, FT> capChoice(Type<?> choiceType) {
            return new TypedOptic(Cartesian.Mu.TYPE_TOKEN, choiceType, choiceType, this.type, this.type, Optics.proj1());
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            Matcher matcher = (Matcher)o;
            return Objects.equals(this.resultType, matcher.resultType) && Objects.equals(this.name, matcher.name) && Objects.equals(this.type, matcher.type);
        }

        public int hashCode() {
            int result = this.resultType.hashCode();
            result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
            result = 31 * result + this.type.hashCode();
            return result;
        }
    }
}

