/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.block.particle;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={TerrainParticle.class})
abstract class TerrainParticleMixin {
    TerrainParticleMixin() {
    }

    @Redirect(method={"<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/block/BlockStateModelSet;getParticleMaterial(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/resources/model/sprite/Material$Baked;"))
    private static Material.Baked getParticleIconProxy(BlockStateModelSet models, BlockState state, @Local(argsOnly=true) ClientLevel level, @Local(argsOnly=true) BlockPos blockPos) {
        return models.getParticleMaterial(state, level, blockPos);
    }
}

