/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking.server;

import io.netty.channel.ChannelFutureListener;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.networking.v1.ClientboundConfigurationChannelEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.impl.networking.AbstractChanneledNetworkAddon;
import net.fabricmc.fabric.impl.networking.ChannelInfoHolder;
import net.fabricmc.fabric.impl.networking.NetworkingImpl;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.fabricmc.fabric.mixin.networking.accessor.ServerCommonPacketListenerImplAccessor;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.jspecify.annotations.Nullable;

public final class ServerConfigurationNetworkAddon
extends AbstractChanneledNetworkAddon<ServerConfigurationNetworking.ConfigurationPacketHandler<?>> {
    private final ServerConfigurationPacketListenerImpl listener;
    private final MinecraftServer server;
    private final ServerConfigurationNetworking.Context context;
    private RegisterState registerState = RegisterState.NOT_SENT;
    private @Nullable String clientBrand = null;
    private boolean isReconfiguring = false;

    public ServerConfigurationNetworkAddon(ServerConfigurationPacketListenerImpl listener, MinecraftServer server) {
        super(ServerNetworkingImpl.CONFIGURATION, ((ServerCommonPacketListenerImplAccessor)((Object)listener)).getConnection(), "ServerConfigurationNetworkAddon for " + listener.getOwner().name());
        this.listener = listener;
        this.server = server;
        this.context = new ContextImpl(server, listener, this);
        this.registerPendingChannels((ChannelInfoHolder)((Object)this.connection), ConnectionProtocol.CONFIGURATION);
    }

    @Override
    public boolean handle(CustomPacketPayload payload) {
        if (payload instanceof BrandPayload) {
            BrandPayload brandPayload = (BrandPayload)payload;
            this.clientBrand = brandPayload.brand();
            return false;
        }
        return super.handle(payload);
    }

    @Override
    protected boolean isOnReceiveThread() {
        return true;
    }

    @Override
    protected void invokeInitEvent() {
    }

    public void preConfiguration() {
        ServerConfigurationConnectionEvents.BEFORE_CONFIGURE.invoker().onSendConfiguration(this.listener, this.server);
    }

    public void configuration() {
        ServerConfigurationConnectionEvents.CONFIGURE.invoker().onSendConfiguration(this.listener, this.server);
    }

    public boolean startConfiguration() {
        if (this.registerState == RegisterState.NOT_SENT) {
            this.sendInitialChannelRegistrationPacket();
            this.sendPacket(new ClientboundPingPacket(16430876));
            this.registerState = RegisterState.SENT;
            return true;
        }
        if (this.registerState != RegisterState.RECEIVED && this.registerState != RegisterState.NOT_RECEIVED) {
            throw new IllegalStateException();
        }
        return false;
    }

    @Override
    protected void receiveRegistration(boolean register, RegistrationPayload resolvable) {
        super.receiveRegistration(register, resolvable);
        if (register && this.registerState == RegisterState.SENT) {
            this.registerState = RegisterState.RECEIVED;
            this.listener.startConfiguration();
        }
    }

    public void onPong(int parameter) {
        if (this.registerState == RegisterState.SENT) {
            this.registerState = RegisterState.NOT_RECEIVED;
            this.listener.startConfiguration();
        }
    }

    @Override
    protected void receive(ServerConfigurationNetworking.ConfigurationPacketHandler<?> listener, CustomPacketPayload payload) {
        listener.receive(payload, this.context);
    }

    @Override
    protected void schedule(Runnable task) {
        this.server.execute(task);
    }

    @Override
    public Packet<?> createPacket(CustomPacketPayload packet) {
        return ServerConfigurationNetworking.createClientboundPacket(packet);
    }

    @Override
    protected void invokeRegisterEvent(List<Identifier> ids) {
        ClientboundConfigurationChannelEvents.REGISTER.invoker().onChannelRegister(this.listener, this, this.server, ids);
    }

    @Override
    protected void invokeUnregisterEvent(List<Identifier> ids) {
        ClientboundConfigurationChannelEvents.UNREGISTER.invoker().onChannelUnregister(this.listener, this, this.server, ids);
    }

    @Override
    protected void handleRegistration(Identifier channelName) {
        RegistrationPayload registrationPayload;
        if (this.registerState != RegisterState.NOT_SENT && (registrationPayload = this.createRegistrationPayload(RegistrationPayload.REGISTER, Collections.singleton(channelName))) != null) {
            this.sendPacket(registrationPayload);
        }
    }

    @Override
    protected void handleUnregistration(Identifier channelName) {
        RegistrationPayload registrationPayload;
        if (this.registerState != RegisterState.NOT_SENT && (registrationPayload = this.createRegistrationPayload(RegistrationPayload.UNREGISTER, Collections.singleton(channelName))) != null) {
            this.sendPacket(registrationPayload);
        }
    }

    @Override
    protected void invokeDisconnectEvent() {
        ServerConfigurationConnectionEvents.DISCONNECT.invoker().onConfigureDisconnect(this.listener, this.server);
    }

    @Override
    protected boolean isReservedChannel(Identifier channelName) {
        return NetworkingImpl.isReservedCommonChannel(channelName);
    }

    @Override
    public void sendPacket(Packet<?> packet, ChannelFutureListener callback) {
        this.listener.send(packet, callback);
    }

    public @Nullable String getClientBrand() {
        return this.clientBrand;
    }

    public boolean isReconfiguring() {
        return this.isReconfiguring;
    }

    public void setReconfiguring() {
        this.isReconfiguring = true;
    }

    public ChannelInfoHolder getChannelInfoHolder() {
        return (ChannelInfoHolder)((Object)((ServerCommonPacketListenerImplAccessor)((Object)this.listener)).getConnection());
    }

    private static enum RegisterState {
        NOT_SENT,
        SENT,
        RECEIVED,
        NOT_RECEIVED;

    }

    private record ContextImpl(MinecraftServer server, ServerConfigurationPacketListenerImpl packetListener, PacketSender responseSender) implements ServerConfigurationNetworking.Context
    {
        private ContextImpl {
            Objects.requireNonNull(server, "server");
            Objects.requireNonNull(packetListener, "packetListener");
            Objects.requireNonNull(responseSender, "responseSender");
        }
    }
}

