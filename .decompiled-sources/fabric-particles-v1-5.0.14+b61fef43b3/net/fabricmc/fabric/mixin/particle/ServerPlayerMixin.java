/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.particle;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.fabric.impl.particle.BlockParticleOptionExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ServerPlayer.class})
abstract class ServerPlayerMixin {
    ServerPlayerMixin() {
    }

    @ModifyExpressionValue(method={"checkFallDamage"}, at={@At(value="NEW", target="(Lnet/minecraft/core/particles/ParticleType;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/core/particles/BlockParticleOption;")})
    private BlockParticleOption modifyBlockStateParticleOption(BlockParticleOption original, double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        ((BlockParticleOptionExtension)((Object)original)).fabric_setBlockPos(landedPosition);
        return original;
    }
}

