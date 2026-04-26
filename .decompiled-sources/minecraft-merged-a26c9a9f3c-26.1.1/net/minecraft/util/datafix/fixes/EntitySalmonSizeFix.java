/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class EntitySalmonSizeFix
extends NamedEntityFix {
    public EntitySalmonSizeFix(Schema outputSchema) {
        super(outputSchema, false, "EntitySalmonSizeFix", References.ENTITY, "minecraft:salmon");
    }

    @Override
    protected Typed<?> fix(Typed<?> entity) {
        return entity.update(DSL.remainderFinder(), tag -> {
            String type = tag.get("type").asString("medium");
            if (type.equals("large")) {
                return tag;
            }
            return tag.set("type", tag.createString("medium"));
        });
    }
}

