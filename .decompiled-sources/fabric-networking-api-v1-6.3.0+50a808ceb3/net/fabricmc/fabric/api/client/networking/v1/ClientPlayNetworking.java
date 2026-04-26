/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.networking.v1;

import java.util.Objects;
import java.util.Set;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;
import net.fabricmc.fabric.impl.networking.client.ClientPlayNetworkAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public final class ClientPlayNetworking {
    public static <T extends CustomPacketPayload> boolean registerGlobalReceiver(CustomPacketPayload.Type<T> type, PlayPayloadHandler<T> handler) {
        return ClientNetworkingImpl.PLAY.registerGlobalReceiver(type.id(), handler);
    }

    public static @Nullable PlayPayloadHandler<?> unregisterGlobalReceiver(Identifier id) {
        return ClientNetworkingImpl.PLAY.unregisterGlobalReceiver(id);
    }

    public static Set<Identifier> getGlobalReceivers() {
        return ClientNetworkingImpl.PLAY.getChannels();
    }

    public static <T extends CustomPacketPayload> boolean registerReceiver(CustomPacketPayload.Type<T> type, PlayPayloadHandler<T> handler) {
        ClientPlayNetworkAddon addon = ClientNetworkingImpl.getClientPlayAddon();
        if (addon != null) {
            return addon.registerChannel(type.id(), handler);
        }
        throw new IllegalStateException("Cannot register receiver while not in game!");
    }

    public static @Nullable PlayPayloadHandler<?> unregisterReceiver(Identifier id) {
        ClientPlayNetworkAddon addon = ClientNetworkingImpl.getClientPlayAddon();
        if (addon != null) {
            return (PlayPayloadHandler)addon.unregisterChannel(id);
        }
        throw new IllegalStateException("Cannot unregister receiver while not in game!");
    }

    public static Set<Identifier> getReceived() throws IllegalStateException {
        ClientPlayNetworkAddon addon = ClientNetworkingImpl.getClientPlayAddon();
        if (addon != null) {
            return addon.getReceivableChannels();
        }
        throw new IllegalStateException("Cannot get a list of channels the client can receive packets on while not in game!");
    }

    public static Set<Identifier> getSendable() throws IllegalStateException {
        ClientPlayNetworkAddon addon = ClientNetworkingImpl.getClientPlayAddon();
        if (addon != null) {
            return addon.getSendableChannels();
        }
        throw new IllegalStateException("Cannot get a list of channels the server can receive packets on while not in game!");
    }

    public static boolean canSend(Identifier channelName) throws IllegalArgumentException {
        if (Minecraft.getInstance().getConnection() != null) {
            return ClientNetworkingImpl.getAddon(Minecraft.getInstance().getConnection()).getSendableChannels().contains(channelName);
        }
        return false;
    }

    public static boolean canSend(CustomPacketPayload.Type<?> type) {
        return ClientPlayNetworking.canSend(type.id());
    }

    public static <T extends CustomPacketPayload> Packet<ServerCommonPacketListener> createServerboundPacket(T packet) {
        return ClientNetworkingImpl.createServerboundPacket(packet);
    }

    public static PacketSender getSender() throws IllegalStateException {
        if (Minecraft.getInstance().getConnection() != null) {
            return ClientNetworkingImpl.getAddon(Minecraft.getInstance().getConnection());
        }
        throw new IllegalStateException("Cannot get payload sender when not in game!");
    }

    public static void send(CustomPacketPayload payload) {
        Objects.requireNonNull(payload, "Payload cannot be null");
        Objects.requireNonNull(payload.type(), "CustomPacketPayload#type() cannot return null for payload class: " + String.valueOf(payload.getClass()));
        if (Minecraft.getInstance().getConnection() != null) {
            Minecraft.getInstance().getConnection().send(ClientPlayNetworking.createServerboundPacket(payload));
            return;
        }
        throw new IllegalStateException("Cannot send packets when not in game!");
    }

    private ClientPlayNetworking() {
    }

    @FunctionalInterface
    public static interface PlayPayloadHandler<T extends CustomPacketPayload> {
        public void receive(T var1, Context var2);
    }

    @ApiStatus.NonExtendable
    public static interface Context {
        public Minecraft client();

        public LocalPlayer player();

        public PacketSender responseSender();

        default public PacketContext packetContext() {
            return Objects.requireNonNull(this.client().getConnection(), "this.client().getConnection() is null!").getPacketContext();
        }
    }
}

