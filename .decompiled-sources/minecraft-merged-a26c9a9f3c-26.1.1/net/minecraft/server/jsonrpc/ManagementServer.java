/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.jsonrpc;

import com.google.common.collect.Sets;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.server.jsonrpc.Connection;
import net.minecraft.server.jsonrpc.JsonRpcLogger;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.security.AuthenticationHandler;
import net.minecraft.server.jsonrpc.websocket.JsonToWebSocketEncoder;
import net.minecraft.server.jsonrpc.websocket.WebSocketToJsonCodec;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ManagementServer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final HostAndPort hostAndPort;
    private final AuthenticationHandler authenticationHandler;
    private @Nullable Channel serverChannel;
    private final NioEventLoopGroup nioEventLoopGroup;
    private final Set<Connection> connections = Sets.newIdentityHashSet();

    public ManagementServer(HostAndPort hostAndPort, AuthenticationHandler authenticationHandler) {
        this.hostAndPort = hostAndPort;
        this.authenticationHandler = authenticationHandler;
        this.nioEventLoopGroup = new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Management server IO #%d").setDaemon(true).build());
    }

    public ManagementServer(HostAndPort hostAndPort, AuthenticationHandler authenticationHandler, NioEventLoopGroup nioEventLoopGroup) {
        this.hostAndPort = hostAndPort;
        this.authenticationHandler = authenticationHandler;
        this.nioEventLoopGroup = nioEventLoopGroup;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void onConnected(Connection connection) {
        Set<Connection> set = this.connections;
        synchronized (set) {
            this.connections.add(connection);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void onDisconnected(Connection connection) {
        Set<Connection> set = this.connections;
        synchronized (set) {
            this.connections.remove(connection);
        }
    }

    public void startWithoutTls(MinecraftApi minecraftApi) {
        this.start(minecraftApi, null);
    }

    public void startWithTls(MinecraftApi minecraftApi, SslContext sslContext) {
        this.start(minecraftApi, sslContext);
    }

    private void start(final MinecraftApi minecraftApi, final @Nullable SslContext sslContext) {
        final JsonRpcLogger jsonrpcLogger = new JsonRpcLogger();
        ChannelFuture channel = ((ServerBootstrap)((ServerBootstrap)((ServerBootstrap)new ServerBootstrap().handler(new LoggingHandler(LogLevel.DEBUG))).channel(NioServerSocketChannel.class)).childHandler(new ChannelInitializer<Channel>(this){
            final /* synthetic */ ManagementServer this$0;
            {
                ManagementServer managementServer = this$0;
                Objects.requireNonNull(managementServer);
                this.this$0 = managementServer;
            }

            @Override
            protected void initChannel(Channel channel) {
                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                }
                catch (ChannelException channelException) {
                    // empty catch block
                }
                ChannelPipeline pipeline = channel.pipeline();
                if (sslContext != null) {
                    pipeline.addLast(sslContext.newHandler(channel.alloc()));
                }
                pipeline.addLast(new HttpServerCodec()).addLast(new HttpObjectAggregator(65536)).addLast(this.this$0.authenticationHandler).addLast(new WebSocketServerProtocolHandler("/")).addLast(new WebSocketToJsonCodec()).addLast(new JsonToWebSocketEncoder()).addLast(new Connection(channel, this.this$0, minecraftApi, jsonrpcLogger));
            }
        }).group(this.nioEventLoopGroup).localAddress(this.hostAndPort.getHost(), this.hostAndPort.getPort())).bind();
        this.serverChannel = channel.channel();
        channel.syncUninterruptibly();
        LOGGER.info("Json-RPC Management connection listening on {}:{}", (Object)this.hostAndPort.getHost(), (Object)this.getPort());
    }

    public void stop(boolean closeNioEventLoopGroup) throws InterruptedException {
        if (this.serverChannel != null) {
            this.serverChannel.close().sync();
            this.serverChannel = null;
        }
        this.connections.clear();
        if (closeNioEventLoopGroup) {
            this.nioEventLoopGroup.shutdownGracefully().sync();
        }
    }

    public void tick() {
        this.forEachConnection(Connection::tick);
    }

    public int getPort() {
        return this.serverChannel != null ? ((InetSocketAddress)this.serverChannel.localAddress()).getPort() : this.hostAndPort.getPort();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void forEachConnection(Consumer<Connection> action) {
        Set<Connection> set = this.connections;
        synchronized (set) {
            this.connections.forEach(action);
        }
    }
}

