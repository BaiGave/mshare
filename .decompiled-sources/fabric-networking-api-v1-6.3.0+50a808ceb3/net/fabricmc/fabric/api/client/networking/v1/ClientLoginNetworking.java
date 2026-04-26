/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.networking.v1;

import io.netty.channel.ChannelFutureListener;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class ClientLoginNetworking {
    public static boolean registerGlobalReceiver(Identifier channelName, LoginQueryRequestHandler queryHandler) {
        return ClientNetworkingImpl.LOGIN.registerGlobalReceiver(channelName, queryHandler);
    }

    public static @Nullable LoginQueryRequestHandler unregisterGlobalReceiver(Identifier channelName) {
        return ClientNetworkingImpl.LOGIN.unregisterGlobalReceiver(channelName);
    }

    public static Set<Identifier> getGlobalReceivers() {
        return ClientNetworkingImpl.LOGIN.getChannels();
    }

    public static boolean registerReceiver(Identifier channelName, LoginQueryRequestHandler queryHandler) throws IllegalStateException {
        PacketListener packetListener;
        Connection connection = ClientNetworkingImpl.getLoginConnection();
        if (connection != null && (packetListener = connection.getPacketListener()) instanceof ClientHandshakePacketListenerImpl) {
            return ClientNetworkingImpl.getAddon((ClientHandshakePacketListenerImpl)packetListener).registerChannel(channelName, queryHandler);
        }
        throw new IllegalStateException("Cannot register receiver while client is not logging in!");
    }

    public static @Nullable LoginQueryRequestHandler unregisterReceiver(Identifier channelName) throws IllegalStateException {
        PacketListener packetListener;
        Connection connection = ClientNetworkingImpl.getLoginConnection();
        if (connection != null && (packetListener = connection.getPacketListener()) instanceof ClientHandshakePacketListenerImpl) {
            return (LoginQueryRequestHandler)ClientNetworkingImpl.getAddon((ClientHandshakePacketListenerImpl)packetListener).unregisterChannel(channelName);
        }
        throw new IllegalStateException("Cannot unregister receiver while client is not logging in!");
    }

    private ClientLoginNetworking() {
    }

    @FunctionalInterface
    public static interface LoginQueryRequestHandler {
        public CompletableFuture<@Nullable FriendlyByteBuf> receive(Minecraft var1, ClientHandshakePacketListenerImpl var2, FriendlyByteBuf var3, Consumer<ChannelFutureListener> var4);
    }
}

