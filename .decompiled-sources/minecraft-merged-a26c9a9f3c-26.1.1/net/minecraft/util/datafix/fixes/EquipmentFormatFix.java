/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.util.datafix.fixes.References;

public class EquipmentFormatFix
extends DataFix {
    public EquipmentFormatFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> oldItemStackType = this.getInputSchema().getTypeRaw(References.ITEM_STACK);
        Type<?> newItemStackType = this.getOutputSchema().getTypeRaw(References.ITEM_STACK);
        OpticFinder<?> idFinder = oldItemStackType.findField("id");
        return this.fix(oldItemStackType, newItemStackType, idFinder);
    }

    private <ItemStackOld, ItemStackNew> TypeRewriteRule fix(Type<ItemStackOld> oldItemStackType, Type<ItemStackNew> newItemStackType, OpticFinder<?> idFinder) {
        Type<Pair<String, Pair<Either<ItemStackOld, Unit>, Pair<Either<ItemStackOld, Unit>, Pair<Either<ItemStackOld, Unit>, Either<ItemStackOld, Unit>>>>>> oldEquipmentType = DSL.named(References.ENTITY_EQUIPMENT.typeName(), DSL.and(DSL.optional(DSL.field("ArmorItems", DSL.list(oldItemStackType))), DSL.optional(DSL.field("HandItems", DSL.list(oldItemStackType))), DSL.optional(DSL.field("body_armor_item", oldItemStackType)), DSL.optional(DSL.field("saddle", oldItemStackType))));
        Type<Pair<String, Either<Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Dynamic<?>>>>>>>>>, Unit>>> newEquipmentType = DSL.named(References.ENTITY_EQUIPMENT.typeName(), DSL.optional(DSL.field("equipment", DSL.and(DSL.optional(DSL.field("mainhand", newItemStackType)), DSL.optional(DSL.field("offhand", newItemStackType)), DSL.optional(DSL.field("feet", newItemStackType)), DSL.and(DSL.optional(DSL.field("legs", newItemStackType)), DSL.optional(DSL.field("chest", newItemStackType)), DSL.optional(DSL.field("head", newItemStackType)), DSL.and(DSL.optional(DSL.field("body", newItemStackType)), DSL.optional(DSL.field("saddle", newItemStackType)), DSL.remainderType()))))));
        if (!oldEquipmentType.equals(this.getInputSchema().getType(References.ENTITY_EQUIPMENT))) {
            throw new IllegalStateException("Input entity_equipment type does not match expected");
        }
        if (!newEquipmentType.equals(this.getOutputSchema().getType(References.ENTITY_EQUIPMENT))) {
            throw new IllegalStateException("Output entity_equipment type does not match expected");
        }
        return this.fixTypeEverywhere("EquipmentFormatFix", oldEquipmentType, newEquipmentType, ops -> {
            Predicate<Object> isPlaceholder = itemStack -> {
                Typed<Object> typed = new Typed<Object>(oldItemStackType, (DynamicOps<?>)ops, itemStack);
                return typed.getOptional(idFinder).isEmpty();
            };
            return namedOldEquipment -> {
                String typeName = (String)namedOldEquipment.getFirst();
                Pair oldEquipment = (Pair)namedOldEquipment.getSecond();
                List armorItems = ((Either)oldEquipment.getFirst()).map(Function.identity(), ignored -> List.of());
                List handItems = ((Either)((Pair)oldEquipment.getSecond()).getFirst()).map(Function.identity(), ignored -> List.of());
                Either body = (Either)((Pair)((Pair)oldEquipment.getSecond()).getSecond()).getFirst();
                Either saddle = (Either)((Pair)((Pair)oldEquipment.getSecond()).getSecond()).getSecond();
                Either feet = EquipmentFormatFix.getItemFromList(0, armorItems, isPlaceholder);
                Either legs = EquipmentFormatFix.getItemFromList(1, armorItems, isPlaceholder);
                Either chest = EquipmentFormatFix.getItemFromList(2, armorItems, isPlaceholder);
                Either head = EquipmentFormatFix.getItemFromList(3, armorItems, isPlaceholder);
                Either mainhand = EquipmentFormatFix.getItemFromList(0, handItems, isPlaceholder);
                Either offhand = EquipmentFormatFix.getItemFromList(1, handItems, isPlaceholder);
                if (EquipmentFormatFix.areAllEmpty(body, saddle, feet, legs, chest, head, mainhand, offhand)) {
                    return Pair.of(typeName, Either.right(Unit.INSTANCE));
                }
                return Pair.of(typeName, Either.left(Pair.of(mainhand, Pair.of(offhand, Pair.of(feet, Pair.of(legs, Pair.of(chest, Pair.of(head, Pair.of(body, Pair.of(saddle, new Dynamic(ops)))))))))));
            };
        });
    }

    @SafeVarargs
    private static boolean areAllEmpty(Either<?, Unit> ... fields) {
        for (Either<?, Unit> field : fields) {
            if (!field.right().isEmpty()) continue;
            return false;
        }
        return true;
    }

    private static <ItemStack> Either<ItemStack, Unit> getItemFromList(int index, List<ItemStack> items, Predicate<ItemStack> isPlaceholder) {
        if (index >= items.size()) {
            return Either.right(Unit.INSTANCE);
        }
        ItemStack item = items.get(index);
        if (isPlaceholder.test(item)) {
            return Either.right(Unit.INSTANCE);
        }
        return Either.left(item);
    }
}

