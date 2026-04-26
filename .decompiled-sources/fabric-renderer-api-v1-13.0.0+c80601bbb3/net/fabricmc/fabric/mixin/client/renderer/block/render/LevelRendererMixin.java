/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.block.render;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={LevelRenderer.class})
abstract class LevelRendererMixin {
    @Shadow
    private ClientLevel level;
    @Unique
    private final RandomSource random = RandomSource.createThreadLocalInstance(0L);

    LevelRendererMixin() {
    }

    @Redirect(method={"extractBlockOutline"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;hasMaterialFlag(I)Z"))
    private boolean hasMaterialFlagProxy(BlockStateModel model, @BakedQuad.MaterialFlags int flag, @Local(name={"pos"}) BlockPos pos, @Local(name={"state"}) BlockState state) {
        this.random.setSeed(state.getSeed(pos));
        return model.hasMaterialFlag(this.level, pos, state, this.random, flag);
    }
}

