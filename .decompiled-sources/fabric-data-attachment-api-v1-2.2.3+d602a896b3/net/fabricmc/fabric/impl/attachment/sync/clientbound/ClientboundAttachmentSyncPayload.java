/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.attachment.sync.clientbound;

import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundAttachmentSyncPayload(AttachmentChange attachment) implements CustomPacketPayload
{
    public static final StreamCodec<FriendlyByteBuf, ClientboundAttachmentSyncPayload> CODEC = StreamCodec.composite(AttachmentChange.PACKET_CODEC, ClientboundAttachmentSyncPayload::attachment, ClientboundAttachmentSyncPayload::new);
    public static final Identifier PACKET_ID = Identifier.fromNamespaceAndPath("fabric", "attachment_sync_v1");
    public static final CustomPacketPayload.Type<ClientboundAttachmentSyncPayload> TYPE = new CustomPacketPayload.Type(PACKET_ID);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

