/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.networking.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.LoginPacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

public final class ServerLoginConnectionEvents {
    public static final Event<Init> INIT = EventFactory.createArrayBacked(Init.class, callbacks -> (listener, server) -> {
        for (Init callback : callbacks) {
            callback.onLoginInit(listener, server);
        }
    });
    public static final Event<QueryStart> QUERY_START = EventFactory.createArrayBacked(QueryStart.class, callbacks -> (listener, server, sender, synchronizer) -> {
        for (QueryStart callback : callbacks) {
            callback.onLoginStart(listener, server, sender, synchronizer);
        }
    });
    public static final Event<Disconnect> DISCONNECT = EventFactory.createArrayBacked(Disconnect.class, callbacks -> (listener, server) -> {
        for (Disconnect callback : callbacks) {
            callback.onLoginDisconnect(listener, server);
        }
    });

    private ServerLoginConnectionEvents() {
    }

    @FunctionalInterface
    public static interface Disconnect {
        public void onLoginDisconnect(ServerLoginPacketListenerImpl var1, MinecraftServer var2);
    }

    @FunctionalInterface
    public static interface QueryStart {
        public void onLoginStart(ServerLoginPacketListenerImpl var1, MinecraftServer var2, LoginPacketSender var3, ServerLoginNetworking.LoginSynchronizer var4);
    }

    @FunctionalInterface
    public static interface Init {
        public void onLoginInit(ServerLoginPacketListenerImpl var1, MinecraftServer var2);
    }
}

