/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.ChunkHeightAndBiomeFix;
import net.minecraft.util.datafix.fixes.References;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jspecify.annotations.Nullable;

public class ChunkProtoTickListFix
extends DataFix {
    private static final int SECTION_WIDTH = 16;
    private static final ImmutableSet<String> ALWAYS_WATERLOGGED = ImmutableSet.of("minecraft:bubble_column", "minecraft:kelp", "minecraft:kelp_plant", "minecraft:seagrass", "minecraft:tall_seagrass");

    public ChunkProtoTickListFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> chunkType = this.getInputSchema().getType(References.CHUNK);
        OpticFinder<?> levelFinder = chunkType.findField("Level");
        OpticFinder<?> sectionsFinder = levelFinder.type().findField("Sections");
        OpticFinder sectionFinder = ((List.ListType)sectionsFinder.type()).getElement().finder();
        OpticFinder<?> blockStateContainerFinder = sectionFinder.type().findField("block_states");
        OpticFinder<?> biomeContainerFinder = sectionFinder.type().findField("biomes");
        OpticFinder<?> blockStatePaletteFinder = blockStateContainerFinder.type().findField("palette");
        OpticFinder<?> tileTickFinder = levelFinder.type().findField("TileTicks");
        return this.fixTypeEverywhereTyped("ChunkProtoTickListFix", chunkType, chunk -> chunk.updateTyped(levelFinder, level -> {
            level = level.update(DSL.remainderFinder(), tag -> DataFixUtils.orElse(tag.get("LiquidTicks").result().map(v -> tag.set("fluid_ticks", (Dynamic<?>)v).remove("LiquidTicks")), tag));
            Dynamic<?> chunkTag = level.get(DSL.remainderFinder());
            MutableInt lowestY = new MutableInt();
            Int2ObjectArrayMap<Supplier<PoorMansPalettedContainer>> palettedContainers = new Int2ObjectArrayMap<Supplier<PoorMansPalettedContainer>>();
            level.getOptionalTyped(sectionsFinder).ifPresent(sections -> sections.getAllTyped(sectionFinder).forEach(section -> {
                Dynamic<?> sectionRemainder = section.get(DSL.remainderFinder());
                int sectionY = sectionRemainder.get("Y").asInt(Integer.MAX_VALUE);
                if (sectionY == Integer.MAX_VALUE) {
                    return;
                }
                if (section.getOptionalTyped(biomeContainerFinder).isPresent()) {
                    lowestY.setValue(Math.min(sectionY, lowestY.intValue()));
                }
                section.getOptionalTyped(blockStateContainerFinder).ifPresent(blockContainer -> palettedContainers.put(sectionY, (Supplier<PoorMansPalettedContainer>)Suppliers.memoize(() -> {
                    List palette = blockContainer.getOptionalTyped(blockStatePaletteFinder).map(x -> x.write().result().map(r -> r.asList(Function.identity())).orElse(Collections.emptyList())).orElse(Collections.emptyList());
                    long[] data = blockContainer.get(DSL.remainderFinder()).get("data").asLongStream().toArray();
                    return new PoorMansPalettedContainer(palette, data);
                })));
            }));
            byte sectionMinY = lowestY.byteValue();
            level = level.update(DSL.remainderFinder(), remainder -> remainder.update("yPos", y -> y.createByte(sectionMinY)));
            if (level.getOptionalTyped(tileTickFinder).isPresent() || chunkTag.get("fluid_ticks").result().isPresent()) {
                return level;
            }
            int sectionX = chunkTag.get("xPos").asInt(0);
            int sectionZ = chunkTag.get("zPos").asInt(0);
            Dynamic<?> fluidTicks = this.makeTickList(chunkTag, palettedContainers, sectionMinY, sectionX, sectionZ, "LiquidsToBeTicked", ChunkProtoTickListFix::getLiquid);
            Dynamic<?> blockTicks = this.makeTickList(chunkTag, palettedContainers, sectionMinY, sectionX, sectionZ, "ToBeTicked", ChunkProtoTickListFix::getBlock);
            Optional parsedBlockTicks = tileTickFinder.type().readTyped(blockTicks).result();
            if (parsedBlockTicks.isPresent()) {
                level = level.set(tileTickFinder, parsedBlockTicks.get().getFirst());
            }
            return level.update(DSL.remainderFinder(), remainder -> remainder.remove("ToBeTicked").remove("LiquidsToBeTicked").set("fluid_ticks", fluidTicks));
        }));
    }

    private Dynamic<?> makeTickList(Dynamic<?> tag, Int2ObjectMap<Supplier<PoorMansPalettedContainer>> palettedContainers, byte sectionMinY, int sectionX, int sectionZ, String protoTickListTag, Function<Dynamic<?>, String> typeGetter) {
        Stream<Object> newTickList = Stream.empty();
        List ticksPerSection = tag.get(protoTickListTag).asList(Function.identity());
        for (int sectionYIndex = 0; sectionYIndex < ticksPerSection.size(); ++sectionYIndex) {
            int sectionY = sectionYIndex + sectionMinY;
            Supplier container = (Supplier)palettedContainers.get(sectionY);
            Stream<Dynamic> newTickListForSection = ((Dynamic)ticksPerSection.get(sectionYIndex)).asStream().mapToInt(pos -> pos.asShort((short)-1)).filter(pos -> pos > 0).mapToObj(arg_0 -> this.lambda$makeTickList$2(tag, (Supplier)container, sectionX, sectionY, sectionZ, typeGetter, arg_0));
            newTickList = Stream.concat(newTickList, newTickListForSection);
        }
        return tag.createList(newTickList);
    }

    private static String getBlock(@Nullable Dynamic<?> blockState) {
        return blockState != null ? blockState.get("Name").asString("minecraft:air") : "minecraft:air";
    }

    private static String getLiquid(@Nullable Dynamic<?> blockState) {
        if (blockState == null) {
            return "minecraft:empty";
        }
        String block = blockState.get("Name").asString("");
        if ("minecraft:water".equals(block)) {
            return blockState.get("Properties").get("level").asInt(0) == 0 ? "minecraft:water" : "minecraft:flowing_water";
        }
        if ("minecraft:lava".equals(block)) {
            return blockState.get("Properties").get("level").asInt(0) == 0 ? "minecraft:lava" : "minecraft:flowing_lava";
        }
        if (ALWAYS_WATERLOGGED.contains(block) || blockState.get("Properties").get("waterlogged").asBoolean(false)) {
            return "minecraft:water";
        }
        return "minecraft:empty";
    }

    private Dynamic<?> createTick(Dynamic<?> tag, @Nullable Supplier<PoorMansPalettedContainer> container, int sectionX, int sectionY, int sectionZ, int pos, Function<Dynamic<?>, String> typeGetter) {
        int relativeX = pos & 0xF;
        int relativeY = pos >>> 4 & 0xF;
        int relativeZ = pos >>> 8 & 0xF;
        String type = typeGetter.apply(container != null ? container.get().get(relativeX, relativeY, relativeZ) : null);
        return tag.createMap(ImmutableMap.builder().put(tag.createString("i"), tag.createString(type)).put(tag.createString("x"), tag.createInt(sectionX * 16 + relativeX)).put(tag.createString("y"), tag.createInt(sectionY * 16 + relativeY)).put(tag.createString("z"), tag.createInt(sectionZ * 16 + relativeZ)).put(tag.createString("t"), tag.createInt(0)).put(tag.createString("p"), tag.createInt(0)).build());
    }

    private /* synthetic */ Dynamic lambda$makeTickList$2(Dynamic tag, Supplier container, int sectionX, int sectionY, int sectionZ, Function typeGetter, int pos) {
        return this.createTick(tag, container, sectionX, sectionY, sectionZ, pos, typeGetter);
    }

    public static final class PoorMansPalettedContainer {
        private static final long SIZE_BITS = 4L;
        private final List<? extends Dynamic<?>> palette;
        private final long[] data;
        private final int bits;
        private final long mask;
        private final int valuesPerLong;

        public PoorMansPalettedContainer(List<? extends Dynamic<?>> palette, long[] data) {
            this.palette = palette;
            this.data = data;
            this.bits = Math.max(4, ChunkHeightAndBiomeFix.ceillog2(palette.size()));
            this.mask = (1L << this.bits) - 1L;
            this.valuesPerLong = (char)(64 / this.bits);
        }

        public @Nullable Dynamic<?> get(int x, int y, int z) {
            int entryCount = this.palette.size();
            if (entryCount < 1) {
                return null;
            }
            if (entryCount == 1) {
                return this.palette.getFirst();
            }
            int index = this.getIndex(x, y, z);
            int cellIndex = index / this.valuesPerLong;
            if (cellIndex < 0 || cellIndex >= this.data.length) {
                return null;
            }
            long cellValue = this.data[cellIndex];
            int bitIndex = (index - cellIndex * this.valuesPerLong) * this.bits;
            int paletteIndex = (int)(cellValue >> bitIndex & this.mask);
            if (paletteIndex < 0 || paletteIndex >= entryCount) {
                return null;
            }
            return this.palette.get(paletteIndex);
        }

        private int getIndex(int x, int y, int z) {
            return (y << 4 | z) << 4 | x;
        }

        public List<? extends Dynamic<?>> palette() {
            return this.palette;
        }

        public long[] data() {
            return this.data;
        }
    }
}

