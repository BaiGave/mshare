/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking.server;

import java.util.Objects;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.networking.GlobalReceiverRegistry;
import net.fabricmc.fabric.impl.networking.PacketListenerExtensions;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.fabricmc.fabric.impl.networking.server.ServerConfigurationNetworkAddon;
import net.fabricmc.fabric.impl.networking.server.ServerLoginNetworkAddon;
import net.fabricmc.fabric.impl.networking.server.ServerPlayNetworkAddon;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

public final class ServerNetworkingImpl {
    public static final GlobalReceiverRegistry<ServerLoginNetworking.LoginQueryResponseHandler> LOGIN = new GlobalReceiverRegistry(PacketFlow.SERVERBOUND, ConnectionProtocol.LOGIN, null);
    public static final GlobalReceiverRegistry<ServerConfigurationNetworking.ConfigurationPacketHandler<?>> CONFIGURATION = new GlobalReceiverRegistry(PacketFlow.SERVERBOUND, ConnectionProtocol.CONFIGURATION, PayloadTypeRegistryImpl.SERVERBOUND_CONFIGURATION);
    public static final GlobalReceiverRegistry<ServerPlayNetworking.PlayPayloadHandler<?>> PLAY = new GlobalReceiverRegistry(PacketFlow.SERVERBOUND, ConnectionProtocol.PLAY, PayloadTypeRegistryImpl.SERVERBOUND_PLAY);

    public static ServerPlayNetworkAddon getAddon(ServerGamePacketListenerImpl listener) {
        return (ServerPlayNetworkAddon)((PacketListenerExtensions)((Object)listener)).getAddon();
    }

    public static ServerLoginNetworkAddon getAddon(ServerLoginPacketListenerImpl listener) {
        return (ServerLoginNetworkAddon)((PacketListenerExtensions)((Object)listener)).getAddon();
    }

    public static ServerConfigurationNetworkAddon getAddon(ServerConfigurationPacketListenerImpl listener) {
        return (ServerConfigurationNetworkAddon)((PacketListenerExtensions)((Object)listener)).getAddon();
    }

    public static Packet<ClientCommonPacketListener> createClientboundPacket(CustomPacketPayload payload) {
        Objects.requireNonNull(payload, "Payload cannot be null");
        Objects.requireNonNull(payload.type(), "CustomPacketPayload#type() cannot return null for payload class: " + String.valueOf(payload.getClass()));
        return new ClientboundCustomPayloadPacket(payload);
    }
}

