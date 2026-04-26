/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.recipe.v1;

import java.util.Collection;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.recipe.v1.FabricRecipeAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public interface FabricRecipeManager
extends FabricRecipeAccess {
    default public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getAllMatches(RecipeType<T> type, I input, Level level) {
        throw new AssertionError((Object)"Implemented in Mixin");
    }

    default public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> getAllOfType(RecipeType<T> type) {
        throw new AssertionError((Object)"Implemented in Mixin");
    }
}

