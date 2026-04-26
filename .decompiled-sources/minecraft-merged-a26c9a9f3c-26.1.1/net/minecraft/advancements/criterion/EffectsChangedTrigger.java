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
import net.minecraft.advancements.criterion.MobEffectsPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
import org.jspecify.annotations.Nullable;

public class EffectsChangedTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, @Nullable Entity source) {
        LootContext wrappedSource = source != null ? EntityPredicate.createContext(player, source) : null;
        this.trigger(player, (T t) -> t.matches(player, wrappedSource));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<MobEffectsPredicate> effects, Optional<ContextAwarePredicate> source) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), MobEffectsPredicate.CODEC.optionalFieldOf("effects").forGetter(TriggerInstance::effects), EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("source").forGetter(TriggerInstance::source)).apply((Applicative<TriggerInstance, ?>)i, TriggerInstance::new));

        public static Criterion<TriggerInstance> hasEffects(MobEffectsPredicate.Builder effects) {
            return CriteriaTriggers.EFFECTS_CHANGED.createCriterion(new TriggerInstance(Optional.empty(), effects.build(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> gotEffectsFrom(EntityPredicate.Builder source) {
            return CriteriaTriggers.EFFECTS_CHANGED.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(EntityPredicate.wrap(source.build()))));
        }

        public boolean matches(ServerPlayer player, @Nullable LootContext source) {
            if (this.effects.isPresent() && !this.effects.get().matches(player)) {
                return false;
            }
            return !this.source.isPresent() || source != null && this.source.get().matches(source);
        }

        @Override
        public void validate(ValidationContextSource validator) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(validator);
            Validatable.validate(validator.entityContext(), "source", this.source);
        }
    }
}

