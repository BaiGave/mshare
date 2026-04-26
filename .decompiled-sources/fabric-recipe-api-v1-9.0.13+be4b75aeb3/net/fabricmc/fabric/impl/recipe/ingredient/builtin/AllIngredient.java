/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.ingredient.builtin;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.CombinedIngredient;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class AllIngredient
extends CombinedIngredient {
    private static final MapCodec<AllIngredient> CODEC = ((MapCodec)Ingredient.CODEC.listOf().fieldOf("ingredients")).xmap(AllIngredient::new, CombinedIngredient::getIngredients);
    public static final CustomIngredientSerializer<AllIngredient> SERIALIZER = new CombinedIngredient.Serializer<AllIngredient>(Identifier.fromNamespaceAndPath("fabric", "all"), AllIngredient::new, CODEC);

    public AllIngredient(List<Ingredient> ingredients) {
        super(ingredients);
    }

    @Override
    public boolean test(ItemStack stack) {
        for (Ingredient ingredient : this.ingredients) {
            if (ingredient.test(stack)) continue;
            return false;
        }
        return true;
    }

    @Override
    public Stream<Holder<Item>> items() {
        ArrayList<Holder<Item>> previewStacks = new ArrayList<Holder<Item>>(((Ingredient)this.ingredients.getFirst()).items().toList());
        for (int i = 1; i < this.ingredients.size(); ++i) {
            Ingredient ing = (Ingredient)this.ingredients.get(i);
            previewStacks.removeIf(entry -> !ing.test(((Item)entry.value()).getDefaultInstance()));
        }
        return previewStacks.stream();
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}

