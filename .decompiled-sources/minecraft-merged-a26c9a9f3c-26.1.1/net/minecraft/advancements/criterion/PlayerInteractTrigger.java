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
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;

public class PlayerInteractTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack itemStack, Entity interactedWith) {
        LootContext context = EntityPredicate.createContext(player, interactedWith);
        this.trigger(player, t -> t.matches(itemStack, context));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item, Optional<ContextAwarePredicate> entity) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item), EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(TriggerInstance::entity)).apply((Applicative<TriggerInstance, ?>)i, TriggerInstance::new));

        public static Criterion<TriggerInstance> itemUsedOnEntity(Optional<ContextAwarePredicate> player, ItemPredicate.Builder item, Optional<ContextAwarePredicate> entity) {
            return CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.createCriterion(new TriggerInstance(player, Optional.of(item.build()), entity));
        }

        public static Criterion<TriggerInstance> equipmentSheared(Optional<ContextAwarePredicate> player, ItemPredicate.Builder item, Optional<ContextAwarePredicate> entity) {
            return CriteriaTriggers.PLAYER_SHEARED_EQUIPMENT.createCriterion(new TriggerInstance(player, Optional.of(item.build()), entity));
        }

        public static Criterion<TriggerInstance> equipmentSheared(ItemPredicate.Builder item, Optional<ContextAwarePredicate> entity) {
            return CriteriaTriggers.PLAYER_SHEARED_EQUIPMENT.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(item.build()), entity));
        }

        public static Criterion<TriggerInstance> itemUsedOnEntity(ItemPredicate.Builder item, Optional<ContextAwarePredicate> entity) {
            return TriggerInstance.itemUsedOnEntity(Optional.empty(), item, entity);
        }

        public boolean matches(ItemStack itemStack, LootContext interactedWith) {
            if (this.item.isPresent() && !this.item.get().test(itemStack)) {
                return false;
            }
            return this.entity.isEmpty() || this.entity.get().matches(interactedWith);
        }

        @Override
        public void validate(ValidationContextSource validator) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(validator);
            Validatable.validate(validator.entityContext(), "entity", this.entity);
        }
    }
}

