/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.blockpredicates.CombiningPredicate;

class AllOfPredicate
extends CombiningPredicate {
    public static final MapCodec<AllOfPredicate> CODEC = AllOfPredicate.codec(AllOfPredicate::new);

    public AllOfPredicate(List<BlockPredicate> predicates) {
        super(predicates);
    }

    @Override
    public boolean test(WorldGenLevel level, BlockPos origin) {
        for (BlockPredicate predicate : this.predicates) {
            if (predicate.test(level, origin)) continue;
            return false;
        }
        return true;
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.ALL_OF;
    }
}

