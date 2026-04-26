/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.HiddenByteBuf;

public class LocalFrameEncoder
extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        ctx.write(HiddenByteBuf.pack(msg), promise);
    }
}

