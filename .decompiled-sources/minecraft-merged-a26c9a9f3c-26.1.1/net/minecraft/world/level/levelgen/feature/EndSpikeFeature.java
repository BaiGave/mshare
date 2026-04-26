/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.EndSpikeConfiguration;
import net.minecraft.world.phys.AABB;

public class EndSpikeFeature
extends Feature<EndSpikeConfiguration> {
    public static final int NUMBER_OF_SPIKES = 10;
    private static final int SPIKE_DISTANCE = 42;
    private static final LoadingCache<Long, List<EndSpike>> SPIKE_CACHE = CacheBuilder.newBuilder().expireAfterWrite(5L, TimeUnit.MINUTES).build(new SpikeCacheLoader());

    public EndSpikeFeature(Codec<EndSpikeConfiguration> codec) {
        super(codec);
    }

    public static List<EndSpike> getSpikesForLevel(WorldGenLevel level) {
        RandomSource random = RandomSource.createThreadLocalInstance(level.getSeed());
        long key = random.nextLong() & 0xFFFFL;
        return SPIKE_CACHE.getUnchecked(key);
    }

    @Override
    public boolean place(FeaturePlaceContext<EndSpikeConfiguration> context) {
        EndSpikeConfiguration config = context.config();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        List<EndSpike> spikes = config.getSpikes();
        if (spikes.isEmpty()) {
            spikes = EndSpikeFeature.getSpikesForLevel(level);
        }
        for (EndSpike spike : spikes) {
            if (!spike.isCenterWithinChunk(origin)) continue;
            this.placeSpike(level, random, config, spike);
        }
        return true;
    }

    private void placeSpike(ServerLevelAccessor level, RandomSource random, EndSpikeConfiguration config, EndSpike spike) {
        EndCrystal endCrystal;
        int radius = spike.getRadius();
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(spike.getCenterX() - radius, level.getMinY(), spike.getCenterZ() - radius), new BlockPos(spike.getCenterX() + radius, spike.getHeight() + 10, spike.getCenterZ() + radius))) {
            if (pos.distToLowCornerSqr(spike.getCenterX(), pos.getY(), spike.getCenterZ()) <= (double)(radius * radius + 1) && pos.getY() < spike.getHeight()) {
                this.setBlock(level, pos, Blocks.OBSIDIAN.defaultBlockState());
                continue;
            }
            if (pos.getY() <= 65) continue;
            this.setBlock(level, pos, Blocks.AIR.defaultBlockState());
        }
        if (spike.isGuarded()) {
            int start = -2;
            int end = 2;
            int yEnd = 3;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int dx = -2; dx <= 2; ++dx) {
                for (int dz = -2; dz <= 2; ++dz) {
                    for (int dy = 0; dy <= 3; ++dy) {
                        boolean top;
                        boolean isXSide = Mth.abs(dx) == 2;
                        boolean isZSide = Mth.abs(dz) == 2;
                        boolean bl = top = dy == 3;
                        if (!isXSide && !isZSide && !top) continue;
                        boolean xEdge = dx == -2 || dx == 2 || top;
                        boolean zEdge = dz == -2 || dz == 2 || top;
                        BlockState state = (BlockState)((BlockState)((BlockState)((BlockState)Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, xEdge && dz != -2)).setValue(IronBarsBlock.SOUTH, xEdge && dz != 2)).setValue(IronBarsBlock.WEST, zEdge && dx != -2)).setValue(IronBarsBlock.EAST, zEdge && dx != 2);
                        this.setBlock(level, pos.set(spike.getCenterX() + dx, spike.getHeight() + dy, spike.getCenterZ() + dz), state);
                    }
                }
            }
        }
        if ((endCrystal = EntityType.END_CRYSTAL.create(level.getLevel(), EntitySpawnReason.STRUCTURE)) != null) {
            endCrystal.setBeamTarget(config.getCrystalBeamTarget());
            endCrystal.setInvulnerable(config.isCrystalInvulnerable());
            endCrystal.snapTo((double)spike.getCenterX() + 0.5, spike.getHeight() + 1, (double)spike.getCenterZ() + 0.5, random.nextFloat() * 360.0f, 0.0f);
            level.addFreshEntity(endCrystal);
            BlockPos crystalPos = endCrystal.blockPosition();
            this.setBlock(level, crystalPos.below(), Blocks.BEDROCK.defaultBlockState());
            this.setBlock(level, crystalPos, FireBlock.getState(level, crystalPos));
        }
    }

    public static class EndSpike {
        public static final Codec<EndSpike> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)Codec.INT.fieldOf("centerX")).orElse(0).forGetter(s -> s.centerX), ((MapCodec)Codec.INT.fieldOf("centerZ")).orElse(0).forGetter(s -> s.centerZ), ((MapCodec)Codec.INT.fieldOf("radius")).orElse(0).forGetter(s -> s.radius), ((MapCodec)Codec.INT.fieldOf("height")).orElse(0).forGetter(s -> s.height), ((MapCodec)Codec.BOOL.fieldOf("guarded")).orElse(false).forGetter(s -> s.guarded)).apply((Applicative<EndSpike, ?>)i, EndSpike::new));
        private final int centerX;
        private final int centerZ;
        private final int radius;
        private final int height;
        private final boolean guarded;
        private final AABB topBoundingBox;

        public EndSpike(int centerX, int centerZ, int radius, int height, boolean guarded) {
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.radius = radius;
            this.height = height;
            this.guarded = guarded;
            this.topBoundingBox = new AABB(centerX - radius, DimensionType.MIN_Y, centerZ - radius, centerX + radius, DimensionType.MAX_Y, centerZ + radius);
        }

        public boolean isCenterWithinChunk(BlockPos chunkOrigin) {
            return SectionPos.blockToSectionCoord(chunkOrigin.getX()) == SectionPos.blockToSectionCoord(this.centerX) && SectionPos.blockToSectionCoord(chunkOrigin.getZ()) == SectionPos.blockToSectionCoord(this.centerZ);
        }

        public int getCenterX() {
            return this.centerX;
        }

        public int getCenterZ() {
            return this.centerZ;
        }

        public int getRadius() {
            return this.radius;
        }

        public int getHeight() {
            return this.height;
        }

        public boolean isGuarded() {
            return this.guarded;
        }

        public AABB getTopBoundingBox() {
            return this.topBoundingBox;
        }
    }

    private static class SpikeCacheLoader
    extends CacheLoader<Long, List<EndSpike>> {
        private SpikeCacheLoader() {
        }

        @Override
        public List<EndSpike> load(Long seed) {
            IntArrayList sizes = Util.toShuffledList(IntStream.range(0, 10), RandomSource.createThreadLocalInstance(seed));
            ArrayList<EndSpike> result = Lists.newArrayList();
            for (int i = 0; i < 10; ++i) {
                int x = Mth.floor(42.0 * Math.cos(2.0 * (-Math.PI + 0.3141592653589793 * (double)i)));
                int z = Mth.floor(42.0 * Math.sin(2.0 * (-Math.PI + 0.3141592653589793 * (double)i)));
                int size = sizes.get(i);
                int radius = 2 + size / 3;
                int height = 76 + size * 3;
                boolean guarded = size == 1 || size == 2;
                result.add(new EndSpike(x, z, radius, height, guarded));
            }
            return result;
        }
    }
}

