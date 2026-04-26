/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.particle;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.fabric.impl.particle.BlockParticleOptionExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={Breeze.class})
abstract class BreezeMixin
extends Monster {
    private BreezeMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyExpressionValue(method={"emitJumpTrailParticles", "emitGroundParticles"}, at={@At(value="NEW", target="(Lnet/minecraft/core/particles/ParticleType;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/core/particles/BlockParticleOption;")})
    private BlockParticleOption modifyBlockStateParticleOption(BlockParticleOption original) {
        BlockPos blockPos = !this.getInBlockState().isAir() ? this.blockPosition() : this.getOnPos();
        ((BlockParticleOptionExtension)((Object)original)).fabric_setBlockPos(blockPos);
        return original;
    }
}

