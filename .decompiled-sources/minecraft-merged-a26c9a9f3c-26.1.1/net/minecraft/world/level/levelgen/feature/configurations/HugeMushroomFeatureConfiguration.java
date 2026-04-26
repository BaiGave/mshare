/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record HugeMushroomFeatureConfiguration(BlockStateProvider capProvider, BlockStateProvider stemProvider, int foliageRadius, BlockPredicate canPlaceOn) implements FeatureConfiguration
{
    public static final Codec<HugeMushroomFeatureConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)BlockStateProvider.CODEC.fieldOf("cap_provider")).forGetter(c -> c.capProvider), ((MapCodec)BlockStateProvider.CODEC.fieldOf("stem_provider")).forGetter(c -> c.stemProvider), ((MapCodec)Codec.INT.fieldOf("foliage_radius")).orElse(2).forGetter(c -> c.foliageRadius), ((MapCodec)BlockPredicate.CODEC.fieldOf("can_place_on")).forGetter(c -> c.canPlaceOn)).apply((Applicative<HugeMushroomFeatureConfiguration, ?>)i, HugeMushroomFeatureConfiguration::new));
}

