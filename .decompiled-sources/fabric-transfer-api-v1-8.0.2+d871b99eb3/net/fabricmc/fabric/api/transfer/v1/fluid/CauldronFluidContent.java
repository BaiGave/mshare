/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.fluid;

import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.lookup.v1.custom.ApiProviderMap;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.impl.transfer.fluid.CauldronStorage;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public final class CauldronFluidContent {
    public final Block block;
    public final Fluid fluid;
    public final long amountPerLevel;
    public final int maxLevel;
    public final @Nullable IntegerProperty levelProperty;
    private static final ApiProviderMap<Block, CauldronFluidContent> BLOCK_TO_CAULDRON = ApiProviderMap.create();
    private static final ApiProviderMap<Fluid, CauldronFluidContent> FLUID_TO_CAULDRON = ApiProviderMap.create();

    private CauldronFluidContent(Block block, Fluid fluid, long amountPerLevel, int maxLevel, @Nullable IntegerProperty levelProperty) {
        this.block = block;
        this.fluid = fluid;
        this.amountPerLevel = amountPerLevel;
        this.maxLevel = maxLevel;
        this.levelProperty = levelProperty;
    }

    public static @Nullable CauldronFluidContent getForBlock(Block block) {
        return BLOCK_TO_CAULDRON.get(block);
    }

    public static @Nullable CauldronFluidContent getForFluid(Fluid fluid) {
        return FLUID_TO_CAULDRON.get(fluid);
    }

    public static synchronized CauldronFluidContent registerCauldron(Block block, Fluid fluid, long amountPerLevel, @Nullable IntegerProperty levelProperty) {
        CauldronFluidContent data;
        CauldronFluidContent existingBlockData = BLOCK_TO_CAULDRON.get(block);
        if (existingBlockData != null) {
            return existingBlockData;
        }
        if (FLUID_TO_CAULDRON.get(fluid) != null) {
            throw new IllegalArgumentException("Fluid already has a mapping for a different block.");
        }
        if (levelProperty == null) {
            data = new CauldronFluidContent(block, fluid, amountPerLevel, 1, null);
        } else {
            List<Integer> levels = levelProperty.getPossibleValues();
            if (levels.size() == 0) {
                throw new RuntimeException("Cauldron should have at least one possible level.");
            }
            int minLevel = Integer.MAX_VALUE;
            int maxLevel = 0;
            Iterator iterator = levels.iterator();
            while (iterator.hasNext()) {
                int level2 = (Integer)iterator.next();
                minLevel = Math.min(minLevel, level2);
                maxLevel = Math.max(maxLevel, level2);
            }
            if (minLevel != 1 || maxLevel < 1) {
                throw new IllegalStateException("Minimum level should be 1, and maximum level should be >= 1.");
            }
            data = new CauldronFluidContent(block, fluid, amountPerLevel, maxLevel, levelProperty);
        }
        BLOCK_TO_CAULDRON.putIfAbsent(block, data);
        FLUID_TO_CAULDRON.putIfAbsent(fluid, data);
        FluidStorage.SIDED.registerForBlocks((level, pos, state, be, context) -> CauldronStorage.get(level, pos), block);
        return data;
    }

    public int currentLevel(BlockState state) {
        if (this.fluid == Fluids.EMPTY) {
            return 0;
        }
        if (this.levelProperty == null) {
            return 1;
        }
        return state.getValue(this.levelProperty);
    }

    static {
        CauldronFluidContent.registerCauldron(Blocks.CAULDRON, Fluids.EMPTY, 81000L, null);
        CauldronFluidContent.registerCauldron(Blocks.WATER_CAULDRON, Fluids.WATER, 27000L, LayeredCauldronBlock.LEVEL);
        CauldronFluidContent.registerCauldron(Blocks.LAVA_CAULDRON, Fluids.LAVA, 81000L, null);
    }
}

