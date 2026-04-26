/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.particle;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.fabric.impl.particle.BlockParticleOptionExtension;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={Warden.class})
abstract class WardenMixin
extends Monster
implements VibrationSystem {
    private WardenMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyExpressionValue(method={"clientDiggingParticles"}, at={@At(value="NEW", target="(Lnet/minecraft/core/particles/ParticleType;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/core/particles/BlockParticleOption;")})
    private BlockParticleOption modifyBlockStateParticleOption(BlockParticleOption original) {
        ((BlockParticleOptionExtension)((Object)original)).fabric_setBlockPos(this.getOnPos());
        return original;
    }
}

