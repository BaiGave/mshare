/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class CarvingStepRemoveFix
extends DataFix {
    public CarvingStepRemoveFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("CarvingStepRemoveFix", this.getInputSchema().getType(References.CHUNK), CarvingStepRemoveFix::fixChunk);
    }

    private static Typed<?> fixChunk(Typed<?> input) {
        return input.update(DSL.remainderFinder(), chunkIn -> {
            Optional mask;
            Dynamic chunk = chunkIn;
            Optional carvingMasks = chunk.get("CarvingMasks").result();
            if (carvingMasks.isPresent() && (mask = carvingMasks.get().get("AIR").result()).isPresent()) {
                chunk = chunk.set("carving_mask", mask.get());
            }
            return chunk.remove("CarvingMasks");
        });
    }
}

