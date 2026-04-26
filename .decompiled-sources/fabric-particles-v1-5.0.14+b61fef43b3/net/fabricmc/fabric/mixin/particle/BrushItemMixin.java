/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.particle;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.fabric.impl.particle.BlockParticleOptionExtension;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={BrushItem.class})
abstract class BrushItemMixin {
    BrushItemMixin() {
    }

    @ModifyExpressionValue(method={"spawnDustParticles"}, at={@At(value="NEW", target="(Lnet/minecraft/core/particles/ParticleType;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/core/particles/BlockParticleOption;")})
    private BlockParticleOption modifyBlockStateParticleOption(BlockParticleOption original, Level level, BlockHitResult hitResult, BlockState state, Vec3 userRotation, HumanoidArm arm) {
        ((BlockParticleOptionExtension)((Object)original)).fabric_setBlockPos(hitResult.getBlockPos());
        return original;
    }
}

