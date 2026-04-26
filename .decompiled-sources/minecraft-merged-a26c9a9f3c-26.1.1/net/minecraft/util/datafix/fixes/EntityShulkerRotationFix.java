/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class EntityShulkerRotationFix
extends NamedEntityFix {
    public EntityShulkerRotationFix(Schema outputSchema) {
        super(outputSchema, false, "EntityShulkerRotationFix", References.ENTITY, "minecraft:shulker");
    }

    public Dynamic<?> fixTag(Dynamic<?> input) {
        List<Double> rotation = input.get("Rotation").asList(d -> d.asDouble(180.0));
        if (!rotation.isEmpty()) {
            rotation.set(0, rotation.get(0) - 180.0);
            return input.set("Rotation", input.createList(rotation.stream().map(input::createDouble)));
        }
        return input;
    }

    @Override
    protected Typed<?> fix(Typed<?> entity) {
        return entity.update(DSL.remainderFinder(), this::fixTag);
    }
}

