/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.NamedEntityWriteReadFix;
import net.minecraft.util.datafix.fixes.References;

public class FilteredSignsFix
extends NamedEntityWriteReadFix {
    public FilteredSignsFix(Schema outputSchema) {
        super(outputSchema, false, "Remove filtered text from signs", References.BLOCK_ENTITY, "minecraft:sign");
    }

    @Override
    protected <T> Dynamic<T> fix(Dynamic<T> input) {
        return input.remove("FilteredText1").remove("FilteredText2").remove("FilteredText3").remove("FilteredText4");
    }
}

