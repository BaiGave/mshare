/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.recipe.v1.ingredient;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import org.jspecify.annotations.Nullable;

public interface FabricIngredient {
    default public @Nullable CustomIngredient getCustomIngredient() {
        return null;
    }

    default public boolean requiresTesting() {
        return this.getCustomIngredient() != null && this.getCustomIngredient().requiresTesting();
    }
}

