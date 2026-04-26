/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.datagen.recipe;

import net.fabricmc.fabric.api.datagen.v1.recipe.FabricRecipeOutput;
import net.minecraft.data.recipes.RecipeOutput;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={RecipeOutput.class})
public interface RecipeOutputMixin
extends FabricRecipeOutput {
}

