/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.networking.v1;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import net.fabricmc.fabric.api.networking.v1.LoginPacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.fabricmc.fabric.mixin.networking.accessor.ServerLoginPacketListenerImplAccessor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public final class ServerLoginNetworking {
    public static boolean registerGlobalReceiver(Identifier channelName, LoginQueryResponseHandler channelHandler) {
        return ServerNetworkingImpl.LOGIN.registerGlobalReceiver(channelName, channelHandler);
    }

    public static @Nullable LoginQueryResponseHandler unregisterGlobalReceiver(Identifier channelName) {
        return ServerNetworkingImpl.LOGIN.unregisterGlobalReceiver(channelName);
    }

    public static Set<Identifier> getGlobalReceivers() {
        return ServerNetworkingImpl.LOGIN.getChannels();
    }

    public static boolean registerReceiver(ServerLoginPacketListenerImpl packetListener, Identifier channelName, LoginQueryResponseHandler responseHandler) {
        Objects.requireNonNull(packetListener, "Packet listener cannot be null");
        return ServerNetworkingImpl.getAddon(packetListener).registerChannel(channelName, responseHandler);
    }

    public static @Nullable LoginQueryResponseHandler unregisterReceiver(ServerLoginPacketListenerImpl packetListener, Identifier channelName) {
        Objects.requireNonNull(packetListener, "Packet listener cannot be null");
        return (LoginQueryResponseHandler)ServerNetworkingImpl.getAddon(packetListener).unregisterChannel(channelName);
    }

    public static MinecraftServer getServer(ServerLoginPacketListenerImpl listener) {
        Objects.requireNonNull(listener, "Packet listener cannot be null");
        return ((ServerLoginPacketListenerImplAccessor)((Object)listener)).getServer();
    }

    public static LoginPacketSender getSender(ServerLoginPacketListenerImpl listener) {
        Objects.requireNonNull(listener, "Packet listener cannot be null");
        return ServerNetworkingImpl.getAddon(listener);
    }

    private ServerLoginNetworking() {
    }

    @FunctionalInterface
    public static interface LoginQueryResponseHandler {
        public void receive(MinecraftServer var1, ServerLoginPacketListenerImpl var2, boolean var3, FriendlyByteBuf var4, LoginSynchronizer var5, PacketSender var6);
    }

    @FunctionalInterface
    @ApiStatus.NonExtendable
    public static interface LoginSynchronizer {
        public void waitFor(Future<?> var1);
    }
}

