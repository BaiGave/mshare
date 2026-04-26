/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.world.level.storage.loot.ValidationContextSource;

public class ImpossibleTrigger
implements CriterionTrigger<TriggerInstance> {
    @Override
    public void addPlayerListener(PlayerAdvancements player, CriterionTrigger.Listener<TriggerInstance> listener) {
    }

    @Override
    public void removePlayerListener(PlayerAdvancements player, CriterionTrigger.Listener<TriggerInstance> listener) {
    }

    @Override
    public void removePlayerListeners(PlayerAdvancements player) {
    }

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public record TriggerInstance() implements CriterionTriggerInstance
    {
        public static final Codec<TriggerInstance> CODEC = MapCodec.unitCodec(new TriggerInstance());

        @Override
        public void validate(ValidationContextSource validator) {
        }
    }
}

