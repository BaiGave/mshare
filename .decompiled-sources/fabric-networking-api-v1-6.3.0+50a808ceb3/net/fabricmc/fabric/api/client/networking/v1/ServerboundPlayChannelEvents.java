/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.networking.v1;

import java.util.List;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.Identifier;

public final class ServerboundPlayChannelEvents {
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

    private ServerboundPlayChannelEvents() {
    }

    @FunctionalInterface
    public static interface Unregister {
        public void onChannelUnregister(ClientPacketListener var1, PacketSender var2, Minecraft var3, List<Identifier> var4);
    }

    @FunctionalInterface
    public static interface Register {
        public void onChannelRegister(ClientPacketListener var1, PacketSender var2, Minecraft var3, List<Identifier> var4);
    }
}

