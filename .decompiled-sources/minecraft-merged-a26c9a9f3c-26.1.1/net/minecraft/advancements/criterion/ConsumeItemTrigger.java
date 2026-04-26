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
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ConsumeItemTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack itemStack) {
        this.trigger(player, (T t) -> t.matches(itemStack));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item)).apply((Applicative<TriggerInstance, ?>)i, TriggerInstance::new));

        public static Criterion<TriggerInstance> usedItem() {
            return CriteriaTriggers.CONSUME_ITEM.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> usedItem(HolderGetter<Item> items, ItemLike item) {
            return TriggerInstance.usedItem(ItemPredicate.Builder.item().of(items, item.asItem()));
        }

        public static Criterion<TriggerInstance> usedItem(ItemPredicate.Builder predicate) {
            return CriteriaTriggers.CONSUME_ITEM.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(predicate.build())));
        }

        public boolean matches(ItemStack itemStack) {
            return this.item.isEmpty() || this.item.get().test(itemStack);
        }
    }
}

