/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class HugeFungusConfiguration
implements FeatureConfiguration {
    public static final Codec<HugeFungusConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)BlockState.CODEC.fieldOf("valid_base_block")).forGetter(c -> c.validBaseState), ((MapCodec)BlockState.CODEC.fieldOf("stem_state")).forGetter(c -> c.stemState), ((MapCodec)BlockState.CODEC.fieldOf("hat_state")).forGetter(c -> c.hatState), ((MapCodec)BlockState.CODEC.fieldOf("decor_state")).forGetter(c -> c.decorState), ((MapCodec)BlockPredicate.CODEC.fieldOf("replaceable_blocks")).forGetter(c -> c.replaceableBlocks), ((MapCodec)Codec.BOOL.fieldOf("planted")).orElse(false).forGetter(c -> c.planted)).apply((Applicative<HugeFungusConfiguration, ?>)i, HugeFungusConfiguration::new));
    public final BlockState validBaseState;
    public final BlockState stemState;
    public final BlockState hatState;
    public final BlockState decorState;
    public final BlockPredicate replaceableBlocks;
    public final boolean planted;

    public HugeFungusConfiguration(BlockState validBaseState, BlockState stemState, BlockState hatState, BlockState decorState, BlockPredicate replaceableBlocks, boolean planted) {
        this.validBaseState = validBaseState;
        this.stemState = stemState;
        this.hatState = hatState;
        this.decorState = decorState;
        this.replaceableBlocks = replaceableBlocks;
        this.planted = planted;
    }
}

