/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviders;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class LargeDripstoneConfiguration
implements FeatureConfiguration {
    public static final Codec<LargeDripstoneConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)Codec.intRange(1, 512).fieldOf("floor_to_ceiling_search_range")).orElse(30).forGetter(c -> c.floorToCeilingSearchRange), ((MapCodec)IntProviders.codec(1, 60).fieldOf("column_radius")).forGetter(c -> c.columnRadius), ((MapCodec)FloatProviders.codec(0.0f, 20.0f).fieldOf("height_scale")).forGetter(c -> c.heightScale), ((MapCodec)Codec.floatRange(0.1f, 1.0f).fieldOf("max_column_radius_to_cave_height_ratio")).forGetter(c -> Float.valueOf(c.maxColumnRadiusToCaveHeightRatio)), ((MapCodec)FloatProviders.codec(0.1f, 10.0f).fieldOf("stalactite_bluntness")).forGetter(c -> c.stalactiteBluntness), ((MapCodec)FloatProviders.codec(0.1f, 10.0f).fieldOf("stalagmite_bluntness")).forGetter(c -> c.stalagmiteBluntness), ((MapCodec)FloatProviders.codec(0.0f, 2.0f).fieldOf("wind_speed")).forGetter(c -> c.windSpeed), ((MapCodec)Codec.intRange(0, 100).fieldOf("min_radius_for_wind")).forGetter(c -> c.minRadiusForWind), ((MapCodec)Codec.floatRange(0.0f, 5.0f).fieldOf("min_bluntness_for_wind")).forGetter(c -> Float.valueOf(c.minBluntnessForWind))).apply((Applicative<LargeDripstoneConfiguration, ?>)i, LargeDripstoneConfiguration::new));
    public final int floorToCeilingSearchRange;
    public final IntProvider columnRadius;
    public final FloatProvider heightScale;
    public final float maxColumnRadiusToCaveHeightRatio;
    public final FloatProvider stalactiteBluntness;
    public final FloatProvider stalagmiteBluntness;
    public final FloatProvider windSpeed;
    public final int minRadiusForWind;
    public final float minBluntnessForWind;

    public LargeDripstoneConfiguration(int floorToCeilingSearchRange, IntProvider columnRadius, FloatProvider heightScale, float maxColumnRadiusToCaveHeightRatio, FloatProvider stalactiteBluntness, FloatProvider stalagmiteBluntness, FloatProvider windSpeed, int minRadiusForWind, float minBluntnessForWind) {
        this.floorToCeilingSearchRange = floorToCeilingSearchRange;
        this.columnRadius = columnRadius;
        this.heightScale = heightScale;
        this.maxColumnRadiusToCaveHeightRatio = maxColumnRadiusToCaveHeightRatio;
        this.stalactiteBluntness = stalactiteBluntness;
        this.stalagmiteBluntness = stalagmiteBluntness;
        this.windSpeed = windSpeed;
        this.minRadiusForWind = minRadiusForWind;
        this.minBluntnessForWind = minBluntnessForWind;
    }
}

