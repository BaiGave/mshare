/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.networking.v1;

import java.util.Objects;
import java.util.Set;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.impl.networking.client.ClientConfigurationNetworkAddon;
import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public final class ClientConfigurationNetworking {
    public static <T extends CustomPacketPayload> boolean registerGlobalReceiver(CustomPacketPayload.Type<T> type, ConfigurationPayloadHandler<T> handler) {
        return ClientNetworkingImpl.CONFIGURATION.registerGlobalReceiver(type.id(), handler);
    }

    public static @Nullable ConfigurationPayloadHandler<?> unregisterGlobalReceiver(CustomPacketPayload.Type<?> type) {
        return ClientNetworkingImpl.CONFIGURATION.unregisterGlobalReceiver(type.id());
    }

    public static Set<Identifier> getGlobalReceivers() {
        return ClientNetworkingImpl.CONFIGURATION.getChannels();
    }

    public static <T extends CustomPacketPayload> boolean registerReceiver(CustomPacketPayload.Type<T> type, ConfigurationPayloadHandler<T> handler) {
        ClientConfigurationNetworkAddon addon = ClientNetworkingImpl.getClientConfigurationAddon();
        if (addon != null) {
            return addon.registerChannel(type.id(), handler);
        }
        throw new IllegalStateException("Cannot register receiver while not configuring!");
    }

    public static @Nullable ConfigurationPayloadHandler<?> unregisterReceiver(Identifier id) {
        ClientConfigurationNetworkAddon addon = ClientNetworkingImpl.getClientConfigurationAddon();
        if (addon != null) {
            return (ConfigurationPayloadHandler)addon.unregisterChannel(id);
        }
        throw new IllegalStateException("Cannot unregister receiver while not configuring!");
    }

    public static Set<Identifier> getReceived() throws IllegalStateException {
        ClientConfigurationNetworkAddon addon = ClientNetworkingImpl.getClientConfigurationAddon();
        if (addon != null) {
            return addon.getReceivableChannels();
        }
        throw new IllegalStateException("Cannot get a list of channels the client can receive packets on while not configuring!");
    }

    public static Set<Identifier> getSendable() throws IllegalStateException {
        ClientConfigurationNetworkAddon addon = ClientNetworkingImpl.getClientConfigurationAddon();
        if (addon != null) {
            return addon.getSendableChannels();
        }
        throw new IllegalStateException("Cannot get a list of channels the server can receive packets on while not configuring!");
    }

    public static boolean canSend(Identifier channelName) throws IllegalArgumentException {
        ClientConfigurationNetworkAddon addon = ClientNetworkingImpl.getClientConfigurationAddon();
        if (addon != null) {
            return addon.getSendableChannels().contains(channelName);
        }
        throw new IllegalStateException("Cannot get a list of channels the server can receive packets on while not configuring!");
    }

    public static boolean canSend(CustomPacketPayload.Type<?> type) {
        return ClientConfigurationNetworking.canSend(type.id());
    }

    public static PacketSender getSender() throws IllegalStateException {
        ClientConfigurationNetworkAddon addon = ClientNetworkingImpl.getClientConfigurationAddon();
        if (addon != null) {
            return addon;
        }
        throw new IllegalStateException("Cannot get PacketSender while not configuring!");
    }

    public static void send(CustomPacketPayload payload) {
        Objects.requireNonNull(payload, "Payload cannot be null");
        Objects.requireNonNull(payload.type(), "CustomPacketPayload#type() cannot return null for payload class: " + String.valueOf(payload.getClass()));
        ClientConfigurationNetworkAddon addon = ClientNetworkingImpl.getClientConfigurationAddon();
        if (addon != null) {
            addon.sendPacket(payload);
            return;
        }
        throw new IllegalStateException("Cannot send packet while not configuring!");
    }

    private ClientConfigurationNetworking() {
    }

    @FunctionalInterface
    public static interface ConfigurationPayloadHandler<T extends CustomPacketPayload> {
        public void receive(T var1, Context var2);
    }

    @ApiStatus.NonExtendable
    public static interface Context {
        public Minecraft client();

        public ClientConfigurationPacketListenerImpl packetListener();

        public PacketSender responseSender();

        default public PacketContext packetContext() {
            return this.packetListener().getPacketContext();
        }
    }
}

