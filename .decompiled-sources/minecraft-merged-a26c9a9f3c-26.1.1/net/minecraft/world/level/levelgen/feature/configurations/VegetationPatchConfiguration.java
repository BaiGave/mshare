/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class VegetationPatchConfiguration
implements FeatureConfiguration {
    public static final Codec<VegetationPatchConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)TagKey.hashedCodec(Registries.BLOCK).fieldOf("replaceable")).forGetter(c -> c.replaceable), ((MapCodec)BlockStateProvider.CODEC.fieldOf("ground_state")).forGetter(c -> c.groundState), ((MapCodec)PlacedFeature.CODEC.fieldOf("vegetation_feature")).forGetter(c -> c.vegetationFeature), ((MapCodec)CaveSurface.CODEC.fieldOf("surface")).forGetter(c -> c.surface), ((MapCodec)IntProviders.codec(1, 128).fieldOf("depth")).forGetter(c -> c.depth), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("extra_bottom_block_chance")).forGetter(c -> Float.valueOf(c.extraBottomBlockChance)), ((MapCodec)Codec.intRange(1, 256).fieldOf("vertical_range")).forGetter(c -> c.verticalRange), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("vegetation_chance")).forGetter(c -> Float.valueOf(c.vegetationChance)), ((MapCodec)IntProviders.CODEC.fieldOf("xz_radius")).forGetter(c -> c.xzRadius), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("extra_edge_column_chance")).forGetter(c -> Float.valueOf(c.extraEdgeColumnChance))).apply((Applicative<VegetationPatchConfiguration, ?>)i, VegetationPatchConfiguration::new));
    public final TagKey<Block> replaceable;
    public final BlockStateProvider groundState;
    public final Holder<PlacedFeature> vegetationFeature;
    public final CaveSurface surface;
    public final IntProvider depth;
    public final float extraBottomBlockChance;
    public final int verticalRange;
    public final float vegetationChance;
    public final IntProvider xzRadius;
    public final float extraEdgeColumnChance;

    public VegetationPatchConfiguration(TagKey<Block> replaceable, BlockStateProvider groundState, Holder<PlacedFeature> vegetationFeature, CaveSurface surface, IntProvider depth, float extraBottomBlockChance, int verticalRange, float vegetationChance, IntProvider xzRadius, float extraEdgeColumnChance) {
        this.replaceable = replaceable;
        this.groundState = groundState;
        this.vegetationFeature = vegetationFeature;
        this.surface = surface;
        this.depth = depth;
        this.extraBottomBlockChance = extraBottomBlockChance;
        this.verticalRange = verticalRange;
        this.vegetationChance = vegetationChance;
        this.xzRadius = xzRadius;
        this.extraEdgeColumnChance = extraEdgeColumnChance;
    }
}

