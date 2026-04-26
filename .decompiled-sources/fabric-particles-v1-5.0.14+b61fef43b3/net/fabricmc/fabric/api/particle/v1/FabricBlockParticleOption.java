/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.particle.v1;

import net.fabricmc.fabric.impl.particle.BlockParticleOptionFactoryImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public interface FabricBlockParticleOption {
    public static BlockParticleOption create(ParticleType<BlockParticleOption> type, BlockState blockState, @Nullable BlockPos blockPos) {
        return BlockParticleOptionFactoryImpl.create(type, blockState, blockPos);
    }

    default public @Nullable BlockPos getBlockPos() {
        throw new AssertionError((Object)"Implemented in Mixin");
    }
}

