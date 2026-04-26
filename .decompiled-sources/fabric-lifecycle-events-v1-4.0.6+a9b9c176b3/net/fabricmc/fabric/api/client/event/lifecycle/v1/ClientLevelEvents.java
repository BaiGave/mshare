/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.event.lifecycle.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public final class ClientLevelEvents {
    public static final Event<AfterClientLevelChange> AFTER_CLIENT_LEVEL_CHANGE = EventFactory.createArrayBacked(AfterClientLevelChange.class, callbacks -> (client, level) -> {
        for (AfterClientLevelChange callback : callbacks) {
            callback.afterLevelChange(client, level);
        }
    });

    private ClientLevelEvents() {
    }

    @FunctionalInterface
    public static interface AfterClientLevelChange {
        public void afterLevelChange(Minecraft var1, ClientLevel var2);
    }
}

