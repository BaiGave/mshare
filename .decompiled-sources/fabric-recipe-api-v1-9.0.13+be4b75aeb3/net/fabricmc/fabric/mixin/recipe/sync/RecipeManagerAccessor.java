/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.recipe.sync;

import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={RecipeManager.class})
public interface RecipeManagerAccessor {
    @Accessor
    public RecipeMap getRecipes();
}

