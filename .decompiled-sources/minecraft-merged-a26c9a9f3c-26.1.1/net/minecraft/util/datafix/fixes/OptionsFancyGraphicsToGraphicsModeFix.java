/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;

public class OptionsFancyGraphicsToGraphicsModeFix
extends DataFix {
    public OptionsFancyGraphicsToGraphicsModeFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("fancyGraphics to graphicsMode", this.getInputSchema().getType(References.OPTIONS), input -> input.update(DSL.remainderFinder(), tag -> tag.renameAndFixField("fancyGraphics", "graphicsMode", OptionsFancyGraphicsToGraphicsModeFix::fixGraphicsMode)));
    }

    private static <T> Dynamic<T> fixGraphicsMode(Dynamic<T> field) {
        if ("true".equals(field.asString("true"))) {
            return field.createString("1");
        }
        return field.createString("0");
    }
}

