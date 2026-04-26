/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.item.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.fabricmc.fabric.api.item.v1.EnchantmentSource;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public final class EnchantmentEvents {
    public static final Event<AllowEnchanting> ALLOW_ENCHANTING = EventFactory.createArrayBacked(AllowEnchanting.class, callbacks -> (enchantment, target, context) -> {
        for (AllowEnchanting callback : callbacks) {
            TriState result = callback.allowEnchanting(enchantment, target, context);
            if (result == TriState.DEFAULT) continue;
            return result;
        }
        return TriState.DEFAULT;
    });
    public static final Event<Modify> MODIFY = EventFactory.createArrayBacked(Modify.class, callbacks -> (key, builder, source) -> {
        for (Modify callback : callbacks) {
            callback.modify(key, builder, source);
        }
    });

    private EnchantmentEvents() {
    }

    @FunctionalInterface
    public static interface Modify {
        public void modify(ResourceKey<Enchantment> var1, Enchantment.Builder var2, EnchantmentSource var3);
    }

    @FunctionalInterface
    public static interface AllowEnchanting {
        public TriState allowEnchanting(Holder<Enchantment> var1, ItemStack var2, EnchantingContext var3);
    }
}

