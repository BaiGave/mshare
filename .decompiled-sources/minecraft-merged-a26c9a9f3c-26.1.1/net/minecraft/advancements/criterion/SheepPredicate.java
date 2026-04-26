/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.advancements.criterion.EntitySubPredicates;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public record SheepPredicate(Optional<Boolean> sheared) implements EntitySubPredicate
{
    public static final MapCodec<SheepPredicate> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Codec.BOOL.optionalFieldOf("sheared").forGetter(SheepPredicate::sheared)).apply((Applicative<SheepPredicate, ?>)i, SheepPredicate::new));

    public MapCodec<SheepPredicate> codec() {
        return EntitySubPredicates.SHEEP;
    }

    @Override
    public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 position) {
        if (entity instanceof Sheep) {
            Sheep sheep = (Sheep)entity;
            return !this.sheared.isPresent() || sheep.isSheared() == this.sheared.get().booleanValue();
        }
        return false;
    }

    public static SheepPredicate hasWool() {
        return new SheepPredicate(Optional.of(false));
    }
}

