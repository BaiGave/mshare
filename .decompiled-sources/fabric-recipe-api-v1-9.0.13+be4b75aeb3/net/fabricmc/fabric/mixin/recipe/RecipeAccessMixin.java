/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.recipe;

import net.fabricmc.fabric.api.recipe.v1.FabricRecipeAccess;
import net.minecraft.world.item.crafting.RecipeAccess;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={RecipeAccess.class})
public interface RecipeAccessMixin
extends FabricRecipeAccess {
}

