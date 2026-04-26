/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.material.FluidState;

public class SpringConfiguration
implements FeatureConfiguration {
    public static final Codec<SpringConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)FluidState.CODEC.fieldOf("state")).forGetter(c -> c.state), ((MapCodec)Codec.BOOL.fieldOf("requires_block_below")).orElse(true).forGetter(c -> c.requiresBlockBelow), ((MapCodec)Codec.INT.fieldOf("rock_count")).orElse(4).forGetter(c -> c.rockCount), ((MapCodec)Codec.INT.fieldOf("hole_count")).orElse(1).forGetter(c -> c.holeCount), ((MapCodec)RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("valid_blocks")).forGetter(c -> c.validBlocks)).apply((Applicative<SpringConfiguration, ?>)i, SpringConfiguration::new));
    public final FluidState state;
    public final boolean requiresBlockBelow;
    public final int rockCount;
    public final int holeCount;
    public final HolderSet<Block> validBlocks;

    public SpringConfiguration(FluidState state, boolean requiresBlockBelow, int rockCount, int holeCount, HolderSet<Block> validBlocks) {
        this.state = state;
        this.requiresBlockBelow = requiresBlockBelow;
        this.rockCount = rockCount;
        this.holeCount = holeCount;
        this.validBlocks = validBlocks;
    }
}

