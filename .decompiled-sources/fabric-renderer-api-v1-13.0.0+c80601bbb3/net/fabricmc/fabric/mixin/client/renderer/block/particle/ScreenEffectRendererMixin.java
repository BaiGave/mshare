/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.block.particle;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ScreenEffectRenderer.class})
abstract class ScreenEffectRendererMixin {
    @Unique
    private static @Nullable BlockPos pos;

    ScreenEffectRendererMixin() {
    }

    @WrapOperation(method={"renderScreenEffect"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/block/BlockStateModelSet;getParticleMaterial(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/resources/model/sprite/Material$Baked;")})
    private static Material.Baked getParticleMaterialProxy(BlockStateModelSet models, BlockState state, Operation<Material.Baked> original, @Local(name={"player"}) Player player) {
        Level level;
        if (pos != null && (level = player.level()) instanceof BlockAndTintGetter) {
            BlockAndTintGetter level2 = (BlockAndTintGetter)((Object)level);
            Material.Baked material = models.getParticleMaterial(state, level2, pos);
            pos = null;
            return material;
        }
        return original.call(models, state);
    }

    @Inject(method={"getViewBlockingState"}, at={@At(value="RETURN")})
    private static void onReturnGetInWallBlockState(CallbackInfoReturnable<@Nullable BlockState> cir, @Local(name={"testPos"}) BlockPos.MutableBlockPos testPos) {
        pos = cir.getReturnValue() != null ? testPos.immutable() : null;
    }
}

