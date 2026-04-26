/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.event.lifecycle.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;

public final class ClientLifecycleEvents {
    public static final Event<ClientStarted> CLIENT_STARTED = EventFactory.createArrayBacked(ClientStarted.class, callbacks -> client -> {
        for (ClientStarted callback : callbacks) {
            callback.onClientStarted(client);
        }
    });
    public static final Event<ClientStopping> CLIENT_STOPPING = EventFactory.createArrayBacked(ClientStopping.class, callbacks -> client -> {
        for (ClientStopping callback : callbacks) {
            callback.onClientStopping(client);
        }
    });

    private ClientLifecycleEvents() {
    }

    @FunctionalInterface
    public static interface ClientStopping {
        public void onClientStopping(Minecraft var1);
    }

    @FunctionalInterface
    public static interface ClientStarted {
        public void onClientStarted(Minecraft var1);
    }
}

