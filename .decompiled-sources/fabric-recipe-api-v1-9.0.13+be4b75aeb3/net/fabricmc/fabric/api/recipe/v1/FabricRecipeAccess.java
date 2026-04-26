/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.recipe.v1;

import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;
import net.fabricmc.fabric.impl.recipe.sync.SynchronizedRecipesImpl;

public interface FabricRecipeAccess {
    default public SynchronizedRecipes getSynchronizedRecipes() {
        return SynchronizedRecipesImpl.EMPTY;
    }
}

