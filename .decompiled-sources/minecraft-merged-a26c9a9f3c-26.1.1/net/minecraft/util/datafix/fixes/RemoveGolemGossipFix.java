/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class RemoveGolemGossipFix
extends NamedEntityFix {
    public RemoveGolemGossipFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType, "Remove Golem Gossip Fix", References.ENTITY, "minecraft:villager");
    }

    @Override
    protected Typed<?> fix(Typed<?> entity) {
        return entity.update(DSL.remainderFinder(), RemoveGolemGossipFix::fixValue);
    }

    private static Dynamic<?> fixValue(Dynamic<?> tag) {
        return tag.update("Gossips", gossips -> tag.createList(gossips.asStream().filter(attribute -> !attribute.get("Type").asString("").equals("golem"))));
    }
}

