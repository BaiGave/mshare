/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={CraftingRecipe.class})
public interface CraftingRecipeMixin {
    @WrapOperation(method={"defaultCraftingReminder"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;")})
    private static Item captureStack(ItemStack stack, Operation<Item> operation, @Share(value="stack") LocalRef<ItemStack> stackRef) {
        stackRef.set(stack);
        return operation.call(stack);
    }

    @Redirect(method={"defaultCraftingReminder"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/Item;getCraftingRemainder()Lnet/minecraft/world/item/ItemStackTemplate;"))
    private static ItemStackTemplate getStackRemainder(Item item, @Share(value="stack") LocalRef<ItemStack> stackRef) {
        return stackRef.get().getCraftingRemainder();
    }
}

