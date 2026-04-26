/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking.splitter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import net.fabricmc.fabric.impl.networking.GenericPayloadAccessor;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.fabricmc.fabric.impl.networking.VanillaPacketTypes;
import net.fabricmc.fabric.impl.networking.splitter.FabricSplitPacketPayload;
import net.fabricmc.fabric.mixin.networking.accessor.PacketDecoderAccessor;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.VarInt;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class FabricPacketMerger
extends MessageToMessageDecoder<Packet<?>> {
    private final PacketDecoder<?> packetDecoder;
    private final PayloadTypeRegistryImpl<?> payloadTypeRegistry;
    private final VanillaPacketTypes vanillaPacketTypes;
    private @Nullable Merger packetMerger;

    public FabricPacketMerger(PacketDecoder<?> packetDecoder, PayloadTypeRegistryImpl<?> payloadTypeRegistry, VanillaPacketTypes vanillaPacketTypes) {
        this.packetDecoder = packetDecoder;
        this.payloadTypeRegistry = payloadTypeRegistry;
        this.vanillaPacketTypes = vanillaPacketTypes;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list) throws Exception {
        GenericPayloadAccessor accessor;
        CustomPacketPayload payload2;
        if (this.packetMerger != null) {
            CustomPacketPayload payload2;
            FabricPacketMerger.ensureNotTransitioning(packet);
            if (packet instanceof GenericPayloadAccessor) {
                GenericPayloadAccessor accessor2 = (GenericPayloadAccessor)((Object)packet);
                v0 = accessor2.fabric_payload();
            } else {
                v0 = payload2 = null;
            }
            if (payload2 == null) {
                throw new DecoderException("Received '" + String.valueOf(packet.type().id()) + "' packet, while expecting 'minecraft:custom_payload'!");
            }
            if (!(payload2 instanceof FabricSplitPacketPayload)) {
                throw new DecoderException("Expected '" + String.valueOf(FabricSplitPacketPayload.TYPE.id()) + "' payload packet, but received '" + String.valueOf(payload2.type().id()) + "'!");
            }
            FabricSplitPacketPayload splitPacketPayload = (FabricSplitPacketPayload)payload2;
            if (this.packetMerger.add(channelHandlerContext, splitPacketPayload, list)) {
                this.packetMerger = null;
            }
        } else if (packet instanceof GenericPayloadAccessor && (payload2 = (accessor = (GenericPayloadAccessor)((Object)packet)).fabric_payload()) instanceof FabricSplitPacketPayload) {
            FabricSplitPacketPayload payload3 = (FabricSplitPacketPayload)payload2;
            FabricPacketMerger.ensureNotTransitioning(packet);
            ByteBuf buf = payload3.byteBuf();
            int packetSize = VarInt.read(buf);
            int readerIndex = buf.readerIndex();
            PacketType<?> packetType = this.vanillaPacketTypes.get(VarInt.read(buf));
            if (packetType != packet.type()) {
                throw new DecoderException("Received unsupported split packet type! Expected '" + String.valueOf(packet.type().id()) + " got '" + String.valueOf(packetType != null ? packetType.id() : "<NULL>") + "'!");
            }
            Identifier payloadId = (Identifier)Identifier.STREAM_CODEC.decode(payload3.byteBuf());
            buf.readerIndex(readerIndex);
            int maxSize = this.payloadTypeRegistry.getMaxPacketSizeForSplitting(payloadId);
            if (maxSize == -1) {
                throw new DecoderException("Received '" + String.valueOf(payloadId) + "' packet doesn't support splitting, but received split data!");
            }
            if (maxSize < packetSize) {
                throw new DecoderException("Received '" + String.valueOf(payloadId) + "' packet is larger than max allowed size! Got " + packetSize + " bytes, expected " + maxSize + " bytes!");
            }
            this.packetMerger = new Merger(this.packetDecoder, payloadId, packetSize);
            if (this.packetMerger.add(channelHandlerContext, payload3, list)) {
                throw new DecoderException("Received '" + String.valueOf(payloadId) + "' as a split packet, but it wasn't actually split!");
            }
        } else {
            list.add(packet);
            if (packet.isTerminal()) {
                channelHandlerContext.pipeline().remove(channelHandlerContext.name());
            }
        }
    }

    private static void ensureNotTransitioning(Packet<?> packet) {
        if (packet.isTerminal()) {
            throw new DecoderException("Terminal message received in bundle");
        }
    }

    private static class Merger {
        private final PacketDecoderAccessor packetDecoder;
        private final Identifier packetId;
        private final int finalSize;
        private final ByteBuf byteBuf;

        Merger(PacketDecoder<?> packetDecoder, Identifier identifier, int finalSize) {
            this.packetDecoder = (PacketDecoderAccessor)((Object)packetDecoder);
            this.packetId = identifier;
            this.byteBuf = Unpooled.buffer(finalSize);
            this.finalSize = finalSize;
        }

        boolean add(ChannelHandlerContext channelHandlerContext, FabricSplitPacketPayload payload, List<Object> objects) throws Exception {
            int newSize = this.byteBuf.readableBytes() + payload.byteBuf().readableBytes();
            if (this.finalSize < newSize) {
                throw new DecoderException("Received too much data for packet '" + String.valueOf(this.packetId) + "'! Expected " + this.finalSize + " bytes, received " + newSize + " bytes!");
            }
            this.byteBuf.writeBytes(payload.byteBuf());
            if (this.byteBuf.readableBytes() == this.finalSize) {
                this.packetDecoder.fabric_decode(channelHandlerContext, this.byteBuf, objects);
                return true;
            }
            return false;
        }
    }
}

