/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.item;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={EnchantmentHelper.class})
abstract class EnchantmentHelperMixin {
    EnchantmentHelperMixin() {
    }

    @Redirect(method={"lambda$getAvailableEnchantmentResults$0"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/enchantment/Enchantment;isPrimaryItem(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean useCustomEnchantingChecks(Enchantment instance, ItemStack stack, ItemStack itemStack, boolean bl, Holder<Enchantment> holder) {
        return stack.canBeEnchantedWith(holder, EnchantingContext.PRIMARY);
    }
}

