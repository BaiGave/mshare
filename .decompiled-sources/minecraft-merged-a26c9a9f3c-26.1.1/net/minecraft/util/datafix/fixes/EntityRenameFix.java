/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;

public abstract class EntityRenameFix
extends DataFix {
    protected final String name;

    public EntityRenameFix(String name, Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
        this.name = name;
    }

    @Override
    public TypeRewriteRule makeRule() {
        TaggedChoice.TaggedChoiceType<?> oldType = this.getInputSchema().findChoiceType(References.ENTITY);
        TaggedChoice.TaggedChoiceType<?> newType = this.getOutputSchema().findChoiceType(References.ENTITY);
        Function<String, Type> patchedInputTypes = Util.memoize(name -> {
            Type<?> type = oldType.types().get(name);
            return ExtraDataFixUtils.patchSubType(type, oldType, newType);
        });
        return this.fixTypeEverywhere(this.name, oldType, newType, ops -> input -> {
            String oldName = (String)input.getFirst();
            Type oldEntityType = (Type)patchedInputTypes.apply(oldName);
            Pair<String, Typed<?>> newEntity = this.fix(oldName, this.getEntity(input.getSecond(), (DynamicOps<?>)ops, oldEntityType));
            Type<?> expectedType = newType.types().get(newEntity.getFirst());
            if (!expectedType.equals(newEntity.getSecond().getType(), true, true)) {
                throw new IllegalStateException(String.format(Locale.ROOT, "Dynamic type check failed: %s not equal to %s", expectedType, newEntity.getSecond().getType()));
            }
            return Pair.of(newEntity.getFirst(), newEntity.getSecond().getValue());
        });
    }

    private <A> Typed<A> getEntity(Object input, DynamicOps<?> ops, Type<A> oldEntityType) {
        return new Typed<Object>(oldEntityType, ops, input);
    }

    protected abstract Pair<String, Typed<?>> fix(String var1, Typed<?> var2);
}

