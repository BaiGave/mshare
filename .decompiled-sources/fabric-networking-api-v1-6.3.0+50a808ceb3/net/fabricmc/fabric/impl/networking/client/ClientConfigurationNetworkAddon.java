/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking.client;

import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ServerboundConfigurationChannelEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.impl.networking.ChannelInfoHolder;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;
import net.fabricmc.fabric.impl.networking.client.ClientCommonNetworkAddon;
import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;
import net.fabricmc.fabric.mixin.networking.client.accessor.ClientCommonPacketListenerImplAccessor;
import net.fabricmc.fabric.mixin.networking.client.accessor.ClientConfigurationPacketListenerImplAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public final class ClientConfigurationNetworkAddon
extends ClientCommonNetworkAddon<ClientConfigurationNetworking.ConfigurationPayloadHandler<?>, ClientConfigurationPacketListenerImpl> {
    private final ContextImpl context;
    private boolean sentInitialRegisterPacket;
    private boolean hasStarted;

    public ClientConfigurationNetworkAddon(ClientConfigurationPacketListenerImpl listener, Minecraft client) {
        super(ClientNetworkingImpl.CONFIGURATION, ((ClientCommonPacketListenerImplAccessor)((Object)listener)).getConnection(), "ClientPlayNetworkAddon for " + ((ClientConfigurationPacketListenerImplAccessor)((Object)listener)).getLocalGameProfile().name(), listener, client);
        this.context = new ContextImpl(client, listener, this);
        this.registerPendingChannels((ChannelInfoHolder)((Object)this.connection), ConnectionProtocol.CONFIGURATION);
    }

    @Override
    protected void invokeInitEvent() {
        ClientConfigurationConnectionEvents.INIT.invoker().onConfigurationInit((ClientConfigurationPacketListenerImpl)this.listener, this.client);
    }

    @Override
    public void onServerReady() {
        super.onServerReady();
        this.invokeStartEvent();
    }

    @Override
    protected void receiveRegistration(boolean register, RegistrationPayload payload) {
        super.receiveRegistration(register, payload);
        if (register && !this.sentInitialRegisterPacket) {
            this.sendInitialChannelRegistrationPacket();
            this.sentInitialRegisterPacket = true;
            this.onServerReady();
        }
    }

    @Override
    public boolean handle(CustomPacketPayload payload) {
        boolean result = super.handle(payload);
        if (payload instanceof BrandPayload) {
            this.invokeStartEvent();
        }
        return result;
    }

    @Override
    protected boolean isOnReceiveThread() {
        return true;
    }

    private void invokeStartEvent() {
        if (!this.hasStarted) {
            this.hasStarted = true;
            ClientConfigurationConnectionEvents.START.invoker().onConfigurationStart((ClientConfigurationPacketListenerImpl)this.listener, this.client);
        }
    }

    @Override
    protected void receive(ClientConfigurationNetworking.ConfigurationPayloadHandler<?> handler, CustomPacketPayload payload) {
        handler.receive(payload, this.context);
    }

    @Override
    public Packet<?> createPacket(CustomPacketPayload packet) {
        return ClientPlayNetworking.createServerboundPacket(packet);
    }

    @Override
    protected void invokeRegisterEvent(List<Identifier> ids) {
        ServerboundConfigurationChannelEvents.REGISTER.invoker().onChannelRegister((ClientConfigurationPacketListenerImpl)this.listener, this, this.client, ids);
    }

    @Override
    protected void invokeUnregisterEvent(List<Identifier> ids) {
        ServerboundConfigurationChannelEvents.UNREGISTER.invoker().onChannelUnregister((ClientConfigurationPacketListenerImpl)this.listener, this, this.client, ids);
    }

    public void handleComplete() {
        ClientConfigurationConnectionEvents.COMPLETE.invoker().onConfigurationComplete((ClientConfigurationPacketListenerImpl)this.listener, this.client);
        ClientConfigurationConnectionEvents.READY.invoker().onConfigurationReady((ClientConfigurationPacketListenerImpl)this.listener, this.client);
        ClientNetworkingImpl.setClientConfigurationAddon(null);
    }

    @Override
    protected void invokeDisconnectEvent() {
        ClientConfigurationConnectionEvents.DISCONNECT.invoker().onConfigurationDisconnect((ClientConfigurationPacketListenerImpl)this.listener, this.client);
    }

    public ChannelInfoHolder getChannelInfoHolder() {
        return (ChannelInfoHolder)((Object)((ClientCommonPacketListenerImplAccessor)((Object)this.listener)).getConnection());
    }

    private record ContextImpl(Minecraft client, ClientConfigurationPacketListenerImpl packetListener, PacketSender responseSender) implements ClientConfigurationNetworking.Context
    {
        private ContextImpl {
            Objects.requireNonNull(client, "client");
            Objects.requireNonNull(packetListener, "packetListener");
            Objects.requireNonNull(responseSender, "responseSender");
        }
    }
}

