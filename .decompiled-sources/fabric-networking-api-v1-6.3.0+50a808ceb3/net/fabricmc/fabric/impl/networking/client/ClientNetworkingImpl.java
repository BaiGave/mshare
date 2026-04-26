/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking.client;

import java.util.Objects;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.impl.networking.CommonPacketsImpl;
import net.fabricmc.fabric.impl.networking.CommonRegisterPayload;
import net.fabricmc.fabric.impl.networking.CommonVersionPayload;
import net.fabricmc.fabric.impl.networking.GlobalReceiverRegistry;
import net.fabricmc.fabric.impl.networking.NetworkingImpl;
import net.fabricmc.fabric.impl.networking.PacketListenerExtensions;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.fabricmc.fabric.impl.networking.client.ClientConfigurationNetworkAddon;
import net.fabricmc.fabric.impl.networking.client.ClientLoginNetworkAddon;
import net.fabricmc.fabric.impl.networking.client.ClientPlayNetworkAddon;
import net.fabricmc.fabric.mixin.networking.client.accessor.ConnectScreenAccessor;
import net.fabricmc.fabric.mixin.networking.client.accessor.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.Nullable;

public final class ClientNetworkingImpl {
    public static final GlobalReceiverRegistry<ClientLoginNetworking.LoginQueryRequestHandler> LOGIN = new GlobalReceiverRegistry(PacketFlow.CLIENTBOUND, ConnectionProtocol.LOGIN, null);
    public static final GlobalReceiverRegistry<ClientConfigurationNetworking.ConfigurationPayloadHandler<?>> CONFIGURATION = new GlobalReceiverRegistry(PacketFlow.CLIENTBOUND, ConnectionProtocol.CONFIGURATION, PayloadTypeRegistryImpl.CLIENTBOUND_CONFIGURATION);
    public static final GlobalReceiverRegistry<ClientPlayNetworking.PlayPayloadHandler<?>> PLAY = new GlobalReceiverRegistry(PacketFlow.CLIENTBOUND, ConnectionProtocol.PLAY, PayloadTypeRegistryImpl.CLIENTBOUND_PLAY);
    public static final ScopedValue<Connection> CONNECTION_SCOPED_VALUE = ScopedValue.newInstance();
    private static ClientPlayNetworkAddon currentPlayAddon;
    private static ClientConfigurationNetworkAddon currentConfigurationAddon;

    public static ClientPlayNetworkAddon getAddon(ClientPacketListener listener) {
        return (ClientPlayNetworkAddon)((PacketListenerExtensions)((Object)listener)).getAddon();
    }

    public static ClientConfigurationNetworkAddon getAddon(ClientConfigurationPacketListenerImpl listener) {
        return (ClientConfigurationNetworkAddon)((PacketListenerExtensions)((Object)listener)).getAddon();
    }

    public static ClientLoginNetworkAddon getAddon(ClientHandshakePacketListenerImpl listener) {
        return (ClientLoginNetworkAddon)((PacketListenerExtensions)((Object)listener)).getAddon();
    }

    public static Packet<ServerCommonPacketListener> createServerboundPacket(CustomPacketPayload payload) {
        Objects.requireNonNull(payload, "Payload cannot be null");
        Objects.requireNonNull(payload.type(), "CustomPacketPayload#type() cannot return null for payload class: " + String.valueOf(payload.getClass()));
        return new ServerboundCustomPayloadPacket(payload);
    }

    public static @Nullable Connection getLoginConnection() {
        Connection connection = ((MinecraftAccessor)((Object)Minecraft.getInstance())).getPendingConnection();
        if (connection != null) {
            return connection;
        }
        if (CONNECTION_SCOPED_VALUE.isBound()) {
            return CONNECTION_SCOPED_VALUE.get();
        }
        if (Minecraft.getInstance().screen instanceof ConnectScreen) {
            return ((ConnectScreenAccessor)((Object)Minecraft.getInstance().screen)).getConnection();
        }
        return null;
    }

    public static @Nullable ClientConfigurationNetworkAddon getClientConfigurationAddon() {
        return currentConfigurationAddon;
    }

    public static @Nullable ClientPlayNetworkAddon getClientPlayAddon() {
        if (Minecraft.getInstance().getConnection() != null) {
            currentPlayAddon = null;
            return ClientNetworkingImpl.getAddon(Minecraft.getInstance().getConnection());
        }
        if (currentPlayAddon != null) {
            return currentPlayAddon;
        }
        return null;
    }

    public static void setClientPlayAddon(ClientPlayNetworkAddon addon) {
        if (addon != null && currentConfigurationAddon != null) {
            throw new IllegalStateException();
        }
        currentPlayAddon = addon;
    }

    public static void setClientConfigurationAddon(ClientConfigurationNetworkAddon addon) {
        currentConfigurationAddon = addon;
    }

    public static void clientInit() {
        ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> {
            currentPlayAddon = null;
        });
        ClientConfigurationConnectionEvents.DISCONNECT.register((listener, client) -> {
            currentConfigurationAddon = null;
        });
        ClientConfigurationNetworking.registerGlobalReceiver(CommonVersionPayload.TYPE, (listener, context) -> {
            int negotiatedVersion = ClientNetworkingImpl.handleVersionPacket(listener, context.responseSender());
            ClientNetworkingImpl.getClientConfigurationAddon().onCommonVersionPacket(negotiatedVersion);
        });
        ClientConfigurationNetworking.registerGlobalReceiver(CommonRegisterPayload.TYPE, (listener, context) -> {
            ClientConfigurationNetworkAddon addon = ClientNetworkingImpl.getClientConfigurationAddon();
            if ("play".equals(listener.protocol())) {
                if (listener.version() != addon.getNegotiatedVersion()) {
                    throw new IllegalStateException("Negotiated common packet version: %d but received packet with version: %d".formatted(addon.getNegotiatedVersion(), listener.version()));
                }
                addon.getChannelInfoHolder().fabric_getPendingChannelsNames(ConnectionProtocol.PLAY).addAll(listener.channels());
                NetworkingImpl.LOGGER.debug("Received accepted channels from the server");
                context.responseSender().sendPacket(new CommonRegisterPayload(addon.getNegotiatedVersion(), "play", ClientPlayNetworking.getGlobalReceivers()));
            } else {
                addon.onCommonRegisterPacket((CommonRegisterPayload)listener);
                context.responseSender().sendPacket(addon.createRegisterPayload());
            }
        });
    }

    private static int handleVersionPacket(CommonVersionPayload payload, PacketSender packetSender) {
        int version = CommonPacketsImpl.getHighestCommonVersion(payload.versions(), CommonPacketsImpl.SUPPORTED_COMMON_PACKET_VERSIONS);
        if (version <= 0) {
            throw new UnsupportedOperationException("Client does not support any requested versions from server");
        }
        packetSender.sendPacket(new CommonVersionPayload(new int[]{version}));
        return version;
    }
}

