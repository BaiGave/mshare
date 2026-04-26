/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class SurfaceRelativeThresholdFilter
extends PlacementFilter {
    public static final MapCodec<SurfaceRelativeThresholdFilter> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Heightmap.Types.CODEC.fieldOf("heightmap")).forGetter(c -> c.heightmap), Codec.INT.optionalFieldOf("min_inclusive", Integer.MIN_VALUE).forGetter(c -> c.minInclusive), Codec.INT.optionalFieldOf("max_inclusive", Integer.MAX_VALUE).forGetter(c -> c.maxInclusive)).apply((Applicative<SurfaceRelativeThresholdFilter, ?>)i, SurfaceRelativeThresholdFilter::new));
    private final Heightmap.Types heightmap;
    private final int minInclusive;
    private final int maxInclusive;

    private SurfaceRelativeThresholdFilter(Heightmap.Types heightmap, int minInclusive, int maxInclusive) {
        this.heightmap = heightmap;
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
    }

    public static SurfaceRelativeThresholdFilter of(Heightmap.Types heightmap, int minInclusive, int maxInclusive) {
        return new SurfaceRelativeThresholdFilter(heightmap, minInclusive, maxInclusive);
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos origin) {
        long surfaceY = context.getHeight(this.heightmap, origin.getX(), origin.getZ());
        long minY = surfaceY + (long)this.minInclusive;
        long maxY = surfaceY + (long)this.maxInclusive;
        return minY <= (long)origin.getY() && (long)origin.getY() <= maxY;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.SURFACE_RELATIVE_THRESHOLD_FILTER;
    }
}

