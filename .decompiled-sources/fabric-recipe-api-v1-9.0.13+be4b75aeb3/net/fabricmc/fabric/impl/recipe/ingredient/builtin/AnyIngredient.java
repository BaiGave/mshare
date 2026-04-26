/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.ingredient.builtin;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.CombinedIngredient;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class AnyIngredient
extends CombinedIngredient {
    private static final MapCodec<AnyIngredient> CODEC = ((MapCodec)Ingredient.CODEC.listOf().fieldOf("ingredients")).xmap(AnyIngredient::new, CombinedIngredient::getIngredients);
    public static final CustomIngredientSerializer<AnyIngredient> SERIALIZER = new CombinedIngredient.Serializer<AnyIngredient>(Identifier.fromNamespaceAndPath("fabric", "any"), AnyIngredient::new, CODEC);

    public AnyIngredient(List<Ingredient> ingredients) {
        super(ingredients);
    }

    @Override
    public boolean test(ItemStack stack) {
        for (Ingredient ingredient : this.ingredients) {
            if (!ingredient.test(stack)) continue;
            return true;
        }
        return false;
    }

    @Override
    public Stream<Holder<Item>> items() {
        return this.ingredients.stream().flatMap(Ingredient::items);
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}

