/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.carver;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviders;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CanyonCarverConfiguration
extends CarverConfiguration {
    public static final Codec<CanyonCarverConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(CarverConfiguration.CODEC.forGetter(c -> c), ((MapCodec)FloatProviders.CODEC.fieldOf("vertical_rotation")).forGetter(c -> c.verticalRotation), ((MapCodec)CanyonShapeConfiguration.CODEC.fieldOf("shape")).forGetter(c -> c.shape)).apply((Applicative<CanyonCarverConfiguration, ?>)i, CanyonCarverConfiguration::new));
    public final FloatProvider verticalRotation;
    public final CanyonShapeConfiguration shape;

    public CanyonCarverConfiguration(float probability, HeightProvider y, FloatProvider yScale, VerticalAnchor lavaLevel, CarverDebugSettings debugSettings, HolderSet<Block> replaceable, FloatProvider verticalRotation, CanyonShapeConfiguration shape) {
        super(probability, y, yScale, lavaLevel, debugSettings, replaceable);
        this.verticalRotation = verticalRotation;
        this.shape = shape;
    }

    public CanyonCarverConfiguration(CarverConfiguration carver, FloatProvider distanceFactor, CanyonShapeConfiguration shape) {
        this(carver.probability, carver.y, carver.yScale, carver.lavaLevel, carver.debugSettings, carver.replaceable, distanceFactor, shape);
    }

    public static class CanyonShapeConfiguration {
        public static final Codec<CanyonShapeConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)FloatProviders.CODEC.fieldOf("distance_factor")).forGetter(c -> c.distanceFactor), ((MapCodec)FloatProviders.CODEC.fieldOf("thickness")).forGetter(c -> c.thickness), ((MapCodec)ExtraCodecs.POSITIVE_INT.fieldOf("width_smoothness")).forGetter(c -> c.widthSmoothness), ((MapCodec)FloatProviders.CODEC.fieldOf("horizontal_radius_factor")).forGetter(c -> c.horizontalRadiusFactor), ((MapCodec)Codec.FLOAT.fieldOf("vertical_radius_default_factor")).forGetter(c -> Float.valueOf(c.verticalRadiusDefaultFactor)), ((MapCodec)Codec.FLOAT.fieldOf("vertical_radius_center_factor")).forGetter(c -> Float.valueOf(c.verticalRadiusCenterFactor))).apply((Applicative<CanyonShapeConfiguration, ?>)i, CanyonShapeConfiguration::new));
        public final FloatProvider distanceFactor;
        public final FloatProvider thickness;
        public final int widthSmoothness;
        public final FloatProvider horizontalRadiusFactor;
        public final float verticalRadiusDefaultFactor;
        public final float verticalRadiusCenterFactor;

        public CanyonShapeConfiguration(FloatProvider distanceFactor, FloatProvider thickness, int widthSmoothness, FloatProvider horizontalRadiusFactor, float verticalRadiusDefaultFactor, float verticalRadiusCenterFactor) {
            this.widthSmoothness = widthSmoothness;
            this.horizontalRadiusFactor = horizontalRadiusFactor;
            this.verticalRadiusDefaultFactor = verticalRadiusDefaultFactor;
            this.verticalRadiusCenterFactor = verticalRadiusCenterFactor;
            this.distanceFactor = distanceFactor;
            this.thickness = thickness;
        }
    }
}

