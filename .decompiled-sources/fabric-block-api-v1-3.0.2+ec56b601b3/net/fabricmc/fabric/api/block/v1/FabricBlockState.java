/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.block.v1;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public interface FabricBlockState {
    default public BlockState getAppearance(BlockAndLightGetter level, BlockPos pos, Direction side, @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
        BlockState self = (BlockState)this;
        return self.getBlock().getAppearance(self, level, pos, side, sourceState, sourcePos);
    }
}

