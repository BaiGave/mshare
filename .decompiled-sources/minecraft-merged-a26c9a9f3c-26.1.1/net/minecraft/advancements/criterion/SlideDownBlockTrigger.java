/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SlideDownBlockTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, BlockState state) {
        this.trigger(player, (T t) -> t.matches(state));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<Holder<Block>> block, Optional<StatePropertiesPredicate> state) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), BuiltInRegistries.BLOCK.holderByNameCodec().optionalFieldOf("block").forGetter(TriggerInstance::block), StatePropertiesPredicate.CODEC.optionalFieldOf("state").forGetter(TriggerInstance::state)).apply((Applicative<TriggerInstance, ?>)i, TriggerInstance::new)).validate(TriggerInstance::validate);

        private static DataResult<TriggerInstance> validate(TriggerInstance trigger) {
            return trigger.block.flatMap(block -> trigger.state.flatMap(state -> state.checkState(((Block)block.value()).getStateDefinition())).map(property -> DataResult.error(() -> "Block" + String.valueOf(block) + " has no property " + property))).orElseGet(() -> DataResult.success(trigger));
        }

        public static Criterion<TriggerInstance> slidesDownBlock(Block block) {
            return CriteriaTriggers.HONEY_BLOCK_SLIDE.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(block.builtInRegistryHolder()), Optional.empty()));
        }

        public boolean matches(BlockState state) {
            if (this.block.isPresent() && !state.is(this.block.get())) {
                return false;
            }
            return !this.state.isPresent() || this.state.get().matches(state);
        }
    }
}

