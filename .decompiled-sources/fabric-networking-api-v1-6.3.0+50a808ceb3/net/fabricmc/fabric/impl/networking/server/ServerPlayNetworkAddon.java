/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking.server;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.networking.v1.ClientboundPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.impl.networking.AbstractChanneledNetworkAddon;
import net.fabricmc.fabric.impl.networking.ChannelInfoHolder;
import net.fabricmc.fabric.impl.networking.NetworkingImpl;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public final class ServerPlayNetworkAddon
extends AbstractChanneledNetworkAddon<ServerPlayNetworking.PlayPayloadHandler<?>> {
    private final ServerGamePacketListenerImpl listener;
    private final MinecraftServer server;
    private final ServerPlayNetworking.Context context;
    private boolean sentInitialRegisterPacket;
    private boolean requestedReconfigure = false;

    public ServerPlayNetworkAddon(ServerGamePacketListenerImpl listener, Connection connection, MinecraftServer server) {
        super(ServerNetworkingImpl.PLAY, connection, "ServerPlayNetworkAddon for " + String.valueOf(listener.player.getDisplayName()));
        this.listener = listener;
        this.server = server;
        this.context = new ContextImpl(server, listener, this);
        this.registerPendingChannels((ChannelInfoHolder)((Object)this.connection), ConnectionProtocol.PLAY);
    }

    @Override
    protected void invokeInitEvent() {
        ServerPlayConnectionEvents.INIT.invoker().onPlayInit(this.listener, this.server);
    }

    public void onClientReady() {
        ServerPlayConnectionEvents.JOIN.invoker().onPlayReady(this.listener, this, this.server);
        this.sendInitialChannelRegistrationPacket();
        this.sentInitialRegisterPacket = true;
    }

    @Override
    protected boolean isOnReceiveThread() {
        return this.server.packetProcessor().isSameThread();
    }

    @Override
    protected void receive(ServerPlayNetworking.PlayPayloadHandler<?> payloadHandler, CustomPacketPayload payload) {
        payloadHandler.receive(payload, this.context);
    }

    @Override
    protected void schedule(Runnable task) {
        this.listener.player.level().getServer().execute(task);
    }

    @Override
    public Packet<?> createPacket(CustomPacketPayload packet) {
        return ServerPlayNetworking.createClientboundPacket(packet);
    }

    @Override
    protected void invokeRegisterEvent(List<Identifier> ids) {
        ClientboundPlayChannelEvents.REGISTER.invoker().onChannelRegister(this.listener, this, this.server, ids);
    }

    @Override
    protected void invokeUnregisterEvent(List<Identifier> ids) {
        ClientboundPlayChannelEvents.UNREGISTER.invoker().onChannelUnregister(this.listener, this, this.server, ids);
    }

    @Override
    protected void handleRegistration(Identifier channelName) {
        RegistrationPayload registrationPayload;
        if (this.sentInitialRegisterPacket && (registrationPayload = this.createRegistrationPayload(RegistrationPayload.REGISTER, Collections.singleton(channelName))) != null) {
            this.sendPacket(registrationPayload);
        }
    }

    @Override
    protected void handleUnregistration(Identifier channelName) {
        RegistrationPayload registrationPayload;
        if (this.sentInitialRegisterPacket && (registrationPayload = this.createRegistrationPayload(RegistrationPayload.UNREGISTER, Collections.singleton(channelName))) != null) {
            this.sendPacket(registrationPayload);
        }
    }

    @Override
    protected void invokeDisconnectEvent() {
        ServerPlayConnectionEvents.DISCONNECT.invoker().onPlayDisconnect(this.listener, this.server);
    }

    @Override
    protected boolean isReservedChannel(Identifier channelName) {
        return NetworkingImpl.isReservedCommonChannel(channelName);
    }

    public void reconfigure() {
        if (this.requestedReconfigure) {
            throw new IllegalStateException("Already requested reconfigure");
        }
        this.requestedReconfigure = true;
        this.listener.switchToConfig();
    }

    public boolean requestedReconfigure() {
        return this.requestedReconfigure;
    }

    private record ContextImpl(MinecraftServer server, ServerGamePacketListenerImpl listener, PacketSender responseSender) implements ServerPlayNetworking.Context
    {
        private ContextImpl {
            Objects.requireNonNull(server, "server");
            Objects.requireNonNull(listener, "listener");
            Objects.requireNonNull(responseSender, "responseSender");
        }

        @Override
        public ServerPlayer player() {
            return this.listener.getPlayer();
        }

        @Override
        public PacketContext packetContext() {
            return this.listener.getPacketContext();
        }
    }
}

