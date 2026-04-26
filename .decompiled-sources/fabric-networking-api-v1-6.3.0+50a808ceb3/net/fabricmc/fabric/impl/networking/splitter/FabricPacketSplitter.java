/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking.splitter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.fabricmc.fabric.impl.networking.splitter.FabricSplitPacketPayload;
import net.fabricmc.fabric.impl.networking.splitter.PassthroughPacket;
import net.fabricmc.fabric.impl.networking.splitter.SplittablePacket;
import net.fabricmc.fabric.mixin.networking.accessor.PacketEncoderAccessor;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.VarInt;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class FabricPacketSplitter
extends MessageToMessageEncoder<Packet<?>> {
    public static final int SAFE_S2C_SPLIT_SIZE = 0x100000;
    public static final int SAFE_C2S_SPLIT_SIZE = Short.MAX_VALUE;
    private final PacketEncoder<?> encoder;
    private final PayloadTypeRegistryImpl<?> payloadTypeRegistry;

    public FabricPacketSplitter(PacketEncoder<?> encoder, PayloadTypeRegistryImpl<?> payloadTypeRegistry) {
        this.encoder = encoder;
        this.payloadTypeRegistry = payloadTypeRegistry;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list) throws Exception {
        if (packet instanceof SplittablePacket) {
            SplittablePacket splittablePacket = (SplittablePacket)((Object)packet);
            splittablePacket.fabric_split(this.payloadTypeRegistry, channelHandlerContext, this.encoder, packet, list::add);
        } else {
            list.add(packet);
        }
        if (packet.isTerminal()) {
            channelHandlerContext.pipeline().remove(channelHandlerContext.name());
        }
    }

    public static void genericPacketSplitter(Identifier packetId, ChannelHandlerContext channelHandlerContext, PacketEncoder<?> encoder, Packet<?> packet, Function<CustomPacketPayload, Packet<?>> packetConstructor, Consumer<Packet<?>> consumer, int maxChunkSize, int maxPacketSize) throws Exception {
        ByteBuf buf = Unpooled.buffer();
        ((PacketEncoderAccessor)((Object)encoder)).fabric_encode(channelHandlerContext, packet, buf);
        if (buf.readableBytes() < maxChunkSize) {
            consumer.accept(new PassthroughPacket(buf));
            return;
        }
        if (buf.readableBytes() > maxPacketSize) {
            throw new EncoderException("Packet '" + String.valueOf(packetId) + "' may not be larger than " + maxPacketSize + " bytes!");
        }
        ByteBuf firstSplit = Unpooled.buffer(maxChunkSize);
        VarInt.write(firstSplit, buf.readableBytes());
        firstSplit.writeBytes(buf.readSlice(maxChunkSize - firstSplit.readableBytes()));
        consumer.accept(packetConstructor.apply(new FabricSplitPacketPayload(firstSplit)));
        while (buf.isReadable()) {
            consumer.accept(packetConstructor.apply(new FabricSplitPacketPayload(buf.readSlice(Math.min(buf.readableBytes(), maxChunkSize)))));
        }
    }
}

