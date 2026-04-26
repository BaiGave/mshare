/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record DiskConfiguration(BlockStateProvider stateProvider, BlockPredicate target, IntProvider radius, int halfHeight) implements FeatureConfiguration
{
    public static final Codec<DiskConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)BlockStateProvider.CODEC.fieldOf("state_provider")).forGetter(DiskConfiguration::stateProvider), ((MapCodec)BlockPredicate.CODEC.fieldOf("target")).forGetter(DiskConfiguration::target), ((MapCodec)IntProviders.codec(0, 8).fieldOf("radius")).forGetter(DiskConfiguration::radius), ((MapCodec)Codec.intRange(0, 4).fieldOf("half_height")).forGetter(DiskConfiguration::halfHeight)).apply((Applicative<DiskConfiguration, ?>)i, DiskConfiguration::new));
}

