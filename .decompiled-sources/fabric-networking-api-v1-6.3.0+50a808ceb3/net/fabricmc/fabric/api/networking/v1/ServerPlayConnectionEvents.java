/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.networking.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public final class ServerPlayConnectionEvents {
    public static final Event<Init> INIT = EventFactory.createArrayBacked(Init.class, callbacks -> (listener, server) -> {
        for (Init callback : callbacks) {
            callback.onPlayInit(listener, server);
        }
    });
    public static final Event<Join> JOIN = EventFactory.createArrayBacked(Join.class, callbacks -> (listener, sender, server) -> {
        for (Join callback : callbacks) {
            callback.onPlayReady(listener, sender, server);
        }
    });
    public static final Event<Disconnect> DISCONNECT = EventFactory.createArrayBacked(Disconnect.class, callbacks -> (listener, server) -> {
        for (Disconnect callback : callbacks) {
            callback.onPlayDisconnect(listener, server);
        }
    });

    private ServerPlayConnectionEvents() {
    }

    @FunctionalInterface
    public static interface Disconnect {
        public void onPlayDisconnect(ServerGamePacketListenerImpl var1, MinecraftServer var2);
    }

    @FunctionalInterface
    public static interface Join {
        public void onPlayReady(ServerGamePacketListenerImpl var1, PacketSender var2, MinecraftServer var3);
    }

    @FunctionalInterface
    public static interface Init {
        public void onPlayInit(ServerGamePacketListenerImpl var1, MinecraftServer var2);
    }
}

