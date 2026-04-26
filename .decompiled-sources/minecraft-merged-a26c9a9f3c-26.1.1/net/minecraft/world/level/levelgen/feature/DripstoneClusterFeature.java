/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ClampedNormalFloat;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.DripstoneUtils;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DripstoneClusterConfiguration;

public class DripstoneClusterFeature
extends Feature<DripstoneClusterConfiguration> {
    public DripstoneClusterFeature(Codec<DripstoneClusterConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<DripstoneClusterConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        DripstoneClusterConfiguration config = context.config();
        RandomSource random = context.random();
        if (!DripstoneUtils.isEmptyOrWater(level, origin)) {
            return false;
        }
        int height = config.height.sample(random);
        float wetness = config.wetness.sample(random);
        float density = config.density.sample(random);
        int xRadius = config.radius.sample(random);
        int zRadius = config.radius.sample(random);
        for (int dx = -xRadius; dx <= xRadius; ++dx) {
            for (int dz = -zRadius; dz <= zRadius; ++dz) {
                double chanceOfStalagmiteOrStalactite = this.getChanceOfStalagmiteOrStalactite(xRadius, zRadius, dx, dz, config);
                BlockPos pos = origin.offset(dx, 0, dz);
                this.placeColumn(level, random, pos, dx, dz, wetness, chanceOfStalagmiteOrStalactite, height, density, config);
            }
        }
        return true;
    }

    private void placeColumn(WorldGenLevel level, RandomSource random, BlockPos pos, int dx, int dz, float chanceOfWater, double chanceOfStalagmiteOrStalactite, int clusterHeight, float density, DripstoneClusterConfiguration config) {
        boolean mergeTips;
        int actualStalagmiteHeight;
        int actualStalactiteHeight;
        int stalagmiteHeight;
        boolean wantStalagmite;
        int stalactiteHeight;
        boolean wantStalactite;
        Column column;
        boolean wantPool;
        Optional<Column> baseColumn = Column.scan(level, pos, config.floorToCeilingSearchRange, DripstoneUtils::isEmptyOrWater, DripstoneUtils::isNeitherEmptyNorWater);
        if (baseColumn.isEmpty()) {
            return;
        }
        OptionalInt ceiling = baseColumn.get().getCeiling();
        OptionalInt baseFloor = baseColumn.get().getFloor();
        if (ceiling.isEmpty() && baseFloor.isEmpty()) {
            return;
        }
        boolean bl = wantPool = random.nextFloat() < chanceOfWater;
        if (wantPool && baseFloor.isPresent() && this.canPlacePool(level, pos.atY(baseFloor.getAsInt()))) {
            int baseFloorY = baseFloor.getAsInt();
            column = baseColumn.get().withFloor(OptionalInt.of(baseFloorY - 1));
            level.setBlock(pos.atY(baseFloorY), Blocks.WATER.defaultBlockState(), 2);
        } else {
            column = baseColumn.get();
        }
        OptionalInt floor = column.getFloor();
        boolean bl2 = wantStalactite = random.nextDouble() < chanceOfStalagmiteOrStalactite;
        if (ceiling.isPresent() && wantStalactite && !this.isLava(level, pos.atY(ceiling.getAsInt()))) {
            int ceilingThickness = config.dripstoneBlockLayerThickness.sample(random);
            this.replaceBlocksWithDripstoneBlocks(level, pos.atY(ceiling.getAsInt()), ceilingThickness, Direction.UP);
            int maxHeightForThisColumn = floor.isPresent() ? Math.min(clusterHeight, ceiling.getAsInt() - floor.getAsInt()) : clusterHeight;
            stalactiteHeight = this.getDripstoneHeight(random, dx, dz, density, maxHeightForThisColumn, config);
        } else {
            stalactiteHeight = 0;
        }
        boolean bl3 = wantStalagmite = random.nextDouble() < chanceOfStalagmiteOrStalactite;
        if (floor.isPresent() && wantStalagmite && !this.isLava(level, pos.atY(floor.getAsInt()))) {
            int floorThickness = config.dripstoneBlockLayerThickness.sample(random);
            this.replaceBlocksWithDripstoneBlocks(level, pos.atY(floor.getAsInt()), floorThickness, Direction.DOWN);
            stalagmiteHeight = ceiling.isPresent() ? Math.max(0, stalactiteHeight + Mth.randomBetweenInclusive(random, -config.maxStalagmiteStalactiteHeightDiff, config.maxStalagmiteStalactiteHeightDiff)) : this.getDripstoneHeight(random, dx, dz, density, clusterHeight, config);
        } else {
            stalagmiteHeight = 0;
        }
        if (ceiling.isPresent() && floor.isPresent() && ceiling.getAsInt() - stalactiteHeight <= floor.getAsInt() + stalagmiteHeight) {
            int floorY = floor.getAsInt();
            int ceilingY = ceiling.getAsInt();
            int lowestStalactiteBottom = Math.max(ceilingY - stalactiteHeight, floorY + 1);
            int highestStalagmiteTop = Math.min(floorY + stalagmiteHeight, ceilingY - 1);
            int actualStalactiteBottom = Mth.randomBetweenInclusive(random, lowestStalactiteBottom, highestStalagmiteTop + 1);
            int actualStalagmiteTop = actualStalactiteBottom - 1;
            actualStalactiteHeight = ceilingY - actualStalactiteBottom;
            actualStalagmiteHeight = actualStalagmiteTop - floorY;
        } else {
            actualStalactiteHeight = stalactiteHeight;
            actualStalagmiteHeight = stalagmiteHeight;
        }
        boolean bl4 = mergeTips = random.nextBoolean() && actualStalactiteHeight > 0 && actualStalagmiteHeight > 0 && column.getHeight().isPresent() && actualStalactiteHeight + actualStalagmiteHeight == column.getHeight().getAsInt();
        if (ceiling.isPresent()) {
            DripstoneUtils.growPointedDripstone(level, pos.atY(ceiling.getAsInt() - 1), Direction.DOWN, actualStalactiteHeight, mergeTips);
        }
        if (floor.isPresent()) {
            DripstoneUtils.growPointedDripstone(level, pos.atY(floor.getAsInt() + 1), Direction.UP, actualStalagmiteHeight, mergeTips);
        }
    }

    private boolean isLava(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.LAVA);
    }

    private int getDripstoneHeight(RandomSource random, int dx, int dz, float density, int maxHeight, DripstoneClusterConfiguration config) {
        if (random.nextFloat() > density) {
            return 0;
        }
        int distanceFromCenter = Math.abs(dx) + Math.abs(dz);
        float heightMean = (float)Mth.clampedMap((double)distanceFromCenter, 0.0, (double)config.maxDistanceFromCenterAffectingHeightBias, (double)maxHeight / 2.0, 0.0);
        return (int)DripstoneClusterFeature.randomBetweenBiased(random, 0.0f, maxHeight, heightMean, config.heightDeviation);
    }

    private boolean canPlacePool(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.WATER) || state.is(Blocks.DRIPSTONE_BLOCK) || state.is(Blocks.POINTED_DRIPSTONE)) {
            return false;
        }
        if (level.getBlockState(pos.above()).getFluidState().is(FluidTags.WATER)) {
            return false;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (this.canBeAdjacentToWater(level, pos.relative(direction))) continue;
            return false;
        }
        return this.canBeAdjacentToWater(level, pos.below());
    }

    private boolean canBeAdjacentToWater(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.getFluidState().is(FluidTags.WATER);
    }

    private void replaceBlocksWithDripstoneBlocks(WorldGenLevel level, BlockPos firstPos, int maxCount, Direction direction) {
        BlockPos.MutableBlockPos pos = firstPos.mutable();
        for (int i = 0; i < maxCount; ++i) {
            if (!DripstoneUtils.placeDripstoneBlockIfPossible(level, pos)) {
                return;
            }
            pos.move(direction);
        }
    }

    private double getChanceOfStalagmiteOrStalactite(int xRadius, int zRadius, int dx, int dz, DripstoneClusterConfiguration config) {
        int xDistanceFromEdge = xRadius - Math.abs(dx);
        int zDistanceFromEdge = zRadius - Math.abs(dz);
        int distanceFromEdge = Math.min(xDistanceFromEdge, zDistanceFromEdge);
        return Mth.clampedMap(distanceFromEdge, 0.0f, config.maxDistanceFromEdgeAffectingChanceOfDripstoneColumn, config.chanceOfDripstoneColumnAtMaxDistanceFromCenter, 1.0f);
    }

    private static float randomBetweenBiased(RandomSource random, float min, float maxExclusive, float mean, float deviation) {
        return ClampedNormalFloat.sample(random, mean, deviation, min, maxExclusive);
    }
}

