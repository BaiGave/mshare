/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.attachment;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.attachment.v1.GlobalAttachments;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTypeImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentSync;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentTargetInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.PacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jspecify.annotations.Nullable;

public class GlobalAttachmentsImpl
implements GlobalAttachments,
AttachmentTargetImpl {
    private final @Nullable MinecraftServer server;

    public GlobalAttachmentsImpl(@Nullable MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void fabric_syncChange(AttachmentType<?> type, AttachmentChange change) {
        if (this.server != null) {
            this.server.getConnection().getConnections().forEach(connection -> {
                PacketListener patt0$temp = connection.getPacketListener();
                if (patt0$temp instanceof ServerGamePacketListenerImpl) {
                    ServerGamePacketListenerImpl serverGamePacketListener = (ServerGamePacketListenerImpl)patt0$temp;
                    if (((AttachmentTypeImpl)type).syncPredicate().test(this, serverGamePacketListener.player)) {
                        AttachmentSync.trySync(change, serverGamePacketListener.player);
                    }
                }
            });
        }
    }

    @Override
    public boolean fabric_shouldTryToSync() {
        return this.server != null;
    }

    @Override
    public AttachmentTargetInfo<?> fabric_getSyncTargetInfo() {
        return AttachmentTargetInfo.GlobalTarget.INSTANCE;
    }

    @Override
    public RegistryAccess fabric_getRegistryAccess() {
        if (this.server != null) {
            return this.server.registryAccess();
        }
        throw new UnsupportedOperationException("GlobalAttachments does not have a registry access on the client side.");
    }
}

