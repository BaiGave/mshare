/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.function.Function;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;

public class FixProjectileStoredItem
extends DataFix {
    private static final String EMPTY_POTION = "minecraft:empty";

    public FixProjectileStoredItem(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> inputEntityType = this.getInputSchema().getType(References.ENTITY);
        Type<?> outputEntityType = this.getOutputSchema().getType(References.ENTITY);
        return this.fixTypeEverywhereTyped("Fix AbstractArrow item type", inputEntityType, outputEntityType, ExtraDataFixUtils.chainAllFilters(this.fixChoice("minecraft:trident", FixProjectileStoredItem::castUnchecked), this.fixChoice("minecraft:arrow", FixProjectileStoredItem::fixArrow), this.fixChoice("minecraft:spectral_arrow", FixProjectileStoredItem::fixSpectralArrow)));
    }

    private Function<Typed<?>, Typed<?>> fixChoice(String entityName, SubFixer<?> fixer) {
        Type<?> inputEntityChoiceType = this.getInputSchema().getChoiceType(References.ENTITY, entityName);
        Type<?> outputEntityChoiceType = this.getOutputSchema().getChoiceType(References.ENTITY, entityName);
        return FixProjectileStoredItem.fixChoiceCap(entityName, fixer, inputEntityChoiceType, outputEntityChoiceType);
    }

    private static <T> Function<Typed<?>, Typed<?>> fixChoiceCap(String entityName, SubFixer<?> fixer, Type<?> inputEntityChoiceType, Type<T> outputEntityChoiceType) {
        OpticFinder<?> entityF = DSL.namedChoice(entityName, inputEntityChoiceType);
        SubFixer<?> typedFixer = fixer;
        return input -> input.updateTyped(entityF, outputEntityChoiceType, typed -> typedFixer.fix((Typed<?>)typed, outputEntityChoiceType));
    }

    private static <T> Typed<T> fixArrow(Typed<?> typed, Type<T> outputType) {
        return Util.writeAndReadTypedOrThrow(typed, outputType, input -> input.set("item", FixProjectileStoredItem.createItemStack(input, FixProjectileStoredItem.getArrowType(input))));
    }

    private static String getArrowType(Dynamic<?> input) {
        return input.get("Potion").asString(EMPTY_POTION).equals(EMPTY_POTION) ? "minecraft:arrow" : "minecraft:tipped_arrow";
    }

    private static <T> Typed<T> fixSpectralArrow(Typed<?> typed, Type<T> outputType) {
        return Util.writeAndReadTypedOrThrow(typed, outputType, input -> input.set("item", FixProjectileStoredItem.createItemStack(input, "minecraft:spectral_arrow")));
    }

    private static Dynamic<?> createItemStack(Dynamic<?> input, String itemName) {
        return input.createMap(ImmutableMap.of(input.createString("id"), input.createString(itemName), input.createString("Count"), input.createInt(1)));
    }

    private static <T> Typed<T> castUnchecked(Typed<?> input, Type<T> outputType) {
        return new Typed<T>(outputType, input.getOps(), input.getValue());
    }

    private static interface SubFixer<F> {
        public Typed<F> fix(Typed<?> var1, Type<F> var2);
    }
}

