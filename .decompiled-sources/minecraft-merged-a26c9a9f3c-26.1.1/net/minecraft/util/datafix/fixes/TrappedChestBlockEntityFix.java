/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.AddNewChoices;
import net.minecraft.util.datafix.fixes.LeavesFix;
import net.minecraft.util.datafix.fixes.References;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class TrappedChestBlockEntityFix
extends DataFix {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SIZE = 4096;
    private static final short SIZE_BITS = 12;

    public TrappedChestBlockEntityFix(Schema outputSchema, boolean changesType) {
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
        OpticFinder tileEntitiesF = DSL.fieldFinder("TileEntities", tileEntityListType);
        Type<?> chunkType1 = this.getInputSchema().getType(References.CHUNK);
        OpticFinder<?> levelFinder = chunkType1.findField("Level");
        OpticFinder<?> sectionsFinder = levelFinder.type().findField("Sections");
        Type<?> sectionsType = sectionsFinder.type();
        if (!(sectionsType instanceof List.ListType)) {
            throw new IllegalStateException("Expecting sections to be a list.");
        }
        Type sectionType = ((List.ListType)sectionsType).getElement();
        OpticFinder sectionFinder = DSL.typeFinder(sectionType);
        return TypeRewriteRule.seq(new AddNewChoices(this.getOutputSchema(), "AddTrappedChestFix", References.BLOCK_ENTITY).makeRule(), this.fixTypeEverywhereTyped("Trapped Chest fix", chunkType1, chunk -> chunk.updateTyped(levelFinder, level -> {
            Optional sections = level.getOptionalTyped(sectionsFinder);
            if (sections.isEmpty()) {
                return level;
            }
            List sectionList = sections.get().getAllTyped(sectionFinder);
            IntOpenHashSet chestLocations = new IntOpenHashSet();
            for (Typed section : sectionList) {
                TrappedChestSection trappedChestSection = new TrappedChestSection(section, this.getInputSchema());
                if (trappedChestSection.isSkippable()) continue;
                for (int i = 0; i < 4096; ++i) {
                    int block = trappedChestSection.getBlock(i);
                    if (!trappedChestSection.isTrappedChest(block)) continue;
                    chestLocations.add(trappedChestSection.getIndex() << 12 | i);
                }
            }
            Dynamic<?> levelTag = level.get(DSL.remainderFinder());
            int chunkX = levelTag.get("xPos").asInt(0);
            int chunkZ = levelTag.get("zPos").asInt(0);
            TaggedChoice.TaggedChoiceType<?> tileEntityChoiceType = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
            return level.updateTyped(tileEntitiesF, tileEntities -> tileEntities.updateTyped(tileEntityChoiceType.finder(), tileEntity -> {
                int z;
                int y;
                Dynamic<?> tag = tileEntity.getOrCreate(DSL.remainderFinder());
                int x = tag.get("x").asInt(0) - (chunkX << 4);
                if (chestLocations.contains(LeavesFix.getIndex(x, y = tag.get("y").asInt(0), z = tag.get("z").asInt(0) - (chunkZ << 4)))) {
                    return tileEntity.update(tileEntityChoiceType.finder(), stringPair -> stringPair.mapFirst(s -> {
                        if (!Objects.equals(s, "minecraft:chest")) {
                            LOGGER.warn("Block Entity was expected to be a chest");
                        }
                        return "minecraft:trapped_chest";
                    }));
                }
                return tileEntity;
            }));
        })));
    }

    public static final class TrappedChestSection
    extends LeavesFix.Section {
        private @Nullable IntSet chestIds;

        public TrappedChestSection(Typed<?> section, Schema inputSchema) {
            super(section, inputSchema);
        }

        @Override
        protected boolean skippable() {
            this.chestIds = new IntOpenHashSet();
            for (int i = 0; i < this.palette.size(); ++i) {
                Dynamic paletteTag = (Dynamic)this.palette.get(i);
                String blockName = paletteTag.get("Name").asString("");
                if (!Objects.equals(blockName, "minecraft:trapped_chest")) continue;
                this.chestIds.add(i);
            }
            return this.chestIds.isEmpty();
        }

        public boolean isTrappedChest(int block) {
            return this.chestIds.contains(block);
        }
    }
}

