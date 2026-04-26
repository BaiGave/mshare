/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.DistancePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class DistanceTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Vec3 startPosition) {
        Vec3 playerPosition = player.position();
        this.trigger(player, (T t) -> t.matches(player.level(), startPosition, playerPosition));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<LocationPredicate> startPosition, Optional<DistancePredicate> distance) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), LocationPredicate.CODEC.optionalFieldOf("start_position").forGetter(TriggerInstance::startPosition), DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(TriggerInstance::distance)).apply((Applicative<TriggerInstance, ?>)i, TriggerInstance::new));

        public static Criterion<TriggerInstance> fallFromHeight(EntityPredicate.Builder player, DistancePredicate distance, LocationPredicate.Builder startPosition) {
            return CriteriaTriggers.FALL_FROM_HEIGHT.createCriterion(new TriggerInstance(Optional.of(EntityPredicate.wrap(player)), Optional.of(startPosition.build()), Optional.of(distance)));
        }

        public static Criterion<TriggerInstance> rideEntityInLava(EntityPredicate.Builder player, DistancePredicate distance) {
            return CriteriaTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER.createCriterion(new TriggerInstance(Optional.of(EntityPredicate.wrap(player)), Optional.empty(), Optional.of(distance)));
        }

        public static Criterion<TriggerInstance> travelledThroughNether(DistancePredicate distance) {
            return CriteriaTriggers.NETHER_TRAVEL.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(distance)));
        }

        public boolean matches(ServerLevel level, Vec3 enteredPosition, Vec3 playerPosition) {
            if (this.startPosition.isPresent() && !this.startPosition.get().matches(level, enteredPosition.x, enteredPosition.y, enteredPosition.z)) {
                return false;
            }
            return !this.distance.isPresent() || this.distance.get().matches(enteredPosition.x, enteredPosition.y, enteredPosition.z, playerPosition.x, playerPosition.y, playerPosition.z);
        }
    }
}

