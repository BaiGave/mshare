/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jspecify.annotations.Nullable;

public interface ClientTooltipComponentCallback {
    public static final Event<ClientTooltipComponentCallback> EVENT = EventFactory.createArrayBacked(ClientTooltipComponentCallback.class, listeners -> data -> {
        for (ClientTooltipComponentCallback listener : listeners) {
            ClientTooltipComponent component = listener.getClientComponent(data);
            if (component == null) continue;
            return component;
        }
        return null;
    });

    public @Nullable ClientTooltipComponent getClientComponent(TooltipComponent var1);
}

