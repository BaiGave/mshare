/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.content.registry;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.registry.LandPathTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={WalkNodeEvaluator.class}, priority=999)
public class WalkNodeEvaluatorMixin {
    @Inject(method={"getPathTypeFromState"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;")}, cancellable=true)
    private static void getCommonNodeType(BlockGetter level, BlockPos pos, CallbackInfoReturnable<PathType> cir, @Local(name={"blockState"}) BlockState blockState) {
        PathType pathType = LandPathTypeRegistry.getPathType(blockState, level, pos, false);
        if (pathType != null) {
            cir.setReturnValue(pathType);
        }
    }
}

