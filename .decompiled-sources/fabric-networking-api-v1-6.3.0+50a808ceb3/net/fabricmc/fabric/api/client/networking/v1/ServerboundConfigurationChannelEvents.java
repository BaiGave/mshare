/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.networking.v1;

import java.util.List;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.resources.Identifier;

public final class ServerboundConfigurationChannelEvents {
    public static final Event<Register> REGISTER = EventFactory.createArrayBacked(Register.class, callbacks -> (listener, sender, client, channels) -> {
        for (Register callback : callbacks) {
            callback.onChannelRegister(listener, sender, client, channels);
        }
    });
    public static final Event<Unregister> UNREGISTER = EventFactory.createArrayBacked(Unregister.class, callbacks -> (listener, sender, client, channels) -> {
        for (Unregister callback : callbacks) {
            callback.onChannelUnregister(listener, sender, client, channels);
        }
    });

    private ServerboundConfigurationChannelEvents() {
    }

    @FunctionalInterface
    public static interface Unregister {
        public void onChannelUnregister(ClientConfigurationPacketListenerImpl var1, PacketSender var2, Minecraft var3, List<Identifier> var4);
    }

    @FunctionalInterface
    public static interface Register {
        public void onChannelRegister(ClientConfigurationPacketListenerImpl var1, PacketSender var2, Minecraft var3, List<Identifier> var4);
    }
}

