/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.block;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets={"net.minecraft.world.level.chunk.LevelChunkSection$1BlockCounter"})
public class ChunkSectionBlockStateCounterMixin {
    @Redirect(method={"accept(Lnet/minecraft/world/level/block/state/BlockState;I)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    private boolean modifyAirCheck(BlockState blockState) {
        return blockState.is(Blocks.AIR) || blockState.is(Blocks.CAVE_AIR) || blockState.is(Blocks.VOID_AIR);
    }
}

