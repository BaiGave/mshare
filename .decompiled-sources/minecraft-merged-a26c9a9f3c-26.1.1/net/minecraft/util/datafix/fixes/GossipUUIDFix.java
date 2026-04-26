/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.AbstractUUIDFix;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class GossipUUIDFix
extends NamedEntityFix {
    public GossipUUIDFix(Schema outputSchema, String entityName) {
        super(outputSchema, false, "Gossip for for " + entityName, References.ENTITY, entityName);
    }

    @Override
    protected Typed<?> fix(Typed<?> entity) {
        return entity.update(DSL.remainderFinder(), tag -> tag.update("Gossips", gossips -> DataFixUtils.orElse(gossips.asStreamOpt().result().map(s -> s.map(gossip -> AbstractUUIDFix.replaceUUIDLeastMost(gossip, "Target", "Target").orElse((Dynamic<?>)gossip))).map(gossips::createList), gossips)));
    }
}

