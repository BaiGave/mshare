/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ExtractItemDecorationsCallback {
    public static final Event<ExtractItemDecorationsCallback> EVENT = EventFactory.createArrayBacked(ExtractItemDecorationsCallback.class, callbacks -> (graphics, font, stack, x, y) -> {
        for (ExtractItemDecorationsCallback callback : callbacks) {
            callback.onExtractItemDecorations(graphics, font, stack, x, y);
        }
    });

    public void onExtractItemDecorations(GuiGraphicsExtractor var1, Font var2, ItemStack var3, int var4, int var5);
}

