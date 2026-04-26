/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class WeightedPlacedFeature {
    public static final Codec<WeightedPlacedFeature> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)PlacedFeature.CODEC.fieldOf("feature")).forGetter(f -> f.feature), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("chance")).forGetter(f -> Float.valueOf(f.chance))).apply((Applicative<WeightedPlacedFeature, ?>)i, WeightedPlacedFeature::new));
    public final Holder<PlacedFeature> feature;
    public final float chance;

    public WeightedPlacedFeature(Holder<PlacedFeature> feature, float chance) {
        this.feature = feature;
        this.chance = chance;
    }

    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        return this.feature.value().place(level, chunkGenerator, random, origin);
    }
}

