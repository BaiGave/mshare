/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.networking.v1;

import java.util.Objects;
import java.util.Set;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.fabricmc.fabric.mixin.networking.accessor.ServerCommonPacketListenerImplAccessor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public final class ServerConfigurationNetworking {
    public static <T extends CustomPacketPayload> boolean registerGlobalReceiver(CustomPacketPayload.Type<T> type, ConfigurationPacketHandler<T> handler) {
        return ServerNetworkingImpl.CONFIGURATION.registerGlobalReceiver(type.id(), handler);
    }

    public static @Nullable ConfigurationPacketHandler<?> unregisterGlobalReceiver(Identifier id) {
        return ServerNetworkingImpl.CONFIGURATION.unregisterGlobalReceiver(id);
    }

    public static Set<Identifier> getGlobalReceivers() {
        return ServerNetworkingImpl.CONFIGURATION.getChannels();
    }

    public static <T extends CustomPacketPayload> boolean registerReceiver(ServerConfigurationPacketListenerImpl packetListener, CustomPacketPayload.Type<T> type, ConfigurationPacketHandler<T> handler) {
        return ServerNetworkingImpl.getAddon(packetListener).registerChannel(type.id(), handler);
    }

    public static @Nullable ConfigurationPacketHandler<?> unregisterReceiver(ServerConfigurationPacketListenerImpl packetListener, Identifier id) {
        return (ConfigurationPacketHandler)ServerNetworkingImpl.getAddon(packetListener).unregisterChannel(id);
    }

    public static Set<Identifier> getReceived(ServerConfigurationPacketListenerImpl listener) {
        Objects.requireNonNull(listener, "Server configuration packet listener cannot be null");
        return ServerNetworkingImpl.getAddon(listener).getReceivableChannels();
    }

    public static Set<Identifier> getSendable(ServerConfigurationPacketListenerImpl listener) {
        Objects.requireNonNull(listener, "Server configuration packet listener cannot be null");
        return ServerNetworkingImpl.getAddon(listener).getSendableChannels();
    }

    public static boolean canSend(ServerConfigurationPacketListenerImpl listener, Identifier channelName) {
        Objects.requireNonNull(listener, "Server configuration packet listener cannot be null");
        Objects.requireNonNull(channelName, "Channel name cannot be null");
        return ServerNetworkingImpl.getAddon(listener).getSendableChannels().contains(channelName);
    }

    public static boolean canSend(ServerConfigurationPacketListenerImpl packetListener, CustomPacketPayload.Type<?> id) {
        Objects.requireNonNull(packetListener, "Server configuration packet listener cannot be null");
        Objects.requireNonNull(id, "Payload id cannot be null");
        return ServerNetworkingImpl.getAddon(packetListener).getSendableChannels().contains(id.id());
    }

    public static Packet<ClientCommonPacketListener> createClientboundPacket(CustomPacketPayload payload) {
        Objects.requireNonNull(payload, "Payload cannot be null");
        Objects.requireNonNull(payload.type(), "CustomPacketPayload#type() cannot return null for payload class: " + String.valueOf(payload.getClass()));
        return ServerNetworkingImpl.createClientboundPacket(payload);
    }

    public static PacketSender getSender(ServerConfigurationPacketListenerImpl listener) {
        Objects.requireNonNull(listener, "Server configuration packet listener cannot be null");
        return ServerNetworkingImpl.getAddon(listener);
    }

    public static void send(ServerConfigurationPacketListenerImpl listener, CustomPacketPayload payload) {
        Objects.requireNonNull(listener, "Server configuration listener cannot be null");
        Objects.requireNonNull(payload, "Payload cannot be null");
        Objects.requireNonNull(payload.type(), "CustomPacketPayload#type() cannot return null for payload class: " + String.valueOf(payload.getClass()));
        listener.send(ServerConfigurationNetworking.createClientboundPacket(payload));
    }

    public static MinecraftServer getServer(ServerConfigurationPacketListenerImpl listener) {
        Objects.requireNonNull(listener, "Packet listener cannot be null");
        return ((ServerCommonPacketListenerImplAccessor)((Object)listener)).getServer();
    }

    public static boolean isReconfiguring(ServerConfigurationPacketListenerImpl listener) {
        Objects.requireNonNull(listener, "Server configuration packet listener cannot be null");
        return ServerNetworkingImpl.getAddon(listener).isReconfiguring();
    }

    private ServerConfigurationNetworking() {
    }

    @FunctionalInterface
    public static interface ConfigurationPacketHandler<T extends CustomPacketPayload> {
        public void receive(T var1, Context var2);
    }

    @ApiStatus.NonExtendable
    public static interface Context {
        public MinecraftServer server();

        public ServerConfigurationPacketListenerImpl packetListener();

        public PacketSender responseSender();

        default public PacketContext packetContext() {
            return this.packetListener().getPacketContext();
        }
    }
}

