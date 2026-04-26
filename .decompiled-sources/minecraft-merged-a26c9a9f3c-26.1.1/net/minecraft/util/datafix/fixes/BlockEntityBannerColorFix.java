/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class BlockEntityBannerColorFix
extends NamedEntityFix {
    public BlockEntityBannerColorFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType, "BlockEntityBannerColorFix", References.BLOCK_ENTITY, "minecraft:banner");
    }

    public Dynamic<?> fixTag(Dynamic<?> input) {
        input = input.update("Base", base -> base.createInt(15 - base.asInt(0)));
        input = input.update("Patterns", list -> DataFixUtils.orElse(list.asStreamOpt().map(stream -> stream.map(pattern -> pattern.update("Color", color -> color.createInt(15 - color.asInt(0))))).map(list::createList).result(), list));
        return input;
    }

    @Override
    protected Typed<?> fix(Typed<?> entity) {
        return entity.update(DSL.remainderFinder(), this::fixTag);
    }
}

