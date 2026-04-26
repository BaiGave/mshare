/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class PointedDripstoneConfiguration
implements FeatureConfiguration {
    public static final Codec<PointedDripstoneConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("chance_of_taller_dripstone")).orElse(Float.valueOf(0.2f)).forGetter(c -> Float.valueOf(c.chanceOfTallerDripstone)), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("chance_of_directional_spread")).orElse(Float.valueOf(0.7f)).forGetter(c -> Float.valueOf(c.chanceOfDirectionalSpread)), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("chance_of_spread_radius2")).orElse(Float.valueOf(0.5f)).forGetter(c -> Float.valueOf(c.chanceOfSpreadRadius2)), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("chance_of_spread_radius3")).orElse(Float.valueOf(0.5f)).forGetter(c -> Float.valueOf(c.chanceOfSpreadRadius3))).apply((Applicative<PointedDripstoneConfiguration, ?>)i, PointedDripstoneConfiguration::new));
    public final float chanceOfTallerDripstone;
    public final float chanceOfDirectionalSpread;
    public final float chanceOfSpreadRadius2;
    public final float chanceOfSpreadRadius3;

    public PointedDripstoneConfiguration(float chanceOfTallerDripstone, float chanceOfDirectionalSpread, float chanceOfSpreadRadius2, float chanceOfSpreadRadius3) {
        this.chanceOfTallerDripstone = chanceOfTallerDripstone;
        this.chanceOfDirectionalSpread = chanceOfDirectionalSpread;
        this.chanceOfSpreadRadius2 = chanceOfSpreadRadius2;
        this.chanceOfSpreadRadius3 = chanceOfSpreadRadius3;
    }
}

