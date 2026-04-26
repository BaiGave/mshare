/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.item;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={EnchantRandomlyFunction.class})
abstract class EnchantRandomlyFunctionMixin {
    EnchantRandomlyFunctionMixin() {
    }

    @Redirect(method={"lambda$run$1"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/enchantment/Enchantment;canEnchant(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean callAllowEnchantingEvent(Enchantment enchantment, ItemStack stack, boolean bl, ItemStack itemStack, Holder<Enchantment> holder) {
        return stack.canBeEnchantedWith(holder, EnchantingContext.ACCEPTABLE);
    }
}

