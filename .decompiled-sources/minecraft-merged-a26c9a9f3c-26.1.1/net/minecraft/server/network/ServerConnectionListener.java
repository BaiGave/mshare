/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.local.LocalAddress;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.RateKickingConnection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.EventLoopGroupHolder;
import net.minecraft.server.network.LegacyQueryHandler;
import net.minecraft.server.network.MemoryServerHandshakePacketListenerImpl;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ServerConnectionListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final MinecraftServer server;
    public volatile boolean running;
    private final List<ChannelFuture> channels = Collections.synchronizedList(Lists.newArrayList());
    private final List<Connection> connections = Collections.synchronizedList(Lists.newArrayList());

    public ServerConnectionListener(MinecraftServer server) {
        this.server = server;
        this.running = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void startTcpServerListener(@Nullable InetAddress address, int port) throws IOException {
        List<ChannelFuture> list = this.channels;
        synchronized (list) {
            EventLoopGroupHolder eventLoopGroupHolder = EventLoopGroupHolder.remote(this.server.useNativeTransport());
            this.channels.add(((ServerBootstrap)((ServerBootstrap)new ServerBootstrap().channel(eventLoopGroupHolder.serverChannelCls())).childHandler(new ChannelInitializer<Channel>(this){
                final /* synthetic */ ServerConnectionListener this$0;
                {
                    ServerConnectionListener serverConnectionListener = this$0;
                    Objects.requireNonNull(serverConnectionListener);
                    this.this$0 = serverConnectionListener;
                }

                @Override
                protected void initChannel(Channel channel) {
                    try {
                        channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                    }
                    catch (ChannelException channelException) {
                        // empty catch block
                    }
                    ChannelPipeline pipeline = channel.pipeline().addLast("timeout", (ChannelHandler)new ReadTimeoutHandler(30));
                    if (this.this$0.server.repliesToStatus()) {
                        pipeline.addLast("legacy_query", (ChannelHandler)new LegacyQueryHandler(this.this$0.getServer()));
                    }
                    Connection.configureSerialization(pipeline, PacketFlow.SERVERBOUND, false, null);
                    int rateLimitPacketsPerSecond = this.this$0.server.getRateLimitPacketsPerSecond();
                    Connection connection = rateLimitPacketsPerSecond > 0 ? new RateKickingConnection(rateLimitPacketsPerSecond) : new Connection(PacketFlow.SERVERBOUND);
                    this.this$0.connections.add(connection);
                    connection.configurePacketHandler(pipeline);
                    connection.setListenerForServerboundHandshake(new ServerHandshakePacketListenerImpl(this.this$0.server, connection));
                }
            }).group(eventLoopGroupHolder.eventLoopGroup()).localAddress(address, port)).bind().syncUninterruptibly());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SocketAddress startMemoryChannel() {
        ChannelFuture newChannel;
        List<ChannelFuture> list = this.channels;
        synchronized (list) {
            newChannel = ((ServerBootstrap)((ServerBootstrap)new ServerBootstrap().channel(EventLoopGroupHolder.local().serverChannelCls())).childHandler(new ChannelInitializer<Channel>(this){
                final /* synthetic */ ServerConnectionListener this$0;
                {
                    ServerConnectionListener serverConnectionListener = this$0;
                    Objects.requireNonNull(serverConnectionListener);
                    this.this$0 = serverConnectionListener;
                }

                @Override
                protected void initChannel(Channel channel) {
                    Connection connection = new Connection(PacketFlow.SERVERBOUND);
                    connection.setListenerForServerboundHandshake(new MemoryServerHandshakePacketListenerImpl(this.this$0.server, connection));
                    this.this$0.connections.add(connection);
                    ChannelPipeline pipeline = channel.pipeline();
                    Connection.configureInMemoryPipeline(pipeline, PacketFlow.SERVERBOUND);
                    if (SharedConstants.DEBUG_FAKE_LATENCY_MS > 0) {
                        pipeline.addLast("latency", (ChannelHandler)new LatencySimulator(SharedConstants.DEBUG_FAKE_LATENCY_MS, SharedConstants.DEBUG_FAKE_JITTER_MS));
                    }
                    connection.configurePacketHandler(pipeline);
                }
            }).group(EventLoopGroupHolder.local().eventLoopGroup()).localAddress(LocalAddress.ANY)).bind().syncUninterruptibly();
            this.channels.add(newChannel);
        }
        return newChannel.channel().localAddress();
    }

    public void stop() {
        this.running = false;
        for (ChannelFuture channel : this.channels) {
            try {
                channel.channel().close().sync();
            }
            catch (InterruptedException ignored) {
                LOGGER.error("Interrupted whilst closing channel");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void tick() {
        List<Connection> list = this.connections;
        synchronized (list) {
            Iterator<Connection> iterator = this.connections.iterator();
            while (iterator.hasNext()) {
                Connection connection = iterator.next();
                if (connection.isConnecting()) continue;
                if (connection.isConnected()) {
                    try {
                        connection.tick();
                    }
                    catch (Exception e) {
                        if (connection.isMemoryConnection()) {
                            throw new ReportedException(CrashReport.forThrowable(e, "Ticking memory connection"));
                        }
                        LOGGER.warn("Failed to handle packet for {}", (Object)connection.getLoggableAddress(this.server.logIPs()), (Object)e);
                        MutableComponent component = Component.literal("Internal server error");
                        connection.send(new ClientboundDisconnectPacket(component), PacketSendListener.thenRun(() -> connection.disconnect(component)));
                        connection.setReadOnly();
                    }
                    continue;
                }
                iterator.remove();
                connection.handleDisconnection();
            }
        }
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public List<Connection> getConnections() {
        return this.connections;
    }

    private static class LatencySimulator
    extends ChannelInboundHandlerAdapter {
        private static final Timer TIMER = new HashedWheelTimer();
        private final int delay;
        private final int jitter;
        private final List<DelayedMessage> queuedMessages = Lists.newArrayList();

        public LatencySimulator(int delay, int jitter) {
            this.delay = delay;
            this.jitter = jitter;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            this.delayDownstream(ctx, msg);
        }

        private void delayDownstream(ChannelHandlerContext ctx, Object msg) {
            int sendDelay = this.delay + (int)(Math.random() * (double)this.jitter);
            this.queuedMessages.add(new DelayedMessage(ctx, msg));
            TIMER.newTimeout(this::onTimeout, sendDelay, TimeUnit.MILLISECONDS);
        }

        private void onTimeout(Timeout timeout) {
            DelayedMessage next = this.queuedMessages.remove(0);
            next.ctx.fireChannelRead(next.msg);
        }

        private static class DelayedMessage {
            public final ChannelHandlerContext ctx;
            public final Object msg;

            public DelayedMessage(ChannelHandlerContext ctx, Object msg) {
                this.ctx = ctx;
                this.msg = msg;
            }
        }
    }
}

