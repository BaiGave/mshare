/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jspecify.annotations.Nullable;

public class LavaSubmergedBlockProcessor
extends StructureProcessor {
    public static final MapCodec<LavaSubmergedBlockProcessor> CODEC = MapCodec.unit(() -> INSTANCE);
    public static final LavaSubmergedBlockProcessor INSTANCE = new LavaSubmergedBlockProcessor();

    @Override
    public @Nullable StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos targetPosition, BlockPos referencePos, StructureTemplate.StructureBlockInfo originalBlockInfo, StructureTemplate.StructureBlockInfo processedBlockInfo, StructurePlaceSettings settings) {
        BlockPos pos = processedBlockInfo.pos();
        boolean wasLavaBefore = level.getBlockState(pos).is(Blocks.LAVA);
        if (wasLavaBefore && !Block.isShapeFullBlock(processedBlockInfo.state().getShape(level, pos))) {
            return new StructureTemplate.StructureBlockInfo(pos, Blocks.LAVA.defaultBlockState(), processedBlockInfo.nbt());
        }
        return processedBlockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.LAVA_SUBMERGED_BLOCK;
    }
}

