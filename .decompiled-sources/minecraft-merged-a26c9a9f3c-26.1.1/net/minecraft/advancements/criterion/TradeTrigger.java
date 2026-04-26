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
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;

public class TradeTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, AbstractVillager villager, ItemStack itemStack) {
        LootContext villagerContext = EntityPredicate.createContext(player, villager);
        this.trigger(player, t -> t.matches(villagerContext, itemStack));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> villager, Optional<ItemPredicate> item) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("villager").forGetter(TriggerInstance::villager), ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item)).apply((Applicative<TriggerInstance, ?>)i, TriggerInstance::new));

        public static Criterion<TriggerInstance> tradedWithVillager() {
            return CriteriaTriggers.TRADE.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> tradedWithVillager(EntityPredicate.Builder player) {
            return CriteriaTriggers.TRADE.createCriterion(new TriggerInstance(Optional.of(EntityPredicate.wrap(player)), Optional.empty(), Optional.empty()));
        }

        public boolean matches(LootContext villager, ItemStack itemStack) {
            if (this.villager.isPresent() && !this.villager.get().matches(villager)) {
                return false;
            }
            return !this.item.isPresent() || this.item.get().test(itemStack);
        }

        @Override
        public void validate(ValidationContextSource validator) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(validator);
            Validatable.validate(validator.entityContext(), "villager", this.villager);
        }
    }
}

