/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;
import net.minecraft.util.datafix.fixes.NamedEntityFix;

public class VariantRenameFix
extends NamedEntityFix {
    private final Map<String, String> renames;

    public VariantRenameFix(Schema outputSchema, String name, DSL.TypeReference type, String entityName, Map<String, String> renames) {
        super(outputSchema, false, name, type, entityName);
        this.renames = renames;
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), remainder -> remainder.update("variant", variant -> DataFixUtils.orElse(variant.asString().map(v -> variant.createString(this.renames.getOrDefault(v, (String)v))).result(), variant)));
    }
}

