/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.recipe.v1.sync;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
public interface SynchronizedRecipes {
    public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getAllMatches(RecipeType<T> var1, I var2, Level var3);

    public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> getAllOfType(RecipeType<T> var1);

    default public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getFirstMatch(RecipeType<T> type, I input, Level level, @Nullable ResourceKey<Recipe<?>> recipe) {
        RecipeHolder<T> recipeHolder = recipe != null ? this.get(type, recipe) : null;
        return this.getFirstMatch(type, input, level, recipeHolder);
    }

    default public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getFirstMatch(RecipeType<T> type, I input, Level level, @Nullable RecipeHolder<T> recipe) {
        return recipe != null && recipe.value().matches(input, level) ? Optional.of(recipe) : this.getFirstMatch(type, input, level);
    }

    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getFirstMatch(RecipeType<T> var1, I var2, Level var3);

    public @Nullable RecipeHolder<?> get(ResourceKey<Recipe<?>> var1);

    default public <T extends Recipe<?>> @Nullable RecipeHolder<T> get(RecipeType<T> type, ResourceKey<Recipe<?>> key) {
        RecipeHolder<?> recipeHolder = this.get(key);
        return recipeHolder != null && recipeHolder.value().getType().equals(type) ? recipeHolder : null;
    }

    public Collection<RecipeHolder<?>> recipes();
}

