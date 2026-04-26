/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.particle;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.impl.particle.BlockParticleOptionExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={Entity.class})
abstract class EntityMixin {
    EntityMixin() {
    }

    @ModifyExpressionValue(method={"spawnSprintParticle"}, at={@At(value="NEW", target="(Lnet/minecraft/core/particles/ParticleType;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/core/particles/BlockParticleOption;")})
    private BlockParticleOption modifyBlockStateParticleOption(BlockParticleOption original, @Local(name={"pos"}) BlockPos pos) {
        ((BlockParticleOptionExtension)((Object)original)).fabric_setBlockPos(pos);
        return original;
    }
}

