/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseBasedStateProvider;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseThresholdProvider
extends NoiseBasedStateProvider {
    public static final MapCodec<NoiseThresholdProvider> CODEC = RecordCodecBuilder.mapCodec(i -> NoiseThresholdProvider.noiseCodec(i).and(i.group(((MapCodec)Codec.floatRange(-1.0f, 1.0f).fieldOf("threshold")).forGetter(p -> Float.valueOf(p.threshold)), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("high_chance")).forGetter(p -> Float.valueOf(p.highChance)), ((MapCodec)BlockState.CODEC.fieldOf("default_state")).forGetter(p -> p.defaultState), ((MapCodec)ExtraCodecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("low_states")).forGetter(p -> p.lowStates), ((MapCodec)ExtraCodecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("high_states")).forGetter(p -> p.highStates))).apply((Applicative<NoiseThresholdProvider, ?>)i, NoiseThresholdProvider::new));
    private final float threshold;
    private final float highChance;
    private final BlockState defaultState;
    private final List<BlockState> lowStates;
    private final List<BlockState> highStates;

    public NoiseThresholdProvider(long seed, NormalNoise.NoiseParameters parameters, float scale, float threshold, float highChance, BlockState defaultState, List<BlockState> lowStates, List<BlockState> highStates) {
        super(seed, parameters, scale);
        this.threshold = threshold;
        this.highChance = highChance;
        this.defaultState = defaultState;
        this.lowStates = lowStates;
        this.highStates = highStates;
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.NOISE_THRESHOLD_PROVIDER;
    }

    @Override
    public BlockState getState(WorldGenLevel level, RandomSource random, BlockPos pos) {
        double localValue = this.getNoiseValue(pos, this.scale);
        if (localValue < (double)this.threshold) {
            return Util.getRandom(this.lowStates, random);
        }
        if (random.nextFloat() < this.highChance) {
            return Util.getRandom(this.highStates, random);
        }
        return this.defaultState;
    }
}

