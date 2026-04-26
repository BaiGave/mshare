/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;

public class HasSturdyFacePredicate
implements BlockPredicate {
    private final Vec3i offset;
    private final Direction direction;
    public static final MapCodec<HasSturdyFacePredicate> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Vec3i.offsetCodec(16).optionalFieldOf("offset", Vec3i.ZERO).forGetter(c -> c.offset), ((MapCodec)Direction.CODEC.fieldOf("direction")).forGetter(c -> c.direction)).apply((Applicative<HasSturdyFacePredicate, ?>)i, HasSturdyFacePredicate::new));

    public HasSturdyFacePredicate(Vec3i offset, Direction direction) {
        this.offset = offset;
        this.direction = direction;
    }

    @Override
    public boolean test(WorldGenLevel level, BlockPos origin) {
        BlockPos testPosition = origin.offset(this.offset);
        return level.getBlockState(testPosition).isFaceSturdy(level, testPosition, this.direction);
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.HAS_STURDY_FACE;
    }
}

