/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;

class TrueBlockPredicate
implements BlockPredicate {
    public static final TrueBlockPredicate INSTANCE = new TrueBlockPredicate();
    public static final MapCodec<TrueBlockPredicate> CODEC = MapCodec.unit(() -> INSTANCE);

    private TrueBlockPredicate() {
    }

    @Override
    public boolean test(WorldGenLevel level, BlockPos origin) {
        return true;
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.TRUE;
    }
}

