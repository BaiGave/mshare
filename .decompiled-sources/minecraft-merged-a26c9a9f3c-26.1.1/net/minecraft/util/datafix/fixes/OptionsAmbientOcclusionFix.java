/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.References;

public class OptionsAmbientOcclusionFix
extends DataFix {
    public OptionsAmbientOcclusionFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("OptionsAmbientOcclusionFix", this.getInputSchema().getType(References.OPTIONS), input -> input.update(DSL.remainderFinder(), tag -> DataFixUtils.orElse(tag.get("ao").asString().map(value -> tag.set("ao", tag.createString(OptionsAmbientOcclusionFix.updateValue(value)))).result(), tag)));
    }

    private static String updateValue(String value) {
        return switch (value) {
            case "0" -> "false";
            case "1", "2" -> "true";
            default -> value;
        };
    }
}

