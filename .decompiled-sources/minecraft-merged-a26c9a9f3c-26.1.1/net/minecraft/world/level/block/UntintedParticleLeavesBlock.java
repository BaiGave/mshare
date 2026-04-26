/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class UntintedParticleLeavesBlock
extends LeavesBlock {
    public static final MapCodec<UntintedParticleLeavesBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)ExtraCodecs.floatRange(0.0f, 1.0f).fieldOf("leaf_particle_chance")).forGetter(e -> Float.valueOf(e.leafParticleChance)), ((MapCodec)ParticleTypes.CODEC.fieldOf("leaf_particle")).forGetter(e -> e.leafParticle), UntintedParticleLeavesBlock.propertiesCodec()).apply((Applicative<UntintedParticleLeavesBlock, ?>)i, UntintedParticleLeavesBlock::new));
    protected final ParticleOptions leafParticle;

    public UntintedParticleLeavesBlock(float leafParticleChance, ParticleOptions leafParticle, BlockBehaviour.Properties properties) {
        super(leafParticleChance, properties);
        this.leafParticle = leafParticle;
    }

    @Override
    protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
        ParticleUtils.spawnParticleBelow(level, pos, random, this.leafParticle);
    }

    public MapCodec<UntintedParticleLeavesBlock> codec() {
        return CODEC;
    }
}

