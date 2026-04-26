/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.datagen.v1.recipe;

import net.minecraft.resources.Identifier;

public interface FabricRecipeOutput {
    default public Identifier getRecipeIdentifier(Identifier recipeId) {
        return recipeId;
    }
}

