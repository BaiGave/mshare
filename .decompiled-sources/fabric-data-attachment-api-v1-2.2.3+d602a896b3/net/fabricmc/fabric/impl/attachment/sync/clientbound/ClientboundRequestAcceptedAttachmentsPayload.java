/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.attachment.sync.clientbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class ClientboundRequestAcceptedAttachmentsPayload
implements CustomPacketPayload {
    public static final ClientboundRequestAcceptedAttachmentsPayload INSTANCE = new ClientboundRequestAcceptedAttachmentsPayload();
    public static final Identifier PACKET_ID = Identifier.fromNamespaceAndPath("fabric", "accepted_attachments_v1");
    public static final CustomPacketPayload.Type<ClientboundRequestAcceptedAttachmentsPayload> ID = new CustomPacketPayload.Type(PACKET_ID);
    public static final StreamCodec<FriendlyByteBuf, ClientboundRequestAcceptedAttachmentsPayload> CODEC = StreamCodec.unit(INSTANCE);

    private ClientboundRequestAcceptedAttachmentsPayload() {
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

