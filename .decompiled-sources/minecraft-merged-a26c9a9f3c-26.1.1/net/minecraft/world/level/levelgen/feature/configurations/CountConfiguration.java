/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class CountConfiguration
implements FeatureConfiguration {
    public static final Codec<CountConfiguration> CODEC = ((MapCodec)IntProviders.codec(0, 256).fieldOf("count")).xmap(CountConfiguration::new, CountConfiguration::count).codec();
    private final IntProvider count;

    public CountConfiguration(int count) {
        this.count = ConstantInt.of(count);
    }

    public CountConfiguration(IntProvider count) {
        this.count = count;
    }

    public IntProvider count() {
        return this.count;
    }
}

