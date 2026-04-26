/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ReferenceCountUtil;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.Packet;

public class UnconfiguredPipelineHandler {
    public static <T extends PacketListener> InboundConfigurationTask setupInboundProtocol(ProtocolInfo<T> protocolInfo) {
        return UnconfiguredPipelineHandler.setupInboundHandler(new PacketDecoder<T>(protocolInfo));
    }

    private static InboundConfigurationTask setupInboundHandler(ChannelInboundHandler newHandler) {
        return ctx -> {
            ctx.pipeline().replace(ctx.name(), "decoder", (ChannelHandler)newHandler);
            ctx.channel().config().setAutoRead(true);
        };
    }

    public static <T extends PacketListener> OutboundConfigurationTask setupOutboundProtocol(ProtocolInfo<T> codecData) {
        return UnconfiguredPipelineHandler.setupOutboundHandler(new PacketEncoder<T>(codecData));
    }

    private static OutboundConfigurationTask setupOutboundHandler(ChannelOutboundHandler newHandler) {
        return ctx -> ctx.pipeline().replace(ctx.name(), "encoder", (ChannelHandler)newHandler);
    }

    @FunctionalInterface
    public static interface InboundConfigurationTask {
        public void run(ChannelHandlerContext var1);

        default public InboundConfigurationTask andThen(InboundConfigurationTask otherTask) {
            return ctx -> {
                this.run(ctx);
                otherTask.run(ctx);
            };
        }
    }

    @FunctionalInterface
    public static interface OutboundConfigurationTask {
        public void run(ChannelHandlerContext var1);

        default public OutboundConfigurationTask andThen(OutboundConfigurationTask otherTask) {
            return ctx -> {
                this.run(ctx);
                otherTask.run(ctx);
            };
        }
    }

    public static class Outbound
    extends ChannelOutboundHandlerAdapter {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (msg instanceof Packet) {
                ReferenceCountUtil.release(msg);
                throw new EncoderException("Pipeline has no outbound protocol configured, can't process packet " + String.valueOf(msg));
            }
            if (msg instanceof OutboundConfigurationTask) {
                OutboundConfigurationTask configurationTask = (OutboundConfigurationTask)msg;
                try {
                    configurationTask.run(ctx);
                }
                finally {
                    ReferenceCountUtil.release(msg);
                }
                promise.setSuccess();
            } else {
                ctx.write(msg, promise);
            }
        }
    }

    public static class Inbound
    extends ChannelDuplexHandler {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof ByteBuf || msg instanceof Packet) {
                ReferenceCountUtil.release(msg);
                throw new DecoderException("Pipeline has no inbound protocol configured, can't process packet " + String.valueOf(msg));
            }
            ctx.fireChannelRead(msg);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (msg instanceof InboundConfigurationTask) {
                InboundConfigurationTask configurationTask = (InboundConfigurationTask)msg;
                try {
                    configurationTask.run(ctx);
                }
                finally {
                    ReferenceCountUtil.release(msg);
                }
                promise.setSuccess();
            } else {
                ctx.write(msg, promise);
            }
        }
    }
}

