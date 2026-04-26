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

public class RandomSequenceSettingsFix
extends DataFix {
    public RandomSequenceSettingsFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("RandomSequenceSettingsFix", this.getInputSchema().getType(References.SAVED_DATA_RANDOM_SEQUENCES), input -> input.update(DSL.remainderFinder(), container -> container.update("data", tag -> tag.emptyMap().set("sequences", (Dynamic<?>)tag))));
    }
}

