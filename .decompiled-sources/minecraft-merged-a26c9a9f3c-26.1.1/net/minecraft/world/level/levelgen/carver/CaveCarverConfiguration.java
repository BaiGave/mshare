/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.carver;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviders;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CaveCarverConfiguration
extends CarverConfiguration {
    public static final Codec<CaveCarverConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(CarverConfiguration.CODEC.forGetter(c -> c), ((MapCodec)FloatProviders.CODEC.fieldOf("horizontal_radius_multiplier")).forGetter(c -> c.horizontalRadiusMultiplier), ((MapCodec)FloatProviders.CODEC.fieldOf("vertical_radius_multiplier")).forGetter(c -> c.verticalRadiusMultiplier), ((MapCodec)FloatProviders.codec(-1.0f, 1.0f).fieldOf("floor_level")).forGetter(c -> c.floorLevel)).apply((Applicative<CaveCarverConfiguration, ?>)i, CaveCarverConfiguration::new));
    public final FloatProvider horizontalRadiusMultiplier;
    public final FloatProvider verticalRadiusMultiplier;
    final FloatProvider floorLevel;

    public CaveCarverConfiguration(float probability, HeightProvider y, FloatProvider yScale, VerticalAnchor lavaLevel, CarverDebugSettings debugSettings, HolderSet<Block> replaceable, FloatProvider horizontalRadiusMultiplier, FloatProvider verticalRadiusMultiplier, FloatProvider floorLevel) {
        super(probability, y, yScale, lavaLevel, debugSettings, replaceable);
        this.horizontalRadiusMultiplier = horizontalRadiusMultiplier;
        this.verticalRadiusMultiplier = verticalRadiusMultiplier;
        this.floorLevel = floorLevel;
    }

    public CaveCarverConfiguration(float probability, HeightProvider y, FloatProvider yScale, VerticalAnchor lavaLevel, HolderSet<Block> replaceable, FloatProvider horizontalRadiusMultiplier, FloatProvider verticalRadiusMultiplier, FloatProvider floorLevel) {
        this(probability, y, yScale, lavaLevel, CarverDebugSettings.DEFAULT, replaceable, horizontalRadiusMultiplier, verticalRadiusMultiplier, floorLevel);
    }

    public CaveCarverConfiguration(CarverConfiguration carver, FloatProvider horizontalRadiusMultiplier, FloatProvider verticalRadiusMultiplier, FloatProvider floorLevel) {
        this(carver.probability, carver.y, carver.yScale, carver.lavaLevel, carver.debugSettings, carver.replaceable, horizontalRadiusMultiplier, verticalRadiusMultiplier, floorLevel);
    }
}

