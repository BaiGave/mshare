/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.recipe.ingredient;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.impl.recipe.ingredient.ShapelessMatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ShapelessRecipe.class})
public class ShapelessRecipeMixin {
    @Final
    @Shadow
    private List<Ingredient> ingredients;
    @Unique
    private boolean fabric_requiresTesting = false;

    @Inject(at={@At(value="RETURN")}, method={"<init>"})
    private void cacheRequiresTesting(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ItemStackTemplate result, List<Ingredient> ingredients, CallbackInfo ci) {
        for (Ingredient ingredient : ingredients) {
            if (!ingredient.requiresTesting()) continue;
            this.fabric_requiresTesting = true;
            break;
        }
    }

    @Inject(at={@At(value="HEAD")}, method={"matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z"}, cancellable=true)
    public void customIngredientMatch(CraftingInput recipeInput, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (this.fabric_requiresTesting) {
            ArrayList<ItemStack> nonEmptyStacks = new ArrayList<ItemStack>(recipeInput.ingredientCount());
            for (int i = 0; i < recipeInput.size(); ++i) {
                ItemStack stack = recipeInput.getItem(i);
                if (stack.isEmpty()) continue;
                nonEmptyStacks.add(stack);
            }
            cir.setReturnValue(ShapelessMatch.isMatch(nonEmptyStacks, this.ingredients));
        }
    }
}

