/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.networking.v1;

import java.util.Objects;
import java.util.Set;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public final class ServerPlayNetworking {
    public static <T extends CustomPacketPayload> boolean registerGlobalReceiver(CustomPacketPayload.Type<T> type, PlayPayloadHandler<T> handler) {
        return ServerNetworkingImpl.PLAY.registerGlobalReceiver(type.id(), handler);
    }

    public static @Nullable PlayPayloadHandler<?> unregisterGlobalReceiver(Identifier id) {
        return ServerNetworkingImpl.PLAY.unregisterGlobalReceiver(id);
    }

    public static Set<Identifier> getGlobalReceivers() {
        return ServerNetworkingImpl.PLAY.getChannels();
    }

    public static <T extends CustomPacketPayload> boolean registerReceiver(ServerGamePacketListenerImpl packetListener, CustomPacketPayload.Type<T> type, PlayPayloadHandler<T> handler) {
        return ServerNetworkingImpl.getAddon(packetListener).registerChannel(type.id(), handler);
    }

    public static @Nullable PlayPayloadHandler<?> unregisterReceiver(ServerGamePacketListenerImpl packetListener, Identifier id) {
        return (PlayPayloadHandler)ServerNetworkingImpl.getAddon(packetListener).unregisterChannel(id);
    }

    public static Set<Identifier> getReceived(ServerPlayer player) {
        Objects.requireNonNull(player, "Server player cannot be null");
        return ServerPlayNetworking.getReceived(player.connection);
    }

    public static Set<Identifier> getReceived(ServerGamePacketListenerImpl listener) {
        Objects.requireNonNull(listener, "Server game packet listener cannot be null");
        return ServerNetworkingImpl.getAddon(listener).getReceivableChannels();
    }

    public static Set<Identifier> getSendable(ServerPlayer player) {
        Objects.requireNonNull(player, "Server player cannot be null");
        return ServerPlayNetworking.getSendable(player.connection);
    }

    public static Set<Identifier> getSendable(ServerGamePacketListenerImpl listener) {
        Objects.requireNonNull(listener, "Server game packet listener cannot be null");
        return ServerNetworkingImpl.getAddon(listener).getSendableChannels();
    }

    public static boolean canSend(ServerPlayer player, Identifier channelName) {
        Objects.requireNonNull(player, "Server player cannot be null");
        return ServerPlayNetworking.canSend(player.connection, channelName);
    }

    public static boolean canSend(ServerPlayer player, CustomPacketPayload.Type<?> type) {
        Objects.requireNonNull(player, "Server player cannot be null");
        return ServerPlayNetworking.canSend(player.connection, type.id());
    }

    public static boolean canSend(ServerGamePacketListenerImpl listener, Identifier channelName) {
        Objects.requireNonNull(listener, "Server game packet listener cannot be null");
        Objects.requireNonNull(channelName, "Channel name cannot be null");
        return ServerNetworkingImpl.getAddon(listener).getSendableChannels().contains(channelName);
    }

    public static boolean canSend(ServerGamePacketListenerImpl listener, CustomPacketPayload.Type<?> type) {
        Objects.requireNonNull(listener, "Server game packet listener cannot be null");
        Objects.requireNonNull(type, "Packet type cannot be null");
        return ServerNetworkingImpl.getAddon(listener).getSendableChannels().contains(type.id());
    }

    public static <T extends CustomPacketPayload> Packet<ClientCommonPacketListener> createClientboundPacket(T packet) {
        return ServerNetworkingImpl.createClientboundPacket(packet);
    }

    public static PacketSender getSender(ServerPlayer player) {
        Objects.requireNonNull(player, "Server player cannot be null");
        return ServerPlayNetworking.getSender(player.connection);
    }

    public static PacketSender getSender(ServerGamePacketListenerImpl listener) {
        Objects.requireNonNull(listener, "Server game packet listener cannot be null");
        return ServerNetworkingImpl.getAddon(listener);
    }

    public static void send(ServerPlayer player, CustomPacketPayload payload) {
        Objects.requireNonNull(player, "Server player cannot be null");
        Objects.requireNonNull(payload, "Payload cannot be null");
        Objects.requireNonNull(payload.type(), "CustomPacketPayload#type() cannot return null for payload class: " + String.valueOf(payload.getClass()));
        player.connection.send(ServerPlayNetworking.createClientboundPacket(payload));
    }

    public static void reconfigure(ServerPlayer player) {
        Objects.requireNonNull(player, "Server player cannot be null");
        ServerPlayNetworking.reconfigure(player.connection);
    }

    public static void reconfigure(ServerGamePacketListenerImpl listener) {
        Objects.requireNonNull(listener, "Server game packet listener cannot be null");
        ServerNetworkingImpl.getAddon(listener).reconfigure();
    }

    private ServerPlayNetworking() {
    }

    @FunctionalInterface
    public static interface PlayPayloadHandler<T extends CustomPacketPayload> {
        public void receive(T var1, Context var2);
    }

    @ApiStatus.NonExtendable
    public static interface Context {
        public MinecraftServer server();

        public ServerPlayer player();

        public PacketSender responseSender();

        default public PacketContext packetContext() {
            return this.player().connection.getPacketContext();
        }
    }
}

