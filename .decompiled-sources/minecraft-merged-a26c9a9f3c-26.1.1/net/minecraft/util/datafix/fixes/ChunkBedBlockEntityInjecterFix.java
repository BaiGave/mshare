/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.util.datafix.fixes.References;

public class ChunkBedBlockEntityInjecterFix
extends DataFix {
    public ChunkBedBlockEntityInjecterFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> chunkType = this.getOutputSchema().getType(References.CHUNK);
        Type<?> levelType = chunkType.findFieldType("Level");
        Type<?> tileEntitiesType = levelType.findFieldType("TileEntities");
        if (!(tileEntitiesType instanceof List.ListType)) {
            throw new IllegalStateException("Tile entity type is not a list type.");
        }
        List.ListType tileEntityListType = (List.ListType)tileEntitiesType;
        return this.cap(levelType, tileEntityListType);
    }

    private <TE> TypeRewriteRule cap(Type<?> levelType, List.ListType<TE> tileEntityListType) {
        Type tileEntityType = tileEntityListType.getElement();
        OpticFinder<?> levelF = DSL.fieldFinder("Level", levelType);
        OpticFinder tileEntitiesF = DSL.fieldFinder("TileEntities", tileEntityListType);
        int bedId = 416;
        return TypeRewriteRule.seq(this.fixTypeEverywhere("InjectBedBlockEntityType", this.getInputSchema().findChoiceType(References.BLOCK_ENTITY), this.getOutputSchema().findChoiceType(References.BLOCK_ENTITY), ops -> v -> v), this.fixTypeEverywhereTyped("BedBlockEntityInjecter", this.getOutputSchema().getType(References.CHUNK), input -> {
            Typed<Object> level = input.getTyped(levelF);
            Dynamic<?> levelTag = level.get(DSL.remainderFinder());
            int chunkX = levelTag.get("xPos").asInt(0);
            int chunkZ = levelTag.get("zPos").asInt(0);
            ArrayList tileEntities = Lists.newArrayList((Iterable)level.getOrCreate(tileEntitiesF));
            List sectionTags = levelTag.get("Sections").asList(Function.identity());
            for (Dynamic sectionTag : sectionTags) {
                int pos = sectionTag.get("Y").asInt(0);
                Streams.mapWithIndex(sectionTag.get("Blocks").asIntStream(), (block, index) -> {
                    if (416 == (block & 0xFF) << 4) {
                        int p = (int)index;
                        int xx = p & 0xF;
                        int yy = p >> 8 & 0xF;
                        int zz = p >> 4 & 0xF;
                        HashMap bedTag = Maps.newHashMap();
                        bedTag.put(sectionTag.createString("id"), sectionTag.createString("minecraft:bed"));
                        bedTag.put(sectionTag.createString("x"), sectionTag.createInt(xx + (chunkX << 4)));
                        bedTag.put(sectionTag.createString("y"), sectionTag.createInt(yy + (pos << 4)));
                        bedTag.put(sectionTag.createString("z"), sectionTag.createInt(zz + (chunkZ << 4)));
                        bedTag.put(sectionTag.createString("color"), sectionTag.createShort((short)14));
                        return bedTag;
                    }
                    return null;
                }).forEachOrdered(bedTag -> {
                    if (bedTag != null) {
                        tileEntities.add(tileEntityType.read(sectionTag.createMap((Map<? extends Dynamic<?>, ? extends Dynamic<?>>)bedTag)).result().orElseThrow(() -> new IllegalStateException("Could not parse newly created bed block entity.")).getFirst());
                    }
                });
            }
            if (!tileEntities.isEmpty()) {
                return input.set(levelF, level.set(tileEntitiesF, tileEntities));
            }
            return input;
        }));
    }
}

