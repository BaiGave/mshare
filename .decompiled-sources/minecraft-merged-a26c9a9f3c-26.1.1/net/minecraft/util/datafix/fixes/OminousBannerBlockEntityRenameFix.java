/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class OminousBannerBlockEntityRenameFix
extends NamedEntityFix {
    public OminousBannerBlockEntityRenameFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType, "OminousBannerBlockEntityRenameFix", References.BLOCK_ENTITY, "minecraft:banner");
    }

    @Override
    protected Typed<?> fix(Typed<?> entity) {
        OpticFinder<?> customNameF = entity.getType().findField("CustomName");
        OpticFinder<?> textComponentF = DSL.typeFinder(this.getInputSchema().getType(References.TEXT_COMPONENT));
        return entity.updateTyped(customNameF, customName -> customName.update(textComponentF, pair -> pair.mapSecond(name -> name.replace("\"translate\":\"block.minecraft.illager_banner\"", "\"translate\":\"block.minecraft.ominous_banner\""))));
    }
}

