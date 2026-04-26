/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootTable;

public class LootTableTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    @Override
    public void trigger(ServerPlayer player, ResourceKey<LootTable> lootTable) {
        this.trigger(player, (T t) -> t.matches(lootTable));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceKey<LootTable> lootTable) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), ((MapCodec)LootTable.KEY_CODEC.fieldOf("loot_table")).forGetter(TriggerInstance::lootTable)).apply((Applicative<TriggerInstance, ?>)i, TriggerInstance::new));

        public static Criterion<TriggerInstance> lootTableUsed(ResourceKey<LootTable> lootTable) {
            return CriteriaTriggers.GENERATE_LOOT.createCriterion(new TriggerInstance(Optional.empty(), lootTable));
        }

        public boolean matches(ResourceKey<LootTable> lootTable) {
            return this.lootTable == lootTable;
        }
    }
}

