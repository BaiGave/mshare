/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking.client;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ServerboundPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.impl.networking.ChannelInfoHolder;
import net.fabricmc.fabric.impl.networking.client.ClientCommonNetworkAddon;
import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

public final class ClientPlayNetworkAddon
extends ClientCommonNetworkAddon<ClientPlayNetworking.PlayPayloadHandler<?>, ClientPacketListener> {
    private final ContextImpl context;
    private static final Logger LOGGER = LogUtils.getLogger();

    public ClientPlayNetworkAddon(ClientPacketListener listener, Minecraft client) {
        super(ClientNetworkingImpl.PLAY, listener.getConnection(), "ClientPlayNetworkAddon for " + listener.getLocalGameProfile().name(), listener, client);
        this.context = new ContextImpl(client, this);
        this.registerPendingChannels((ChannelInfoHolder)((Object)this.connection), ConnectionProtocol.PLAY);
    }

    @Override
    protected void invokeInitEvent() {
        ClientPlayConnectionEvents.INIT.invoker().onPlayInit((ClientPacketListener)this.listener, this.client);
    }

    @Override
    public void onServerReady() {
        try {
            ClientPlayConnectionEvents.JOIN.invoker().onPlayReady((ClientPacketListener)this.listener, this, this.client);
        }
        catch (RuntimeException e) {
            LOGGER.error("Exception thrown while invoking ClientPlayConnectionEvents.JOIN", e);
        }
        this.sendInitialChannelRegistrationPacket();
        super.onServerReady();
    }

    @Override
    protected boolean isOnReceiveThread() {
        return this.client.packetProcessor().isSameThread();
    }

    @Override
    protected void receive(ClientPlayNetworking.PlayPayloadHandler<?> handler, CustomPacketPayload payload) {
        handler.receive(payload, this.context);
    }

    @Override
    public Packet<?> createPacket(CustomPacketPayload packet) {
        return ClientPlayNetworking.createServerboundPacket(packet);
    }

    @Override
    protected void invokeRegisterEvent(List<Identifier> ids) {
        ServerboundPlayChannelEvents.REGISTER.invoker().onChannelRegister((ClientPacketListener)this.listener, this, this.client, ids);
    }

    @Override
    protected void invokeUnregisterEvent(List<Identifier> ids) {
        ServerboundPlayChannelEvents.UNREGISTER.invoker().onChannelUnregister((ClientPacketListener)this.listener, this, this.client, ids);
    }

    @Override
    protected void invokeDisconnectEvent() {
        ClientPlayConnectionEvents.DISCONNECT.invoker().onPlayDisconnect((ClientPacketListener)this.listener, this.client);
    }

    private record ContextImpl(Minecraft client, PacketSender responseSender) implements ClientPlayNetworking.Context
    {
        private ContextImpl {
            Objects.requireNonNull(client, "client");
            Objects.requireNonNull(responseSender, "responseSender");
        }

        @Override
        public LocalPlayer player() {
            return Objects.requireNonNull(this.client.player, "player");
        }
    }
}

