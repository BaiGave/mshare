/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.HiddenByteBuf;

public class LocalFrameDecoder
extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ctx.fireChannelRead(HiddenByteBuf.unpack(msg));
    }
}

