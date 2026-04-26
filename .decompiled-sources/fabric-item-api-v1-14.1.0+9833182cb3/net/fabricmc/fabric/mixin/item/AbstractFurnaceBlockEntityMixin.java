/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.item;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={AbstractFurnaceBlockEntity.class})
public abstract class AbstractFurnaceBlockEntityMixin {
    @Inject(method={"consumeFuel"}, at={@At(value="HEAD")})
    private static void copyStack(NonNullList<ItemStack> items, ItemStack fuel, CallbackInfo ci, @Share(value="itemStack") LocalRef<ItemStack> copiedStack) {
        copiedStack.set(fuel.copy());
    }

    @Redirect(method={"consumeFuel"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/Item;getCraftingRemainder()Lnet/minecraft/world/item/ItemStackTemplate;"))
    private static ItemStackTemplate getCraftingRemainder(Item item, @Share(value="itemStack") LocalRef<ItemStack> stack) {
        return stack.get().getCraftingRemainder();
    }
}

