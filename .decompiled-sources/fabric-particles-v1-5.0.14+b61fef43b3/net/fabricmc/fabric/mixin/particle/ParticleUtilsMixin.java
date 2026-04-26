/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.particle;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.fabric.impl.particle.BlockParticleOptionExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.util.ParticleUtils;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ParticleUtils.class})
abstract class ParticleUtilsMixin {
    ParticleUtilsMixin() {
    }

    @ModifyExpressionValue(method={"spawnSmashAttackParticles"}, at={@At(value="NEW", target="(Lnet/minecraft/core/particles/ParticleType;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/core/particles/BlockParticleOption;")})
    private static BlockParticleOption modifyBlockStateParticleOption(BlockParticleOption original, LevelAccessor level, BlockPos pos, int count) {
        ((BlockParticleOptionExtension)((Object)original)).fabric_setBlockPos(pos);
        return original;
    }
}

