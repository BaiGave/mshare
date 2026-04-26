/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class UnderwaterMagmaConfiguration
implements FeatureConfiguration {
    public static final Codec<UnderwaterMagmaConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)Codec.intRange(0, 512).fieldOf("floor_search_range")).forGetter(c -> c.floorSearchRange), ((MapCodec)Codec.intRange(0, 64).fieldOf("placement_radius_around_floor")).forGetter(c -> c.placementRadiusAroundFloor), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("placement_probability_per_valid_position")).forGetter(c -> Float.valueOf(c.placementProbabilityPerValidPosition))).apply((Applicative<UnderwaterMagmaConfiguration, ?>)i, UnderwaterMagmaConfiguration::new));
    public final int floorSearchRange;
    public final int placementRadiusAroundFloor;
    public final float placementProbabilityPerValidPosition;

    public UnderwaterMagmaConfiguration(int floorSearchRange, int placementRadiusAroundFloor, float placementProbabilityPerValidPosition) {
        this.floorSearchRange = floorSearchRange;
        this.placementRadiusAroundFloor = placementRadiusAroundFloor;
        this.placementProbabilityPerValidPosition = placementProbabilityPerValidPosition;
    }
}

