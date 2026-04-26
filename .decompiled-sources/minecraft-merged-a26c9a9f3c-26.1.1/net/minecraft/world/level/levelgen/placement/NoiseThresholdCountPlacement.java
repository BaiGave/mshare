/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;

public class NoiseThresholdCountPlacement
extends RepeatingPlacement {
    public static final MapCodec<NoiseThresholdCountPlacement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Codec.DOUBLE.fieldOf("noise_level")).forGetter(c -> c.noiseLevel), ((MapCodec)Codec.INT.fieldOf("below_noise")).forGetter(c -> c.belowNoise), ((MapCodec)Codec.INT.fieldOf("above_noise")).forGetter(c -> c.aboveNoise)).apply((Applicative<NoiseThresholdCountPlacement, ?>)i, NoiseThresholdCountPlacement::new));
    private final double noiseLevel;
    private final int belowNoise;
    private final int aboveNoise;

    private NoiseThresholdCountPlacement(double noiseLevel, int belowNoise, int aboveNoise) {
        this.noiseLevel = noiseLevel;
        this.belowNoise = belowNoise;
        this.aboveNoise = aboveNoise;
    }

    public static NoiseThresholdCountPlacement of(double noiseLevel, int belowNoise, int aboveNoise) {
        return new NoiseThresholdCountPlacement(noiseLevel, belowNoise, aboveNoise);
    }

    @Override
    protected int count(RandomSource random, BlockPos origin) {
        double flowerNoise = Biome.BIOME_INFO_NOISE.getValue((double)origin.getX() / 200.0, (double)origin.getZ() / 200.0, false);
        return flowerNoise < this.noiseLevel ? this.belowNoise : this.aboveNoise;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.NOISE_THRESHOLD_COUNT;
    }
}

