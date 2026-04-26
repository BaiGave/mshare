/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;

public class DripstoneUtils {
    protected static double getDripstoneHeight(double xzDistanceFromCenter, double dripstoneRadius, double scale, double bluntness) {
        if (xzDistanceFromCenter < bluntness) {
            xzDistanceFromCenter = bluntness;
        }
        double cutoff = 0.384;
        double r = xzDistanceFromCenter / dripstoneRadius * 0.384;
        double part1 = 0.75 * Math.pow(r, 1.3333333333333333);
        double part2 = Math.pow(r, 0.6666666666666666);
        double part3 = 0.3333333333333333 * Math.log(r);
        double heightRelativeToMaxRadius = scale * (part1 - part2 - part3);
        heightRelativeToMaxRadius = Math.max(heightRelativeToMaxRadius, 0.0);
        return heightRelativeToMaxRadius / 0.384 * dripstoneRadius;
    }

    protected static boolean isCircleMostlyEmbeddedInStone(WorldGenLevel level, BlockPos center, int xzRadius) {
        if (DripstoneUtils.isEmptyOrWaterOrLava(level, center)) {
            return false;
        }
        float arcLength = 6.0f;
        float angleIncrement = 6.0f / (float)xzRadius;
        for (float angle = 0.0f; angle < (float)Math.PI * 2; angle += angleIncrement) {
            int dz;
            int dx = (int)(Mth.cos(angle) * (float)xzRadius);
            if (!DripstoneUtils.isEmptyOrWaterOrLava(level, center.offset(dx, 0, dz = (int)(Mth.sin(angle) * (float)xzRadius)))) continue;
            return false;
        }
        return true;
    }

    protected static boolean isEmptyOrWater(LevelAccessor level, BlockPos pos) {
        return level.isStateAtPosition(pos, DripstoneUtils::isEmptyOrWater);
    }

    protected static boolean isEmptyOrWaterOrLava(LevelAccessor level, BlockPos pos) {
        return level.isStateAtPosition(pos, DripstoneUtils::isEmptyOrWaterOrLava);
    }

    protected static void buildBaseToTipColumn(Direction direction, int totalLength, boolean mergedTip, Consumer<BlockState> consumer) {
        if (totalLength >= 3) {
            consumer.accept(DripstoneUtils.createPointedDripstone(direction, DripstoneThickness.BASE));
            for (int i = 0; i < totalLength - 3; ++i) {
                consumer.accept(DripstoneUtils.createPointedDripstone(direction, DripstoneThickness.MIDDLE));
            }
        }
        if (totalLength >= 2) {
            consumer.accept(DripstoneUtils.createPointedDripstone(direction, DripstoneThickness.FRUSTUM));
        }
        if (totalLength >= 1) {
            consumer.accept(DripstoneUtils.createPointedDripstone(direction, mergedTip ? DripstoneThickness.TIP_MERGE : DripstoneThickness.TIP));
        }
    }

    protected static void growPointedDripstone(LevelAccessor level, BlockPos startPos, Direction tipDirection, int height, boolean mergedTip) {
        if (!DripstoneUtils.isDripstoneBase(level.getBlockState(startPos.relative(tipDirection.getOpposite())))) {
            return;
        }
        BlockPos.MutableBlockPos pos = startPos.mutable();
        DripstoneUtils.buildBaseToTipColumn(tipDirection, height, mergedTip, state -> {
            if (state.is(Blocks.POINTED_DRIPSTONE)) {
                state = (BlockState)state.setValue(PointedDripstoneBlock.WATERLOGGED, level.isWaterAt(pos));
            }
            level.setBlock(pos, (BlockState)state, 2);
            pos.move(tipDirection);
        });
    }

    protected static boolean placeDripstoneBlockIfPossible(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(BlockTags.DRIPSTONE_REPLACEABLE)) {
            level.setBlock(pos, Blocks.DRIPSTONE_BLOCK.defaultBlockState(), 2);
            return true;
        }
        return false;
    }

    private static BlockState createPointedDripstone(Direction direction, DripstoneThickness thickness) {
        return (BlockState)((BlockState)Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, direction)).setValue(PointedDripstoneBlock.THICKNESS, thickness);
    }

    public static boolean isDripstoneBaseOrLava(BlockState state) {
        return DripstoneUtils.isDripstoneBase(state) || state.is(Blocks.LAVA);
    }

    public static boolean isDripstoneBase(BlockState state) {
        return state.is(Blocks.DRIPSTONE_BLOCK) || state.is(BlockTags.DRIPSTONE_REPLACEABLE);
    }

    public static boolean isEmptyOrWater(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER);
    }

    public static boolean isNeitherEmptyNorWater(BlockState state) {
        return !state.isAir() && !state.is(Blocks.WATER);
    }

    public static boolean isEmptyOrWaterOrLava(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.LAVA);
    }
}

