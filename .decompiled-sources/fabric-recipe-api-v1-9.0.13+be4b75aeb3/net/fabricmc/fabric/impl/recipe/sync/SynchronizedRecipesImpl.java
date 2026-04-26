/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.sync;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public record SynchronizedRecipesImpl(RecipeMap preparedRecipes) implements SynchronizedRecipes
{
    public static final SynchronizedRecipesImpl EMPTY = new SynchronizedRecipesImpl(RecipeMap.EMPTY);

    public static SynchronizedRecipesImpl of(Iterable<RecipeHolder<?>> recipes) {
        return new SynchronizedRecipesImpl(RecipeMap.create(recipes));
    }

    @Override
    public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getAllMatches(RecipeType<T> type, I input, Level level) {
        return this.preparedRecipes.getRecipesFor(type, input, level);
    }

    @Override
    public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> getAllOfType(RecipeType<T> type) {
        return this.preparedRecipes.byType(type);
    }

    @Override
    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getFirstMatch(RecipeType<T> type, I input, Level level) {
        return this.preparedRecipes.getRecipesFor(type, input, level).findFirst();
    }

    @Override
    public @Nullable RecipeHolder<?> get(ResourceKey<Recipe<?>> key) {
        return this.preparedRecipes.byKey(key);
    }

    @Override
    public Collection<RecipeHolder<?>> recipes() {
        return this.preparedRecipes.values();
    }
}

