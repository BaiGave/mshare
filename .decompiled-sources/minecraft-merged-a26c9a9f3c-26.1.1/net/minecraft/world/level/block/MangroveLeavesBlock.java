/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MangroveLeavesBlock
extends TintedParticleLeavesBlock
implements BonemealableBlock {
    public static final MapCodec<MangroveLeavesBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)ExtraCodecs.floatRange(0.0f, 1.0f).fieldOf("leaf_particle_chance")).forGetter(e -> Float.valueOf(e.leafParticleChance)), MangroveLeavesBlock.propertiesCodec()).apply((Applicative<MangroveLeavesBlock, ?>)i, MangroveLeavesBlock::new));

    public MapCodec<MangroveLeavesBlock> codec() {
        return CODEC;
    }

    public MangroveLeavesBlock(float leafParticleChance, BlockBehaviour.Properties properties) {
        super(leafParticleChance, properties);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos.below()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos.below(), MangrovePropaguleBlock.createNewHangingPropagule(), 2);
    }

    @Override
    public BlockPos getParticlePos(BlockPos blockPos) {
        return blockPos.below();
    }
}

