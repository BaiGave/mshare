/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.datagen.recipe;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={SmithingTransformRecipeBuilder.class})
abstract class SmithingTransformRecipeBuilderMixin {
    SmithingTransformRecipeBuilderMixin() {
    }

    @ModifyVariable(method={"save(Lnet/minecraft/data/recipes/RecipeOutput;Lnet/minecraft/resources/ResourceKey;)V"}, at=@At(value="HEAD"), argsOnly=true, name={"id"})
    private ResourceKey<Recipe<?>> modifyRecipeKey(ResourceKey<Recipe<?>> recipeKey, RecipeOutput output) {
        return ResourceKey.create(recipeKey.registryKey(), output.getRecipeIdentifier(recipeKey.identifier()));
    }
}

