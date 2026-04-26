/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class NetherForestVegetationConfig
extends BlockPileConfiguration {
    public static final Codec<NetherForestVegetationConfig> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)BlockStateProvider.CODEC.fieldOf("state_provider")).forGetter(c -> c.stateProvider), ((MapCodec)ExtraCodecs.POSITIVE_INT.fieldOf("spread_width")).forGetter(c -> c.spreadWidth), ((MapCodec)ExtraCodecs.POSITIVE_INT.fieldOf("spread_height")).forGetter(c -> c.spreadHeight)).apply((Applicative<NetherForestVegetationConfig, ?>)i, NetherForestVegetationConfig::new));
    public final int spreadWidth;
    public final int spreadHeight;

    public NetherForestVegetationConfig(BlockStateProvider stateProvider, int spreadWidth, int spreadHeight) {
        super(stateProvider);
        this.spreadWidth = spreadWidth;
        this.spreadHeight = spreadHeight;
    }
}

