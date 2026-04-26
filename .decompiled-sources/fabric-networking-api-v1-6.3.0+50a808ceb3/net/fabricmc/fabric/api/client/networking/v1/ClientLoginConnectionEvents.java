/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.networking.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;

public final class ClientLoginConnectionEvents {
    public static final Event<Init> INIT = EventFactory.createArrayBacked(Init.class, callbacks -> (listener, client) -> {
        for (Init callback : callbacks) {
            callback.onLoginStart(listener, client);
        }
    });
    public static final Event<QueryStart> QUERY_START = EventFactory.createArrayBacked(QueryStart.class, callbacks -> (listener, client) -> {
        for (QueryStart callback : callbacks) {
            callback.onLoginQueryStart(listener, client);
        }
    });
    public static final Event<Disconnect> DISCONNECT = EventFactory.createArrayBacked(Disconnect.class, callbacks -> (listener, client) -> {
        for (Disconnect callback : callbacks) {
            callback.onLoginDisconnect(listener, client);
        }
    });

    private ClientLoginConnectionEvents() {
    }

    @FunctionalInterface
    public static interface Disconnect {
        public void onLoginDisconnect(ClientHandshakePacketListenerImpl var1, Minecraft var2);
    }

    @FunctionalInterface
    public static interface QueryStart {
        public void onLoginQueryStart(ClientHandshakePacketListenerImpl var1, Minecraft var2);
    }

    @FunctionalInterface
    public static interface Init {
        public void onLoginStart(ClientHandshakePacketListenerImpl var1, Minecraft var2);
    }
}

