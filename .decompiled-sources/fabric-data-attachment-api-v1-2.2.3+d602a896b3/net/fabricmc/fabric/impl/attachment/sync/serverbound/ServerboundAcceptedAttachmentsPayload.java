/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.attachment.sync.serverbound;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundAcceptedAttachmentsPayload(Set<Identifier> acceptedAttachments) implements CustomPacketPayload
{
    public static final StreamCodec<FriendlyByteBuf, ServerboundAcceptedAttachmentsPayload> CODEC = StreamCodec.composite(ByteBufCodecs.collection(HashSet::new, Identifier.STREAM_CODEC), ServerboundAcceptedAttachmentsPayload::acceptedAttachments, ServerboundAcceptedAttachmentsPayload::new);
    public static final Identifier PACKET_ID = Identifier.fromNamespaceAndPath("fabric", "accepted_attachments_v1");
    public static final CustomPacketPayload.Type<ServerboundAcceptedAttachmentsPayload> ID = new CustomPacketPayload.Type(PACKET_ID);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

