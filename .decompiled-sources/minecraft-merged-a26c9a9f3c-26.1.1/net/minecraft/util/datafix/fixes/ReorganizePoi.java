/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class ReorganizePoi
extends DataFix {
    public ReorganizePoi(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<Pair<String, Dynamic<?>>> poiChunkType = DSL.named(References.POI_CHUNK.typeName(), DSL.remainderType());
        if (!Objects.equals(poiChunkType, this.getInputSchema().getType(References.POI_CHUNK))) {
            throw new IllegalStateException("Poi type is not what was expected.");
        }
        return this.fixTypeEverywhere("POI reorganization", poiChunkType, ops -> input -> input.mapSecond(ReorganizePoi::cap));
    }

    private static <T> Dynamic<T> cap(Dynamic<T> input) {
        HashMap sections = Maps.newHashMap();
        for (int i = 0; i < 16; ++i) {
            String key = String.valueOf(i);
            Optional<Dynamic<T>> section = input.get(key).result();
            if (!section.isPresent()) continue;
            Dynamic<T> sectionRecords = section.get();
            Dynamic newSection = input.createMap(ImmutableMap.of(input.createString("Records"), sectionRecords));
            sections.put(input.createString(Integer.toString(i)), newSection);
            input = input.remove(key);
        }
        return input.set("Sections", input.createMap(sections));
    }
}

