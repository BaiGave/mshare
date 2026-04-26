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
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class ColumnFeatureConfiguration
implements FeatureConfiguration {
    public static final Codec<ColumnFeatureConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)IntProviders.codec(0, 3).fieldOf("reach")).forGetter(c -> c.reach), ((MapCodec)IntProviders.codec(1, 10).fieldOf("height")).forGetter(c -> c.height)).apply((Applicative<ColumnFeatureConfiguration, ?>)i, ColumnFeatureConfiguration::new));
    private final IntProvider reach;
    private final IntProvider height;

    public ColumnFeatureConfiguration(IntProvider reach, IntProvider height) {
        this.reach = reach;
        this.height = height;
    }

    public IntProvider reach() {
        return this.reach;
    }

    public IntProvider height() {
        return this.height;
    }
}

