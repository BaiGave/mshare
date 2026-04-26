/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.recipe.v1.ingredient;

import java.util.stream.Stream;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientImpl;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.ApiStatus;

public interface CustomIngredient {
    public boolean test(ItemStack var1);

    public Stream<Holder<Item>> items();

    public boolean requiresTesting();

    public CustomIngredientSerializer<?> getSerializer();

    default public SlotDisplay display() {
        return new SlotDisplay.Composite(this.items().map(Ingredient::displayForSingleItem).toList());
    }

    @ApiStatus.NonExtendable
    default public Ingredient toVanilla() {
        return new CustomIngredientImpl(this);
    }
}

