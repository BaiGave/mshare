/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.networking.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;

public final class ClientConfigurationConnectionEvents {
    public static final Event<Init> INIT = EventFactory.createArrayBacked(Init.class, callbacks -> (listener, client) -> {
        for (Init callback : callbacks) {
            callback.onConfigurationInit(listener, client);
        }
    });
    public static final Event<Start> START = EventFactory.createArrayBacked(Start.class, callbacks -> (listener, client) -> {
        for (Start callback : callbacks) {
            callback.onConfigurationStart(listener, client);
        }
    });
    public static final Event<Complete> COMPLETE = EventFactory.createArrayBacked(Complete.class, callbacks -> (listener, client) -> {
        for (Complete callback : callbacks) {
            callback.onConfigurationComplete(listener, client);
        }
    });
    public static final Event<Disconnect> DISCONNECT = EventFactory.createArrayBacked(Disconnect.class, callbacks -> (listener, client) -> {
        for (Disconnect callback : callbacks) {
            callback.onConfigurationDisconnect(listener, client);
        }
    });
    @Deprecated
    public static final Event<Ready> READY = EventFactory.createArrayBacked(Ready.class, callbacks -> (listener, client) -> {
        for (Ready callback : callbacks) {
            callback.onConfigurationReady(listener, client);
        }
    });

    private ClientConfigurationConnectionEvents() {
    }

    @Deprecated
    @FunctionalInterface
    public static interface Ready {
        public void onConfigurationReady(ClientConfigurationPacketListenerImpl var1, Minecraft var2);
    }

    @FunctionalInterface
    public static interface Disconnect {
        public void onConfigurationDisconnect(ClientConfigurationPacketListenerImpl var1, Minecraft var2);
    }

    @FunctionalInterface
    public static interface Complete {
        public void onConfigurationComplete(ClientConfigurationPacketListenerImpl var1, Minecraft var2);
    }

    @FunctionalInterface
    public static interface Start {
        public void onConfigurationStart(ClientConfigurationPacketListenerImpl var1, Minecraft var2);
    }

    @FunctionalInterface
    public static interface Init {
        public void onConfigurationInit(ClientConfigurationPacketListenerImpl var1, Minecraft var2);
    }
}

