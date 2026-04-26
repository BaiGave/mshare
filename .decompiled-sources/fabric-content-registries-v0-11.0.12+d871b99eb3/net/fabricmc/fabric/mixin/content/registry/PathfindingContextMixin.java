/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.content.registry;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.registry.LandPathTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={PathfindingContext.class})
public abstract class PathfindingContextMixin {
    @Shadow
    public abstract BlockState getBlockState(BlockPos var1);

    @Shadow
    public abstract CollisionGetter level();

    @Inject(method={"getPathTypeFromState"}, at={@At(value="INVOKE_ASSIGN", target="Lnet/minecraft/core/BlockPos$MutableBlockPos;set(III)Lnet/minecraft/core/BlockPos$MutableBlockPos;")}, cancellable=true)
    private void onGetNodeType(int x, int y, int z, CallbackInfoReturnable<PathType> cir, @Local(name={"pos"}) BlockPos pos) {
        PathType neighborPathType = LandPathTypeRegistry.getPathType(this.getBlockState(pos), this.level(), pos, true);
        if (neighborPathType != null) {
            cir.setReturnValue(neighborPathType);
        }
    }
}

