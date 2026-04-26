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

public class BlockEntityKeepPacked
extends NamedEntityFix {
    public BlockEntityKeepPacked(Schema schema, boolean changesType) {
        super(schema, changesType, "BlockEntityKeepPacked", References.BLOCK_ENTITY, "DUMMY");
    }

    private static Dynamic<?> fixTag(Dynamic<?> tag) {
        return tag.set("keepPacked", tag.createBoolean(true));
    }

    @Override
    protected Typed<?> fix(Typed<?> entity) {
        return entity.update(DSL.remainderFinder(), BlockEntityKeepPacked::fixTag);
    }
}

