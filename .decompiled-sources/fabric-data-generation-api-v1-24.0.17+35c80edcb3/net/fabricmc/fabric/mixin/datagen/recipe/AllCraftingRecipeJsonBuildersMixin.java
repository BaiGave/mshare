/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.datagen.recipe;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.TransmuteRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={SimpleCookingRecipeBuilder.class, ShapedRecipeBuilder.class, ShapelessRecipeBuilder.class, SingleItemRecipeBuilder.class, TransmuteRecipeBuilder.class})
abstract class AllCraftingRecipeJsonBuildersMixin {
    AllCraftingRecipeJsonBuildersMixin() {
    }

    @ModifyVariable(method={"save(Lnet/minecraft/data/recipes/RecipeOutput;Lnet/minecraft/resources/ResourceKey;)V"}, at=@At(value="HEAD"), argsOnly=true, name={"id"})
    private ResourceKey<Recipe<?>> modifyRecipeKey(ResourceKey<Recipe<?>> recipeKey, RecipeOutput output) {
        return ResourceKey.create(recipeKey.registryKey(), output.getRecipeIdentifier(recipeKey.identifier()));
    }
}

