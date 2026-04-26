/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RandomBooleanFeatureConfiguration
implements FeatureConfiguration {
    public static final Codec<RandomBooleanFeatureConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)PlacedFeature.CODEC.fieldOf("feature_true")).forGetter(c -> c.featureTrue), ((MapCodec)PlacedFeature.CODEC.fieldOf("feature_false")).forGetter(c -> c.featureFalse)).apply((Applicative<RandomBooleanFeatureConfiguration, ?>)i, RandomBooleanFeatureConfiguration::new));
    public final Holder<PlacedFeature> featureTrue;
    public final Holder<PlacedFeature> featureFalse;

    public RandomBooleanFeatureConfiguration(Holder<PlacedFeature> featureTrue, Holder<PlacedFeature> featureFalse) {
        this.featureTrue = featureTrue;
        this.featureFalse = featureFalse;
    }

    @Override
    public Stream<Holder<ConfiguredFeature<?, ?>>> getSubFeatures() {
        return Stream.concat(this.featureTrue.value().getFeatures(), this.featureFalse.value().getFeatures());
    }
}

