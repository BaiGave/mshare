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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class ReplaceSphereConfiguration
implements FeatureConfiguration {
    public static final Codec<ReplaceSphereConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)BlockState.CODEC.fieldOf("target")).forGetter(c -> c.targetState), ((MapCodec)BlockState.CODEC.fieldOf("state")).forGetter(c -> c.replaceState), ((MapCodec)IntProviders.codec(0, 12).fieldOf("radius")).forGetter(c -> c.radius)).apply((Applicative<ReplaceSphereConfiguration, ?>)i, ReplaceSphereConfiguration::new));
    public final BlockState targetState;
    public final BlockState replaceState;
    private final IntProvider radius;

    public ReplaceSphereConfiguration(BlockState targetState, BlockState replaceState, IntProvider radius) {
        this.targetState = targetState;
        this.replaceState = replaceState;
        this.radius = radius;
    }

    public IntProvider radius() {
        return this.radius;
    }
}

