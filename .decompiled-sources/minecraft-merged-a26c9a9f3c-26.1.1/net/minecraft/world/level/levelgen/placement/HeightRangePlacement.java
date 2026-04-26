/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class HeightRangePlacement
extends PlacementModifier {
    public static final MapCodec<HeightRangePlacement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)HeightProvider.CODEC.fieldOf("height")).forGetter(c -> c.height)).apply((Applicative<HeightRangePlacement, ?>)i, HeightRangePlacement::new));
    private final HeightProvider height;

    private HeightRangePlacement(HeightProvider height) {
        this.height = height;
    }

    public static HeightRangePlacement of(HeightProvider height) {
        return new HeightRangePlacement(height);
    }

    public static HeightRangePlacement uniform(VerticalAnchor minInclusive, VerticalAnchor maxInclusive) {
        return HeightRangePlacement.of(UniformHeight.of(minInclusive, maxInclusive));
    }

    public static HeightRangePlacement triangle(VerticalAnchor minInclusive, VerticalAnchor maxInclusive) {
        return HeightRangePlacement.of(TrapezoidHeight.of(minInclusive, maxInclusive));
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos origin) {
        return Stream.of(origin.atY(this.height.sample(random, context)));
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.HEIGHT_RANGE;
    }
}

