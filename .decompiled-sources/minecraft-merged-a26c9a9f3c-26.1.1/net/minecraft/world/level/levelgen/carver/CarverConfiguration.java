/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.carver;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviders;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CarverConfiguration
extends ProbabilityFeatureConfiguration {
    public static final MapCodec<CarverConfiguration> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("probability")).forGetter(c -> Float.valueOf(c.probability)), ((MapCodec)HeightProvider.CODEC.fieldOf("y")).forGetter(c -> c.y), ((MapCodec)FloatProviders.CODEC.fieldOf("yScale")).forGetter(c -> c.yScale), ((MapCodec)VerticalAnchor.CODEC.fieldOf("lava_level")).forGetter(c -> c.lavaLevel), CarverDebugSettings.CODEC.optionalFieldOf("debug_settings", CarverDebugSettings.DEFAULT).forGetter(c -> c.debugSettings), ((MapCodec)RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("replaceable")).forGetter(c -> c.replaceable)).apply((Applicative<CarverConfiguration, ?>)i, CarverConfiguration::new));
    public final HeightProvider y;
    public final FloatProvider yScale;
    public final VerticalAnchor lavaLevel;
    public final CarverDebugSettings debugSettings;
    public final HolderSet<Block> replaceable;

    public CarverConfiguration(float probability, HeightProvider y, FloatProvider yScale, VerticalAnchor lavaLevel, CarverDebugSettings debugSettings, HolderSet<Block> replaceable) {
        super(probability);
        this.y = y;
        this.yScale = yScale;
        this.lavaLevel = lavaLevel;
        this.debugSettings = debugSettings;
        this.replaceable = replaceable;
    }
}

