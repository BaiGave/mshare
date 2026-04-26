/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.networking.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public final class ClientPlayConnectionEvents {
    public static final Event<Init> INIT = EventFactory.createArrayBacked(Init.class, callbacks -> (listener, client) -> {
        for (Init callback : callbacks) {
            callback.onPlayInit(listener, client);
        }
    });
    public static final Event<Join> JOIN = EventFactory.createArrayBacked(Join.class, callbacks -> (listener, sender, client) -> {
        for (Join callback : callbacks) {
            callback.onPlayReady(listener, sender, client);
        }
    });
    public static final Event<Disconnect> DISCONNECT = EventFactory.createArrayBacked(Disconnect.class, callbacks -> (listener, client) -> {
        for (Disconnect callback : callbacks) {
            callback.onPlayDisconnect(listener, client);
        }
    });

    private ClientPlayConnectionEvents() {
    }

    @FunctionalInterface
    public static interface Disconnect {
        public void onPlayDisconnect(ClientPacketListener var1, Minecraft var2);
    }

    @FunctionalInterface
    public static interface Join {
        public void onPlayReady(ClientPacketListener var1, PacketSender var2, Minecraft var3);
    }

    @FunctionalInterface
    public static interface Init {
        public void onPlayInit(ClientPacketListener var1, Minecraft var2);
    }
}

