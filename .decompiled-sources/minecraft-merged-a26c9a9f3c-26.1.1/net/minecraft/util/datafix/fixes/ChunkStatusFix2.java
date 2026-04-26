/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Objects;
import net.minecraft.util.datafix.fixes.References;

public class ChunkStatusFix2
extends DataFix {
    private static final Map<String, String> RENAMES_AND_DOWNGRADES = ImmutableMap.builder().put("structure_references", "empty").put("biomes", "empty").put("base", "surface").put("carved", "carvers").put("liquid_carved", "liquid_carvers").put("decorated", "features").put("lighted", "light").put("mobs_spawned", "spawn").put("finalized", "heightmaps").put("fullchunk", "full").build();

    public ChunkStatusFix2(Schema schema, boolean changesType) {
        super(schema, changesType);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> chunkType = this.getInputSchema().getType(References.CHUNK);
        Type<?> levelType = chunkType.findFieldType("Level");
        OpticFinder<?> levelF = DSL.fieldFinder("Level", levelType);
        return this.fixTypeEverywhereTyped("ChunkStatusFix2", chunkType, this.getOutputSchema().getType(References.CHUNK), (Typed<?> input) -> input.updateTyped(levelF, level -> {
            String newStatus;
            Dynamic tag = level.get(DSL.remainderFinder());
            String status = tag.get("Status").asString("empty");
            if (Objects.equals(status, newStatus = RENAMES_AND_DOWNGRADES.getOrDefault(status, "empty"))) {
                return level;
            }
            return level.set(DSL.remainderFinder(), tag.set("Status", tag.createString(newStatus)));
        }));
    }
}

