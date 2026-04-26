/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking.splitter;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record FabricSplitPacketPayload(ByteBuf byteBuf) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<FabricSplitPacketPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("fabric", "split"));
    public static final StreamCodec<ByteBuf, FabricSplitPacketPayload> CODEC = StreamCodec.of(FabricSplitPacketPayload::write, FabricSplitPacketPayload::read);

    private static FabricSplitPacketPayload read(ByteBuf buf) {
        return new FabricSplitPacketPayload(buf.readBytes(buf.readableBytes()));
    }

    private static void write(ByteBuf buf, FabricSplitPacketPayload payload) {
        buf.writeBytes(payload.byteBuf());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

