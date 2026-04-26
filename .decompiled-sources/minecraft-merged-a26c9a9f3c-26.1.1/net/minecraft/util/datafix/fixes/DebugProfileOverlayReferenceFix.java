/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.References;

public class DebugProfileOverlayReferenceFix
extends DataFix {
    public DebugProfileOverlayReferenceFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("DebugProfileOverlayReferenceFix", this.getInputSchema().getType(References.DEBUG_PROFILE), typed -> typed.update(DSL.remainderFinder(), file -> file.update("custom", custom -> custom.updateMapValues(pair -> pair.mapSecond(value -> {
            if (value.asString("").equals("inF3")) {
                return value.createString("inOverlay");
            }
            return value;
        })))));
    }
}

