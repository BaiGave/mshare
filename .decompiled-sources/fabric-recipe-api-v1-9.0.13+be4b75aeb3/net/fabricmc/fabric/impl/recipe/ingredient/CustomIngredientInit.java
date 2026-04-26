/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.ingredient;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.AllIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.AnyIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.ComponentsIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.CustomDataIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.DifferenceIngredient;

public class CustomIngredientInit
implements ModInitializer {
    @Override
    public void onInitialize() {
        CustomIngredientSerializer.register(AllIngredient.SERIALIZER);
        CustomIngredientSerializer.register(AnyIngredient.SERIALIZER);
        CustomIngredientSerializer.register(DifferenceIngredient.SERIALIZER);
        CustomIngredientSerializer.register(ComponentsIngredient.SERIALIZER);
        CustomIngredientSerializer.register(CustomDataIngredient.SERIALIZER);
    }
}

