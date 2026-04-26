/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.References;

public class EntityEquipmentToArmorAndHandFix
extends DataFix {
    public EntityEquipmentToArmorAndHandFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.cap(this.getInputSchema().getTypeRaw(References.ITEM_STACK), this.getOutputSchema().getTypeRaw(References.ITEM_STACK));
    }

    private <ItemStackOld, ItemStackNew> TypeRewriteRule cap(Type<ItemStackOld> oldItemStackType, Type<ItemStackNew> newItemStackType) {
        Type<Pair<String, Either<ItemStackOld, Unit>>> oldEquipmentType = DSL.named(References.ENTITY_EQUIPMENT.typeName(), DSL.optional(DSL.field("Equipment", DSL.list(oldItemStackType))));
        Type<Pair<String, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Either<ItemStackNew, Unit>>>>>> newEquipmentType = DSL.named(References.ENTITY_EQUIPMENT.typeName(), DSL.and(DSL.optional(DSL.field("ArmorItems", DSL.list(newItemStackType))), DSL.optional(DSL.field("HandItems", DSL.list(newItemStackType))), DSL.optional(DSL.field("body_armor_item", newItemStackType)), DSL.optional(DSL.field("saddle", newItemStackType))));
        if (!oldEquipmentType.equals(this.getInputSchema().getType(References.ENTITY_EQUIPMENT))) {
            throw new IllegalStateException("Input entity_equipment type does not match expected");
        }
        if (!newEquipmentType.equals(this.getOutputSchema().getType(References.ENTITY_EQUIPMENT))) {
            throw new IllegalStateException("Output entity_equipment type does not match expected");
        }
        return TypeRewriteRule.seq(this.fixTypeEverywhereTyped("EntityEquipmentToArmorAndHandFix - drop chances", this.getInputSchema().getType(References.ENTITY), typed -> typed.update(DSL.remainderFinder(), EntityEquipmentToArmorAndHandFix::fixDropChances)), this.fixTypeEverywhere("EntityEquipmentToArmorAndHandFix - equipment", oldEquipmentType, newEquipmentType, ops -> {
            Object emptyStack = newItemStackType.read(new Dynamic(ops).emptyMap()).result().orElseThrow(() -> new IllegalStateException("Could not parse newly created empty itemstack.")).getFirst();
            Either noItem = Either.right(DSL.unit());
            return named -> named.mapSecond(equipmentField -> {
                List items = equipmentField.map(Function.identity(), ignored -> List.of());
                Either<Object, Unit> handItems = Either.right(DSL.unit());
                Either<Object, Unit> armorItems = Either.right(DSL.unit());
                if (!items.isEmpty()) {
                    handItems = Either.left(Lists.newArrayList(items.getFirst(), emptyStack));
                }
                if (items.size() > 1) {
                    ArrayList<Object> armor = Lists.newArrayList(emptyStack, emptyStack, emptyStack, emptyStack);
                    for (int i = 1; i < Math.min(items.size(), 5); ++i) {
                        armor.set(i - 1, items.get(i));
                    }
                    armorItems = Either.left(armor);
                }
                return Pair.of(armorItems, Pair.of(handItems, Pair.of(noItem, noItem)));
            });
        }));
    }

    private static Dynamic<?> fixDropChances(Dynamic<?> tag) {
        Optional<Stream<Dynamic<?>>> dropChances = tag.get("DropChances").asStreamOpt().result();
        tag = tag.remove("DropChances");
        if (dropChances.isPresent()) {
            Iterator chances = Stream.concat(dropChances.get().map(value -> Float.valueOf(value.asFloat(0.0f))), Stream.generate(() -> Float.valueOf(0.0f))).iterator();
            float handChance = ((Float)chances.next()).floatValue();
            if (tag.get("HandDropChances").result().isEmpty()) {
                tag = tag.set("HandDropChances", tag.createList(Stream.of(Float.valueOf(handChance), Float.valueOf(0.0f)).map(tag::createFloat)));
            }
            if (tag.get("ArmorDropChances").result().isEmpty()) {
                tag = tag.set("ArmorDropChances", tag.createList(Stream.of((Float)chances.next(), (Float)chances.next(), (Float)chances.next(), (Float)chances.next()).map(tag::createFloat)));
            }
        }
        return tag;
    }
}

