/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

public class OreConfiguration
implements FeatureConfiguration {
    public static final Codec<OreConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)Codec.list(TargetBlockState.CODEC).fieldOf("targets")).forGetter(c -> c.targetStates), ((MapCodec)Codec.intRange(0, 64).fieldOf("size")).forGetter(c -> c.size), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("discard_chance_on_air_exposure")).forGetter(c -> Float.valueOf(c.discardChanceOnAirExposure))).apply((Applicative<OreConfiguration, ?>)i, OreConfiguration::new));
    public final List<TargetBlockState> targetStates;
    public final int size;
    public final float discardChanceOnAirExposure;

    public OreConfiguration(List<TargetBlockState> targetBlockStates, int size, float discardChanceOnAirExposure) {
        this.size = size;
        this.targetStates = targetBlockStates;
        this.discardChanceOnAirExposure = discardChanceOnAirExposure;
    }

    public OreConfiguration(List<TargetBlockState> targetBlockStates, int size) {
        this(targetBlockStates, size, 0.0f);
    }

    public OreConfiguration(RuleTest target, BlockState state, int size, float discardChanceOnAirExposure) {
        this(ImmutableList.of(new TargetBlockState(target, state)), size, discardChanceOnAirExposure);
    }

    public OreConfiguration(RuleTest target, BlockState state, int size) {
        this(ImmutableList.of(new TargetBlockState(target, state)), size, 0.0f);
    }

    public static TargetBlockState target(RuleTest rule, BlockState state) {
        return new TargetBlockState(rule, state);
    }

    public static class TargetBlockState {
        public static final Codec<TargetBlockState> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)RuleTest.CODEC.fieldOf("target")).forGetter(c -> c.target), ((MapCodec)BlockState.CODEC.fieldOf("state")).forGetter(c -> c.state)).apply((Applicative<TargetBlockState, ?>)i, TargetBlockState::new));
        public final RuleTest target;
        public final BlockState state;

        private TargetBlockState(RuleTest target, BlockState state) {
            this.target = target;
            this.state = state;
        }
    }
}

