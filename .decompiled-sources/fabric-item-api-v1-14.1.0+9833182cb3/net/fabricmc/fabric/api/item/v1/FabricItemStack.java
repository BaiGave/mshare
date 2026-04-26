/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.item.v1;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jspecify.annotations.Nullable;

public interface FabricItemStack {
    default public @Nullable ItemStackTemplate getCraftingRemainder() {
        return ((ItemStack)this).getItem().getCraftingRemainder((ItemStack)this);
    }

    default public boolean canBeEnchantedWith(Holder<Enchantment> enchantment, EnchantingContext context) {
        TriState result = EnchantmentEvents.ALLOW_ENCHANTING.invoker().allowEnchanting(enchantment, (ItemStack)this, context);
        return result.orElseGet(() -> ((ItemStack)this).getItem().canBeEnchantedWith((ItemStack)this, enchantment, context));
    }

    default public String getCreatorNamespace() {
        return ((ItemStack)this).getItem().getCreatorNamespace((ItemStack)this);
    }
}

