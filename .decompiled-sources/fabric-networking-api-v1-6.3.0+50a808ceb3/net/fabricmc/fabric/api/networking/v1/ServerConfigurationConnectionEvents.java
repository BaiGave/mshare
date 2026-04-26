/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.networking.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

public final class ServerConfigurationConnectionEvents {
    public static final Event<Configure> BEFORE_CONFIGURE = EventFactory.createArrayBacked(Configure.class, callbacks -> (listener, server) -> {
        for (Configure callback : callbacks) {
            callback.onSendConfiguration(listener, server);
        }
    });
    public static final Event<Configure> CONFIGURE = EventFactory.createArrayBacked(Configure.class, callbacks -> (listener, server) -> {
        for (Configure callback : callbacks) {
            callback.onSendConfiguration(listener, server);
        }
    });
    public static final Event<Disconnect> DISCONNECT = EventFactory.createArrayBacked(Disconnect.class, callbacks -> (handler, server) -> {
        for (Disconnect callback : callbacks) {
            callback.onConfigureDisconnect(handler, server);
        }
    });

    private ServerConfigurationConnectionEvents() {
    }

    @FunctionalInterface
    public static interface Disconnect {
        public void onConfigureDisconnect(ServerConfigurationPacketListenerImpl var1, MinecraftServer var2);
    }

    @FunctionalInterface
    public static interface Configure {
        public void onSendConfiguration(ServerConfigurationPacketListenerImpl var1, MinecraftServer var2);
    }
}

