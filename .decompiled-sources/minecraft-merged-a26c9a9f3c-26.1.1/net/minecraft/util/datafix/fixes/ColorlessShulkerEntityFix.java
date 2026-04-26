/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class ColorlessShulkerEntityFix
extends NamedEntityFix {
    public ColorlessShulkerEntityFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType, "Colorless shulker entity fix", References.ENTITY, "minecraft:shulker");
    }

    @Override
    protected Typed<?> fix(Typed<?> entity) {
        return entity.update(DSL.remainderFinder(), tag -> {
            if (tag.get("Color").asInt(0) == 10) {
                return tag.set("Color", tag.createByte((byte)16));
            }
            return tag;
        });
    }
}

