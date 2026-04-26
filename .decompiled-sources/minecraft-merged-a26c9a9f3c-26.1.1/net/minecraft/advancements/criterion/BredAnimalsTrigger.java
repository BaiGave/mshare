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
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
import org.jspecify.annotations.Nullable;

public class BredAnimalsTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Animal parent, Animal partner, @Nullable AgeableMob child) {
        LootContext parentContext = EntityPredicate.createContext(player, parent);
        LootContext partnerContext = EntityPredicate.createContext(player, partner);
        LootContext childContext = child != null ? EntityPredicate.createContext(player, child) : null;
        this.trigger(player, t -> t.matches(parentContext, partnerContext, childContext));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> parent, Optional<ContextAwarePredicate> partner, Optional<ContextAwarePredicate> child) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("parent").forGetter(TriggerInstance::parent), EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("partner").forGetter(TriggerInstance::partner), EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("child").forGetter(TriggerInstance::child)).apply((Applicative<TriggerInstance, ?>)i, TriggerInstance::new));

        public static Criterion<TriggerInstance> bredAnimals() {
            return CriteriaTriggers.BRED_ANIMALS.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> bredAnimals(EntityPredicate.Builder child) {
            return CriteriaTriggers.BRED_ANIMALS.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(EntityPredicate.wrap(child))));
        }

        public static Criterion<TriggerInstance> bredAnimals(Optional<EntityPredicate> parent1, Optional<EntityPredicate> parent2, Optional<EntityPredicate> child) {
            return CriteriaTriggers.BRED_ANIMALS.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap(parent1), EntityPredicate.wrap(parent2), EntityPredicate.wrap(child)));
        }

        public boolean matches(LootContext parent, LootContext partner, @Nullable LootContext child) {
            if (this.child.isPresent() && (child == null || !this.child.get().matches(child))) {
                return false;
            }
            return TriggerInstance.matches(this.parent, parent) && TriggerInstance.matches(this.partner, partner) || TriggerInstance.matches(this.parent, partner) && TriggerInstance.matches(this.partner, parent);
        }

        private static boolean matches(Optional<ContextAwarePredicate> predicate, LootContext context) {
            return predicate.isEmpty() || predicate.get().matches(context);
        }

        @Override
        public void validate(ValidationContextSource validator) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(validator);
            Validatable.validate(validator.entityContext(), "parent", this.parent);
            Validatable.validate(validator.entityContext(), "partner", this.partner);
            Validatable.validate(validator.entityContext(), "child", this.child);
        }
    }
}

