/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.attachment.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.impl.attachment.AttachmentEntrypoint;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentSync;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentSyncException;
import net.fabricmc.fabric.impl.attachment.sync.clientbound.ClientboundAttachmentSyncPayload;
import net.fabricmc.fabric.impl.attachment.sync.clientbound.ClientboundRequestAcceptedAttachmentsPayload;

public class AttachmentSyncClient
implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientConfigurationNetworking.registerGlobalReceiver(ClientboundRequestAcceptedAttachmentsPayload.ID, (payload, context) -> context.responseSender().sendPacket(AttachmentSync.createResponsePayload()));
        ClientPlayNetworking.registerGlobalReceiver(ClientboundAttachmentSyncPayload.TYPE, (payload, context) -> {
            try {
                payload.attachment().tryApply(context.client().level);
            }
            catch (AttachmentSyncException e) {
                AttachmentEntrypoint.LOGGER.error("Error accepting attachment changes", e);
                context.responseSender().disconnect(e.getComponent());
            }
        });
    }
}

