/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;

class NotPredicate
implements BlockPredicate {
    public static final MapCodec<NotPredicate> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)BlockPredicate.CODEC.fieldOf("predicate")).forGetter(p -> p.predicate)).apply((Applicative<NotPredicate, ?>)i, NotPredicate::new));
    private final BlockPredicate predicate;

    public NotPredicate(BlockPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(WorldGenLevel level, BlockPos origin) {
        return !this.predicate.test(level, origin);
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.NOT;
    }
}

