/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.SectionPos;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class BlendingDataFix
extends DataFix {
    private final String name;
    private static final Set<String> STATUSES_TO_SKIP_BLENDING = Set.of("minecraft:empty", "minecraft:structure_starts", "minecraft:structure_references", "minecraft:biomes");

    public BlendingDataFix(Schema outputSchema) {
        super(outputSchema, false);
        this.name = "Blending Data Fix v" + outputSchema.getVersionKey();
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> chunkType = this.getOutputSchema().getType(References.CHUNK);
        return this.fixTypeEverywhereTyped(this.name, chunkType, chunk -> chunk.update(DSL.remainderFinder(), chunkTag -> BlendingDataFix.updateChunkTag(chunkTag, chunkTag.get("__context"))));
    }

    private static Dynamic<?> updateChunkTag(Dynamic<?> chunkTag, OptionalDynamic<?> contextTag) {
        chunkTag = chunkTag.remove("blending_data");
        boolean isOverworld = "minecraft:overworld".equals(contextTag.get("dimension").asString().result().orElse(""));
        Optional<Dynamic<?>> statusOpt = chunkTag.get("Status").result();
        if (isOverworld && statusOpt.isPresent()) {
            Dynamic<?> belowZeroRetrogen;
            String targetStatus;
            String status = NamespacedSchema.ensureNamespaced(statusOpt.get().asString("empty"));
            Optional<Dynamic<?>> belowZeroRetrogenOpt = chunkTag.get("below_zero_retrogen").result();
            if (!STATUSES_TO_SKIP_BLENDING.contains(status)) {
                chunkTag = BlendingDataFix.updateBlendingData(chunkTag, 384, -64);
            } else if (belowZeroRetrogenOpt.isPresent() && !STATUSES_TO_SKIP_BLENDING.contains(targetStatus = NamespacedSchema.ensureNamespaced((belowZeroRetrogen = belowZeroRetrogenOpt.get()).get("target_status").asString("empty")))) {
                chunkTag = BlendingDataFix.updateBlendingData(chunkTag, 256, 0);
            }
        }
        return chunkTag;
    }

    private static Dynamic<?> updateBlendingData(Dynamic<?> chunkTag, int height, int minY) {
        return chunkTag.set("blending_data", chunkTag.createMap(Map.of(chunkTag.createString("min_section"), chunkTag.createInt(SectionPos.blockToSectionCoord(minY)), chunkTag.createString("max_section"), chunkTag.createInt(SectionPos.blockToSectionCoord(minY + height)))));
    }
}

