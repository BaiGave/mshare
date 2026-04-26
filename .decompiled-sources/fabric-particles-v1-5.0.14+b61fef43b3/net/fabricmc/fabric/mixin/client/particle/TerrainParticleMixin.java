/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.particle;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.client.particle.v1.ParticleRenderEvents;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={TerrainParticle.class})
abstract class TerrainParticleMixin
extends SingleQuadParticle {
    @Shadow
    @Final
    private BlockPos pos;

    private TerrainParticleMixin() {
        super(null, 0.0, 0.0, 0.0, null);
    }

    @WrapOperation(method={"<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/color/block/BlockColors;getTintSource(Lnet/minecraft/world/level/block/state/BlockState;I)Lnet/minecraft/client/color/block/BlockTintSource;")})
    private BlockTintSource removeUntintableParticles(BlockColors instance, BlockState state, int layer, Operation<BlockTintSource> original, @Local(argsOnly=true) ClientLevel level, @Local(argsOnly=true) BlockPos blockPos) {
        if (!ParticleRenderEvents.ALLOW_TERRAIN_PARTICLE_TINT.invoker().allowTerrainParticleTint(state, level, blockPos)) {
            return null;
        }
        return original.call(instance, state, layer);
    }

    @Redirect(method={"createTerrainParticle"}, at=@At(value="NEW", target="(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/particle/TerrainParticle;"))
    private static TerrainParticle constructTerrainParticle(ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ, BlockState state, BlockParticleOption parameters, ClientLevel world1, double x1, double y1, double z1, double velocityX1, double velocityY1, double velocityZ1) {
        BlockPos blockPos = parameters.getBlockPos();
        if (blockPos != null) {
            return new TerrainParticle(level, x, y, z, velocityX, velocityY, velocityZ, state, blockPos);
        }
        return new TerrainParticle(level, x, y, z, velocityX, velocityY, velocityZ, state);
    }
}

