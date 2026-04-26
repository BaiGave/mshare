/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.recipe.v1.ingredient;

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.AllIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.AnyIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.ComponentsIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.CustomDataIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.DifferenceIngredient;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public final class DefaultCustomIngredients {
    public static Ingredient all(Ingredient ... ingredients) {
        for (Ingredient ing : ingredients) {
            Objects.requireNonNull(ing, "Ingredient cannot be null");
        }
        return new AllIngredient(List.of(ingredients)).toVanilla();
    }

    public static Ingredient any(Ingredient ... ingredients) {
        for (Ingredient ing : ingredients) {
            Objects.requireNonNull(ing, "Ingredient cannot be null");
        }
        return new AnyIngredient(List.of(ingredients)).toVanilla();
    }

    public static Ingredient difference(Ingredient base, Ingredient subtracted) {
        Objects.requireNonNull(base, "Base ingredient cannot be null");
        Objects.requireNonNull(subtracted, "Subtracted ingredient cannot be null");
        return new DifferenceIngredient(base, subtracted).toVanilla();
    }

    public static Ingredient components(Ingredient base, DataComponentPatch components) {
        Objects.requireNonNull(base, "Base ingredient cannot be null");
        Objects.requireNonNull(components, "Component patch cannot be null");
        return new ComponentsIngredient(base, components).toVanilla();
    }

    public static Ingredient components(Ingredient base, UnaryOperator<DataComponentPatch.Builder> operator) {
        return DefaultCustomIngredients.components(base, ((DataComponentPatch.Builder)operator.apply(DataComponentPatch.builder())).build());
    }

    public static Ingredient components(ItemStack stack) {
        Objects.requireNonNull(stack, "Stack cannot be null");
        return DefaultCustomIngredients.components(Ingredient.of(new ItemLike[]{stack.getItem()}), stack.getComponentsPatch());
    }

    public static Ingredient customData(Ingredient base, CompoundTag nbt) {
        return new CustomDataIngredient(base, nbt).toVanilla();
    }

    private DefaultCustomIngredients() {
    }
}

