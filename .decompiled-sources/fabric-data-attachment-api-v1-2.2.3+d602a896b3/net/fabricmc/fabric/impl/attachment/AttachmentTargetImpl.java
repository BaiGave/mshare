/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.attachment;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentTargetInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public interface AttachmentTargetImpl
extends AttachmentTarget {
    public static void transfer(AttachmentTarget original, AttachmentTarget target, boolean isDeath) {
        Map<AttachmentType<?>, ?> attachments = ((AttachmentTargetImpl)original).fabric_getAttachments();
        if (attachments == null) {
            return;
        }
        for (Map.Entry<AttachmentType<?>, ?> entry : attachments.entrySet()) {
            AttachmentType<?> type = entry.getKey();
            if (isDeath && !type.copyOnDeath()) continue;
            target.setAttached(type, entry.getValue());
        }
        ((AttachmentTargetImpl)target).fabric_clearDeferredSyncChanges();
    }

    default public @Nullable Map<AttachmentType<?>, ?> fabric_getAttachments() {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public void fabric_writeAttachmentsToNbt(ValueOutput output) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public void fabric_readAttachmentsFromNbt(ValueInput input) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public boolean fabric_hasPersistentAttachments() {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public AttachmentTargetInfo<?> fabric_getSyncTargetInfo() {
        throw new UnsupportedOperationException("Sync target info was not retrieved on server!");
    }

    default public void fabric_computeInitialSyncChanges(ServerPlayer player, Consumer<AttachmentChange> changeOutput) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public void fabric_sendAndClearDeferredSyncChanges(List<ServerPlayer> players) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public void fabric_clearDeferredSyncChanges() {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public <T> void fabric_updateSyncTarget(AttachmentTargetInfo<T> oldTargetInfo, AttachmentTargetInfo<T> newTargetInfo) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public void fabric_syncChange(AttachmentType<?> type, AttachmentChange change) {
    }

    default public void fabric_markChanged(AttachmentType<?> type) {
    }

    default public boolean fabric_shouldTryToSync() {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public boolean fabric_shouldDeferSync() {
        return false;
    }

    public RegistryAccess fabric_getRegistryAccess();
}

