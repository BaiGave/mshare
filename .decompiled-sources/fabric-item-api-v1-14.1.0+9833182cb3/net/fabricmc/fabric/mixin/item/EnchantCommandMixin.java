/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.item;

import java.util.Collection;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.server.commands.EnchantCommand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={EnchantCommand.class})
abstract class EnchantCommandMixin {
    EnchantCommandMixin() {
    }

    @Redirect(method={"enchant"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/enchantment/Enchantment;canEnchant(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean callAllowEnchantingEvent(Enchantment instance, ItemStack stack, CommandSourceStack source, Collection<? extends Entity> targets, Holder<Enchantment> enchantment) {
        return stack.canBeEnchantedWith(enchantment, EnchantingContext.ACCEPTABLE);
    }
}

