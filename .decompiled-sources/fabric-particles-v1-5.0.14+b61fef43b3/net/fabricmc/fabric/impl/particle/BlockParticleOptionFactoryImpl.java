/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.particle;

import net.fabricmc.fabric.impl.particle.BlockParticleOptionExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockParticleOptionFactoryImpl {
    private BlockParticleOptionFactoryImpl() {
    }

    public static BlockParticleOption create(ParticleType<BlockParticleOption> type, BlockState blockState, @Nullable BlockPos blockPos) {
        BlockParticleOption effect = new BlockParticleOption(type, blockState);
        ((BlockParticleOptionExtension)((Object)effect)).fabric_setBlockPos(blockPos);
        return effect;
    }
}

