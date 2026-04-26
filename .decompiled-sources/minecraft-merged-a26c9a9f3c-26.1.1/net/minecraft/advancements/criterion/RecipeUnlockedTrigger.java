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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class RecipeUnlockedTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    @Override
    public void trigger(ServerPlayer player, RecipeHolder<?> recipe) {
        ((SimpleCriterionTrigger)this).trigger(player, (T t) -> t.matches(recipe));
    }

    public static Criterion<TriggerInstance> unlocked(ResourceKey<Recipe<?>> recipe) {
        return CriteriaTriggers.RECIPE_UNLOCKED.createCriterion(new TriggerInstance(Optional.empty(), recipe));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceKey<Recipe<?>> recipe) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), ((MapCodec)Recipe.KEY_CODEC.fieldOf("recipe")).forGetter(TriggerInstance::recipe)).apply((Applicative<TriggerInstance, ?>)i, TriggerInstance::new));

        public boolean matches(RecipeHolder<?> recipe) {
            return this.recipe == recipe.id();
        }
    }
}

