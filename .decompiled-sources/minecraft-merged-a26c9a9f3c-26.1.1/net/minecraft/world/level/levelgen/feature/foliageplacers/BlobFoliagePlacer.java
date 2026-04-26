/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class BlobFoliagePlacer
extends FoliagePlacer {
    public static final MapCodec<BlobFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(i -> BlobFoliagePlacer.blobParts(i).apply((Applicative)i, BlobFoliagePlacer::new));
    protected final int height;

    protected static <P extends BlobFoliagePlacer> Products.P3<RecordCodecBuilder.Mu<P>, IntProvider, IntProvider, Integer> blobParts(RecordCodecBuilder.Instance<P> instance) {
        return BlobFoliagePlacer.foliagePlacerParts(instance).and(((MapCodec)Codec.intRange(0, 16).fieldOf("height")).forGetter(p -> p.height));
    }

    public BlobFoliagePlacer(IntProvider radius, IntProvider offset, int height) {
        super(radius, offset);
        this.height = height;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.BLOB_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(WorldGenLevel level, FoliagePlacer.FoliageSetter foliageSetter, RandomSource random, TreeConfiguration config, int treeHeight, FoliagePlacer.FoliageAttachment foliageAttachment, int foliageHeight, int leafRadius, int offset) {
        for (int yo = offset; yo >= offset - foliageHeight; --yo) {
            int currentRadius = Math.max(leafRadius + foliageAttachment.radiusOffset() - 1 - yo / 2, 0);
            this.placeLeavesRow(level, foliageSetter, random, config, foliageAttachment.pos(), currentRadius, yo, foliageAttachment.doubleTrunk());
        }
    }

    @Override
    public int foliageHeight(RandomSource random, int treeHeight, TreeConfiguration config) {
        return this.height;
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource random, int dx, int y, int dz, int currentRadius, boolean doubleTrunk) {
        return dx == currentRadius && dz == currentRadius && (random.nextInt(2) == 0 || y == 0);
    }
}

