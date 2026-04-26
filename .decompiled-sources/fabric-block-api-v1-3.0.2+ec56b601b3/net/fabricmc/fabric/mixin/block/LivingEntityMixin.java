/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.block.v1.BlockFunctionalityTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LivingEntity.class})
abstract class LivingEntityMixin {
    LivingEntityMixin() {
    }

    @Inject(method={"trapdoorUsableAsLadder"}, at={@At(value="INVOKE_ASSIGN", target="Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")}, allow=1, cancellable=true)
    private void allowTaggedBlocksForTrapdoorClimbing(BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> info, @Local(name={"belowState"}) BlockState belowState) {
        if (belowState.is(BlockFunctionalityTags.CAN_CLIMB_TRAPDOOR_ABOVE)) {
            if (belowState.getBlock() instanceof LadderBlock) {
                if (belowState.getValue(LadderBlock.FACING) == state.getValue(TrapDoorBlock.FACING)) {
                    info.setReturnValue(true);
                }
            } else {
                info.setReturnValue(true);
            }
        }
    }
}

