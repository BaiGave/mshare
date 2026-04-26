/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;

public class FallenTreeConfiguration
implements FeatureConfiguration {
    public static final Codec<FallenTreeConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)BlockStateProvider.CODEC.fieldOf("trunk_provider")).forGetter(c -> c.trunkProvider), ((MapCodec)IntProviders.codec(0, 16).fieldOf("log_length")).forGetter(t -> t.logLength), ((MapCodec)TreeDecorator.CODEC.listOf().fieldOf("stump_decorators")).forGetter(c -> c.stumpDecorators), ((MapCodec)TreeDecorator.CODEC.listOf().fieldOf("log_decorators")).forGetter(c -> c.logDecorators)).apply((Applicative<FallenTreeConfiguration, ?>)i, FallenTreeConfiguration::new));
    public final BlockStateProvider trunkProvider;
    public final IntProvider logLength;
    public final List<TreeDecorator> stumpDecorators;
    public final List<TreeDecorator> logDecorators;

    protected FallenTreeConfiguration(BlockStateProvider trunkProvider, IntProvider logLength, List<TreeDecorator> stumpDecorators, List<TreeDecorator> logDecorators) {
        this.trunkProvider = trunkProvider;
        this.logLength = logLength;
        this.stumpDecorators = stumpDecorators;
        this.logDecorators = logDecorators;
    }

    public static class FallenTreeConfigurationBuilder {
        private final BlockStateProvider trunkProvider;
        private final IntProvider logLength;
        private List<TreeDecorator> stumpDecorators = new ArrayList<TreeDecorator>();
        private List<TreeDecorator> logDecorators = new ArrayList<TreeDecorator>();

        public FallenTreeConfigurationBuilder(BlockStateProvider trunkProvider, IntProvider logLength) {
            this.trunkProvider = trunkProvider;
            this.logLength = logLength;
        }

        public FallenTreeConfigurationBuilder stumpDecorators(List<TreeDecorator> stumpDecorators) {
            this.stumpDecorators = stumpDecorators;
            return this;
        }

        public FallenTreeConfigurationBuilder logDecorators(List<TreeDecorator> logDecorators) {
            this.logDecorators = logDecorators;
            return this;
        }

        public FallenTreeConfiguration build() {
            return new FallenTreeConfiguration(this.trunkProvider, this.logLength, this.stumpDecorators, this.logDecorators);
        }
    }
}

