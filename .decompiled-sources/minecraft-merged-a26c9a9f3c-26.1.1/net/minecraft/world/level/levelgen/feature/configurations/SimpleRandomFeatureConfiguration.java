/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class SimpleRandomFeatureConfiguration
implements FeatureConfiguration {
    public static final Codec<SimpleRandomFeatureConfiguration> CODEC = ((MapCodec)ExtraCodecs.nonEmptyHolderSet(PlacedFeature.LIST_CODEC).fieldOf("features")).xmap(SimpleRandomFeatureConfiguration::new, c -> c.features).codec();
    public final HolderSet<PlacedFeature> features;

    public SimpleRandomFeatureConfiguration(HolderSet<PlacedFeature> features) {
        this.features = features;
    }

    @Override
    public Stream<Holder<ConfiguredFeature<?, ?>>> getSubFeatures() {
        return this.features.stream().flatMap(f -> ((PlacedFeature)f.value()).getFeatures());
    }
}

