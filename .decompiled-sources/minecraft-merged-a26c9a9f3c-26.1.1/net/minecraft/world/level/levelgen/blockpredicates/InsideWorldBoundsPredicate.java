/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;

public class InsideWorldBoundsPredicate
implements BlockPredicate {
    public static final MapCodec<InsideWorldBoundsPredicate> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Vec3i.offsetCodec(16).optionalFieldOf("offset", BlockPos.ZERO).forGetter(c -> c.offset)).apply((Applicative<InsideWorldBoundsPredicate, ?>)i, InsideWorldBoundsPredicate::new));
    private final Vec3i offset;

    public InsideWorldBoundsPredicate(Vec3i offset) {
        this.offset = offset;
    }

    @Override
    public boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
        return worldGenLevel.isInsideBuildHeight(blockPos.offset(this.offset));
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.INSIDE_WORLD_BOUNDS;
    }
}

