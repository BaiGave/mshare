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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RootSystemConfiguration
implements FeatureConfiguration {
    public static final Codec<RootSystemConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)PlacedFeature.CODEC.fieldOf("feature")).forGetter(c -> c.treeFeature), ((MapCodec)Codec.intRange(1, 64).fieldOf("required_vertical_space_for_tree")).forGetter(c -> c.requiredVerticalSpaceForTree), ((MapCodec)Codec.intRange(1, 64).fieldOf("root_radius")).forGetter(c -> c.rootRadius), ((MapCodec)TagKey.hashedCodec(Registries.BLOCK).fieldOf("root_replaceable")).forGetter(c -> c.rootReplaceable), ((MapCodec)BlockStateProvider.CODEC.fieldOf("root_state_provider")).forGetter(c -> c.rootStateProvider), ((MapCodec)Codec.intRange(1, 256).fieldOf("root_placement_attempts")).forGetter(c -> c.rootPlacementAttempts), ((MapCodec)Codec.intRange(1, 4096).fieldOf("root_column_max_height")).forGetter(c -> c.rootColumnMaxHeight), ((MapCodec)Codec.intRange(1, 64).fieldOf("hanging_root_radius")).forGetter(c -> c.hangingRootRadius), ((MapCodec)Codec.intRange(1, 16).fieldOf("hanging_roots_vertical_span")).forGetter(c -> c.hangingRootsVerticalSpan), ((MapCodec)BlockStateProvider.CODEC.fieldOf("hanging_root_state_provider")).forGetter(c -> c.hangingRootStateProvider), ((MapCodec)Codec.intRange(1, 256).fieldOf("hanging_root_placement_attempts")).forGetter(c -> c.hangingRootPlacementAttempts), ((MapCodec)Codec.intRange(1, 64).fieldOf("allowed_vertical_water_for_tree")).forGetter(c -> c.allowedVerticalWaterForTree), ((MapCodec)BlockPredicate.CODEC.fieldOf("allowed_tree_position")).forGetter(c -> c.allowedTreePosition)).apply((Applicative<RootSystemConfiguration, ?>)i, RootSystemConfiguration::new));
    public final Holder<PlacedFeature> treeFeature;
    public final int requiredVerticalSpaceForTree;
    public final int rootRadius;
    public final TagKey<Block> rootReplaceable;
    public final BlockStateProvider rootStateProvider;
    public final int rootPlacementAttempts;
    public final int rootColumnMaxHeight;
    public final int hangingRootRadius;
    public final int hangingRootsVerticalSpan;
    public final BlockStateProvider hangingRootStateProvider;
    public final int hangingRootPlacementAttempts;
    public final int allowedVerticalWaterForTree;
    public final BlockPredicate allowedTreePosition;

    public RootSystemConfiguration(Holder<PlacedFeature> treeFeature, int requiredVerticalSpaceForTree, int rootRadius, TagKey<Block> rootReplaceable, BlockStateProvider rootStateProvider, int rootPlacementAttempts, int rootColumnMaxHeight, int hangingRootRadius, int hangingRootsVerticalSpan, BlockStateProvider hangingRootStateProvider, int hangingRootPlacementAttempts, int allowedVerticalWaterForTree, BlockPredicate allowedTreePosition) {
        this.treeFeature = treeFeature;
        this.requiredVerticalSpaceForTree = requiredVerticalSpaceForTree;
        this.rootRadius = rootRadius;
        this.rootReplaceable = rootReplaceable;
        this.rootStateProvider = rootStateProvider;
        this.rootPlacementAttempts = rootPlacementAttempts;
        this.rootColumnMaxHeight = rootColumnMaxHeight;
        this.hangingRootRadius = hangingRootRadius;
        this.hangingRootsVerticalSpan = hangingRootsVerticalSpan;
        this.hangingRootStateProvider = hangingRootStateProvider;
        this.hangingRootPlacementAttempts = hangingRootPlacementAttempts;
        this.allowedVerticalWaterForTree = allowedVerticalWaterForTree;
        this.allowedTreePosition = allowedTreePosition;
    }
}

