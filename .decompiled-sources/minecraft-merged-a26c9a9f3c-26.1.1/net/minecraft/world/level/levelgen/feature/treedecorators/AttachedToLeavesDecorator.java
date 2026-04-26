/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class AttachedToLeavesDecorator
extends TreeDecorator {
    public static final MapCodec<AttachedToLeavesDecorator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("probability")).forGetter(p -> Float.valueOf(p.probability)), ((MapCodec)Codec.intRange(0, 16).fieldOf("exclusion_radius_xz")).forGetter(p -> p.exclusionRadiusXZ), ((MapCodec)Codec.intRange(0, 16).fieldOf("exclusion_radius_y")).forGetter(p -> p.exclusionRadiusY), ((MapCodec)BlockStateProvider.CODEC.fieldOf("block_provider")).forGetter(p -> p.blockProvider), ((MapCodec)Codec.intRange(1, 16).fieldOf("required_empty_blocks")).forGetter(p -> p.requiredEmptyBlocks), ((MapCodec)ExtraCodecs.nonEmptyList(Direction.CODEC.listOf()).fieldOf("directions")).forGetter(p -> p.directions)).apply((Applicative<AttachedToLeavesDecorator, ?>)i, AttachedToLeavesDecorator::new));
    protected final float probability;
    protected final int exclusionRadiusXZ;
    protected final int exclusionRadiusY;
    protected final BlockStateProvider blockProvider;
    protected final int requiredEmptyBlocks;
    protected final List<Direction> directions;

    public AttachedToLeavesDecorator(float probability, int exclusionRadiusXZ, int exclusionRadiusY, BlockStateProvider blockProvider, int requiredEmptyBlocks, List<Direction> directions) {
        this.probability = probability;
        this.exclusionRadiusXZ = exclusionRadiusXZ;
        this.exclusionRadiusY = exclusionRadiusY;
        this.blockProvider = blockProvider;
        this.requiredEmptyBlocks = requiredEmptyBlocks;
        this.directions = directions;
    }

    @Override
    public void place(TreeDecorator.Context context) {
        HashSet<BlockPos> propaguleBlacklist = new HashSet<BlockPos>();
        RandomSource random = context.random();
        for (BlockPos leafPos : Util.shuffledCopy(context.leaves(), random)) {
            Direction direction;
            BlockPos placementPos = leafPos.relative(direction = Util.getRandom(this.directions, random));
            if (propaguleBlacklist.contains(placementPos) || !(random.nextFloat() < this.probability) || !this.hasRequiredEmptyBlocks(context, leafPos, direction)) continue;
            BlockPos corner1 = placementPos.offset(-this.exclusionRadiusXZ, -this.exclusionRadiusY, -this.exclusionRadiusXZ);
            BlockPos corner2 = placementPos.offset(this.exclusionRadiusXZ, this.exclusionRadiusY, this.exclusionRadiusXZ);
            for (BlockPos inPos : BlockPos.betweenClosed(corner1, corner2)) {
                propaguleBlacklist.add(inPos.immutable());
            }
            context.setBlock(placementPos, this.blockProvider.getState(context.level(), random, placementPos));
        }
    }

    private boolean hasRequiredEmptyBlocks(TreeDecorator.Context context, BlockPos leafPos, Direction direction) {
        for (int i = 1; i <= this.requiredEmptyBlocks; ++i) {
            BlockPos offsetPos = leafPos.relative(direction, i);
            if (context.isAir(offsetPos)) continue;
            return false;
        }
        return true;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.ATTACHED_TO_LEAVES;
    }
}

