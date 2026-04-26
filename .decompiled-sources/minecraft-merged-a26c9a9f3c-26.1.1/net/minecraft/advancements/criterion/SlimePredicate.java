/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.advancements.criterion.EntitySubPredicates;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public record SlimePredicate(MinMaxBounds.Ints size) implements EntitySubPredicate
{
    public static final MapCodec<SlimePredicate> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(MinMaxBounds.Ints.CODEC.optionalFieldOf("size", MinMaxBounds.Ints.ANY).forGetter(SlimePredicate::size)).apply((Applicative<SlimePredicate, ?>)i, SlimePredicate::new));

    public static SlimePredicate sized(MinMaxBounds.Ints size) {
        return new SlimePredicate(size);
    }

    @Override
    public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 position) {
        if (entity instanceof Slime) {
            Slime slime = (Slime)entity;
            return this.size.matches(slime.getSize());
        }
        return false;
    }

    public MapCodec<SlimePredicate> codec() {
        return EntitySubPredicates.SLIME;
    }
}

