/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.item.v1;

import java.util.List;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public interface ItemTooltipCallback {
    public static final Event<ItemTooltipCallback> EVENT = EventFactory.createArrayBacked(ItemTooltipCallback.class, callbacks -> (stack, context, type, lines) -> {
        for (ItemTooltipCallback callback : callbacks) {
            callback.getTooltip(stack, context, type, lines);
        }
    });

    public void getTooltip(ItemStack var1, Item.TooltipContext var2, TooltipFlag var3, List<Component> var4);
}

