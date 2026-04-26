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
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
import net.minecraft.world.phys.Vec3;

public class TargetBlockTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Entity projectile, Vec3 hitPosition, int signalStrength) {
        LootContext projectileContext = EntityPredicate.createContext(player, projectile);
        this.trigger(player, t -> t.matches(projectileContext, hitPosition, signalStrength));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, MinMaxBounds.Ints signalStrength, Optional<ContextAwarePredicate> projectile) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), MinMaxBounds.Ints.CODEC.optionalFieldOf("signal_strength", MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::signalStrength), EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("projectile").forGetter(TriggerInstance::projectile)).apply((Applicative<TriggerInstance, ?>)i, TriggerInstance::new));

        public static Criterion<TriggerInstance> targetHit(MinMaxBounds.Ints redstoneSignalStrength, Optional<ContextAwarePredicate> projectile) {
            return CriteriaTriggers.TARGET_BLOCK_HIT.createCriterion(new TriggerInstance(Optional.empty(), redstoneSignalStrength, projectile));
        }

        public boolean matches(LootContext projectile, Vec3 hitPosition, int signalStrength) {
            if (!this.signalStrength.matches(signalStrength)) {
                return false;
            }
            return !this.projectile.isPresent() || this.projectile.get().matches(projectile);
        }

        @Override
        public void validate(ValidationContextSource validator) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(validator);
            Validatable.validate(validator.entityContext(), "projectile", this.projectile);
        }
    }
}

