/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class ProbabilityFeatureConfiguration
implements FeatureConfiguration {
    public static final Codec<ProbabilityFeatureConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("probability")).forGetter(c -> Float.valueOf(c.probability))).apply((Applicative<ProbabilityFeatureConfiguration, ?>)i, ProbabilityFeatureConfiguration::new));
    public final float probability;

    public ProbabilityFeatureConfiguration(float probability) {
        this.probability = probability;
    }
}

