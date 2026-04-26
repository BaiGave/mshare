/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.block.v1;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public interface FabricBlock {
    default public BlockState getAppearance(BlockState state, BlockAndLightGetter blockAndLightGetter, BlockPos pos, Direction side, @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
        return state;
    }

    public static interface FabricProperties {
        default public @Nullable ResourceKey<Block> blockId() {
            throw new AssertionError((Object)"Implemented in Mixin");
        }

        default public ResourceKey<Block> blockIdOrThrow() {
            return Objects.requireNonNull(this.blockId(), "Block id not set");
        }
    }
}

