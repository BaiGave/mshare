/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.networking.v1;

import java.util.List;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

public final class ClientboundConfigurationChannelEvents {
    public static final Event<Register> REGISTER = EventFactory.createArrayBacked(Register.class, callbacks -> (listener, sender, server, channels) -> {
        for (Register callback : callbacks) {
            callback.onChannelRegister(listener, sender, server, channels);
        }
    });
    public static final Event<Unregister> UNREGISTER = EventFactory.createArrayBacked(Unregister.class, callbacks -> (listener, sender, server, channels) -> {
        for (Unregister callback : callbacks) {
            callback.onChannelUnregister(listener, sender, server, channels);
        }
    });

    private ClientboundConfigurationChannelEvents() {
    }

    @FunctionalInterface
    public static interface Unregister {
        public void onChannelUnregister(ServerConfigurationPacketListenerImpl var1, PacketSender var2, MinecraftServer var3, List<Identifier> var4);
    }

    @FunctionalInterface
    public static interface Register {
        public void onChannelRegister(ServerConfigurationPacketListenerImpl var1, PacketSender var2, MinecraftServer var3, List<Identifier> var4);
    }
}

