/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.advancements.criterion.EntitySubPredicates;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public record LightningBoltPredicate(MinMaxBounds.Ints blocksSetOnFire, Optional<EntityPredicate> entityStruck) implements EntitySubPredicate
{
    public static final MapCodec<LightningBoltPredicate> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(MinMaxBounds.Ints.CODEC.optionalFieldOf("blocks_set_on_fire", MinMaxBounds.Ints.ANY).forGetter(LightningBoltPredicate::blocksSetOnFire), EntityPredicate.CODEC.optionalFieldOf("entity_struck").forGetter(LightningBoltPredicate::entityStruck)).apply((Applicative<LightningBoltPredicate, ?>)i, LightningBoltPredicate::new));

    public static LightningBoltPredicate blockSetOnFire(MinMaxBounds.Ints count) {
        return new LightningBoltPredicate(count, Optional.empty());
    }

    public MapCodec<LightningBoltPredicate> codec() {
        return EntitySubPredicates.LIGHTNING;
    }

    @Override
    public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 position) {
        if (!(entity instanceof LightningBolt)) {
            return false;
        }
        LightningBolt bolt = (LightningBolt)entity;
        return this.blocksSetOnFire.matches(bolt.getBlocksSetOnFire()) && (this.entityStruck.isEmpty() || bolt.getHitEntities().anyMatch(e -> this.entityStruck.get().matches(level, position, (Entity)e)));
    }
}

