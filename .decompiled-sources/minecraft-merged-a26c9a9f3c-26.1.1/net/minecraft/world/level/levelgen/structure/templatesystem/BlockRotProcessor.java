/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jspecify.annotations.Nullable;

public class BlockRotProcessor
extends StructureProcessor {
    public static final MapCodec<BlockRotProcessor> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("rottable_blocks").forGetter(t -> t.rottableBlocks), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("integrity")).forGetter(t -> Float.valueOf(t.integrity))).apply((Applicative<BlockRotProcessor, ?>)i, BlockRotProcessor::new));
    private final Optional<HolderSet<Block>> rottableBlocks;
    private final float integrity;

    public BlockRotProcessor(HolderSet<Block> tag, float integrity) {
        this(Optional.of(tag), integrity);
    }

    public BlockRotProcessor(float integrity) {
        this(Optional.empty(), integrity);
    }

    private BlockRotProcessor(Optional<HolderSet<Block>> blockTagKey, float integrity) {
        this.integrity = integrity;
        this.rottableBlocks = blockTagKey;
    }

    @Override
    public @Nullable StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos targetPosition, BlockPos referencePos, StructureTemplate.StructureBlockInfo originalBlockInfo, StructureTemplate.StructureBlockInfo processedBlockInfo, StructurePlaceSettings settings) {
        RandomSource random = settings.getRandom(processedBlockInfo.pos());
        if (this.rottableBlocks.isPresent() && !originalBlockInfo.state().is(this.rottableBlocks.get()) || random.nextFloat() <= this.integrity) {
            return processedBlockInfo;
        }
        return null;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.BLOCK_ROT;
    }
}

