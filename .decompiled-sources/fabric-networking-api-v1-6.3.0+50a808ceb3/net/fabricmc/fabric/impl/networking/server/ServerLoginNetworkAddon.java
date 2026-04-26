/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking.server;

import io.netty.channel.ChannelFutureListener;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.fabricmc.fabric.api.networking.v1.LoginPacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.impl.networking.AbstractNetworkAddon;
import net.fabricmc.fabric.impl.networking.payload.FriendlyByteBufLoginQueryRequestPayload;
import net.fabricmc.fabric.impl.networking.payload.FriendlyByteBufLoginQueryResponse;
import net.fabricmc.fabric.impl.networking.server.QueryIdFactory;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.fabricmc.fabric.mixin.networking.accessor.ServerLoginPacketListenerImplAccessor;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.jspecify.annotations.Nullable;

public final class ServerLoginNetworkAddon
extends AbstractNetworkAddon<ServerLoginNetworking.LoginQueryResponseHandler>
implements LoginPacketSender {
    private final Connection connection;
    private final ServerLoginPacketListenerImpl listener;
    private final MinecraftServer server;
    private final QueryIdFactory queryIdFactory;
    private final Collection<Future<?>> waits = new ConcurrentLinkedQueue();
    private final Map<Integer, Identifier> channels = new ConcurrentHashMap<Integer, Identifier>();
    private boolean firstQueryTick = true;

    public ServerLoginNetworkAddon(ServerLoginPacketListenerImpl listener) {
        super(ServerNetworkingImpl.LOGIN, "ServerLoginNetworkAddon for " + listener.getUserName());
        this.connection = ((ServerLoginPacketListenerImplAccessor)((Object)listener)).getConnection();
        this.listener = listener;
        this.server = ((ServerLoginPacketListenerImplAccessor)((Object)listener)).getServer();
        this.queryIdFactory = QueryIdFactory.create();
    }

    @Override
    protected void invokeInitEvent() {
        ServerLoginConnectionEvents.INIT.invoker().onLoginInit(this.listener, this.server);
    }

    public boolean queryTick() {
        if (this.firstQueryTick) {
            this.sendCompressionPacket();
            ServerLoginConnectionEvents.QUERY_START.invoker().onLoginStart(this.listener, this.server, this, this.waits::add);
            this.firstQueryTick = false;
        }
        AtomicReference error = new AtomicReference();
        this.waits.removeIf(future -> {
            if (!future.isDone()) {
                return false;
            }
            try {
                future.get();
            }
            catch (ExecutionException ex) {
                Throwable caught = ex.getCause();
                error.getAndUpdate(oldEx -> {
                    if (oldEx == null) {
                        return caught;
                    }
                    oldEx.addSuppressed(caught);
                    return oldEx;
                });
            }
            catch (InterruptedException | CancellationException exception) {
                // empty catch block
            }
            return true;
        });
        return this.channels.isEmpty() && this.waits.isEmpty();
    }

    private void sendCompressionPacket() {
        if (this.server.getCompressionThreshold() >= 0 && !this.connection.isMemoryConnection()) {
            this.connection.send(new ClientboundLoginCompressionPacket(this.server.getCompressionThreshold()), PacketSendListener.thenRun(() -> this.connection.setupCompression(this.server.getCompressionThreshold(), true)));
        }
    }

    public boolean handle(ServerboundCustomQueryAnswerPacket packet) {
        FriendlyByteBufLoginQueryResponse response = (FriendlyByteBufLoginQueryResponse)packet.payload();
        return this.handle(packet.transactionId(), response == null ? null : response.data());
    }

    private boolean handle(int queryId, @Nullable FriendlyByteBuf originalBuf) {
        this.logger.debug("Handling inbound login query with id {}", (Object)queryId);
        Identifier channel = this.channels.remove(queryId);
        if (channel == null) {
            this.logger.warn("Query ID {} was received but no query has been associated in {}!", (Object)queryId, (Object)this.connection);
            return false;
        }
        boolean understood = originalBuf != null;
        @Nullable ServerLoginNetworking.LoginQueryResponseHandler handler = (ServerLoginNetworking.LoginQueryResponseHandler)this.getHandler(channel);
        if (handler == null) {
            return false;
        }
        FriendlyByteBuf buf = understood ? FriendlyByteBufs.slice(originalBuf) : FriendlyByteBufs.empty();
        try {
            handler.receive(this.server, this.listener, understood, buf, this.waits::add, this);
        }
        catch (Throwable ex) {
            this.logger.error("Encountered exception while handling in channel \"{}\"", (Object)channel, (Object)ex);
            throw ex;
        }
        return true;
    }

    @Override
    public Packet<?> createPacket(CustomPacketPayload packet) {
        throw new UnsupportedOperationException("Cannot send CustomPayload during login");
    }

    @Override
    public Packet<?> createPacket(Identifier channelName, FriendlyByteBuf buf) {
        int queryId = this.queryIdFactory.nextId();
        return new ClientboundCustomQueryPacket(queryId, new FriendlyByteBufLoginQueryRequestPayload(channelName, buf));
    }

    @Override
    public void sendPacket(Packet<?> packet, ChannelFutureListener callback) {
        Objects.requireNonNull(packet, "Packet cannot be null");
        this.connection.send(packet, callback);
    }

    @Override
    public void disconnect(Component disconnectReason) {
        Objects.requireNonNull(disconnectReason, "Disconnect reason cannot be null");
        this.connection.disconnect(disconnectReason);
    }

    public void registerOutgoingPacket(ClientboundCustomQueryPacket packet) {
        this.channels.put(packet.transactionId(), packet.payload().id());
    }

    @Override
    protected void handleRegistration(Identifier channelName) {
    }

    @Override
    protected void handleUnregistration(Identifier channelName) {
    }

    @Override
    protected void invokeDisconnectEvent() {
        ServerLoginConnectionEvents.DISCONNECT.invoker().onLoginDisconnect(this.listener, this.server);
    }

    @Override
    protected boolean isReservedChannel(Identifier channelName) {
        return false;
    }
}

