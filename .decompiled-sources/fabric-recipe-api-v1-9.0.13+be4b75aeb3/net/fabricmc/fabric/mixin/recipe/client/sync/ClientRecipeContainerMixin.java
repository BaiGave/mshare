/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.recipe.client.sync;

import net.fabricmc.fabric.api.recipe.v1.FabricRecipeAccess;
import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;
import net.fabricmc.fabric.impl.recipe.sync.SynchronizedRecipesImpl;
import net.fabricmc.fabric.impl.recipe.sync.client.SynchronizedClientRecipesSetter;
import net.minecraft.client.multiplayer.ClientRecipeContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={ClientRecipeContainer.class})
public class ClientRecipeContainerMixin
implements FabricRecipeAccess,
SynchronizedClientRecipesSetter {
    @Unique
    private SynchronizedRecipes synchronizedClientRecipes = SynchronizedRecipesImpl.EMPTY;

    @Override
    public SynchronizedRecipes getSynchronizedRecipes() {
        return this.synchronizedClientRecipes;
    }

    @Override
    public void fabric_setSynchronizedClientRecipes(SynchronizedRecipes recipes) {
        this.synchronizedClientRecipes = recipes;
    }
}

