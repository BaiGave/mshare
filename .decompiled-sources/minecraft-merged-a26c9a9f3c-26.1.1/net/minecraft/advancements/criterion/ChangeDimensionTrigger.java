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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class ChangeDimensionTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ResourceKey<Level> from, ResourceKey<Level> to) {
        this.trigger(player, t -> t.matches(from, to));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ResourceKey<Level>> from, Optional<ResourceKey<Level>> to) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("from").forGetter(TriggerInstance::from), ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("to").forGetter(TriggerInstance::to)).apply((Applicative<TriggerInstance, ?>)i, TriggerInstance::new));

        public static Criterion<TriggerInstance> changedDimension() {
            return CriteriaTriggers.CHANGED_DIMENSION.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> changedDimension(ResourceKey<Level> from, ResourceKey<Level> to) {
            return CriteriaTriggers.CHANGED_DIMENSION.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(from), Optional.of(to)));
        }

        public static Criterion<TriggerInstance> changedDimensionTo(ResourceKey<Level> to) {
            return CriteriaTriggers.CHANGED_DIMENSION.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(to)));
        }

        public static Criterion<TriggerInstance> changedDimensionFrom(ResourceKey<Level> from) {
            return CriteriaTriggers.CHANGED_DIMENSION.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(from), Optional.empty()));
        }

        public boolean matches(ResourceKey<Level> from, ResourceKey<Level> to) {
            if (this.from.isPresent() && this.from.get() != from) {
                return false;
            }
            return !this.to.isPresent() || this.to.get() == to;
        }
    }
}

