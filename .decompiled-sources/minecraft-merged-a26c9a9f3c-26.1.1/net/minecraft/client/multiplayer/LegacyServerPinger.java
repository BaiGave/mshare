/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.multiplayer;

import com.google.common.base.Splitter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.server.network.LegacyProtocolUtils;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class LegacyServerPinger
extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Splitter SPLITTER = Splitter.on('\u0000').limit(6);
    private final ServerAddress address;
    private final Output output;

    public LegacyServerPinger(ServerAddress address, Output output) {
        this.address = address;
        this.output = output;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ByteBuf toSend = ctx.alloc().buffer();
        try {
            toSend.writeByte(254);
            toSend.writeByte(1);
            toSend.writeByte(250);
            LegacyProtocolUtils.writeLegacyString(toSend, "MC|PingHost");
            int sizeIndex = toSend.writerIndex();
            toSend.writeShort(0);
            int payloadStart = toSend.writerIndex();
            toSend.writeByte(127);
            LegacyProtocolUtils.writeLegacyString(toSend, this.address.getHost());
            toSend.writeInt(this.address.getPort());
            int payloadSize = toSend.writerIndex() - payloadStart;
            toSend.setShort(sizeIndex, payloadSize);
            ctx.channel().writeAndFlush(toSend).addListener((GenericFutureListener)ChannelFutureListener.CLOSE_ON_FAILURE);
        }
        catch (Exception e) {
            toSend.release();
            throw e;
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        String str;
        List<String> split;
        short firstByte = msg.readUnsignedByte();
        if (firstByte == 255 && "\u00a71".equals((split = SPLITTER.splitToList(str = LegacyProtocolUtils.readLegacyString(msg))).get(0))) {
            int protocolVersion = Mth.getInt(split.get(1), 0);
            String version = split.get(2);
            String motd = split.get(3);
            int curPlayers = Mth.getInt(split.get(4), -1);
            int maxPlayers = Mth.getInt(split.get(5), -1);
            this.output.handleResponse(protocolVersion, version, motd, curPlayers, maxPlayers);
        }
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface Output {
        public void handleResponse(int var1, String var2, String var3, int var4, int var5);
    }
}

