/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.attachment;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.impl.attachment.AttachmentEntrypoint;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentTargetInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={ChunkAccess.class})
abstract class ChunkAccessMixin
implements AttachmentTargetImpl {
    @Shadow
    @Final
    protected ChunkPos chunkPos;

    ChunkAccessMixin() {
    }

    @Shadow
    public abstract ChunkPos getPos();

    @Shadow
    public abstract void markUnsaved();

    @Shadow
    public abstract ChunkStatus getPersistedStatus();

    @Override
    public AttachmentTargetInfo<?> fabric_getSyncTargetInfo() {
        return new AttachmentTargetInfo.ChunkTarget(this.chunkPos);
    }

    @Override
    public void fabric_markChanged(AttachmentType<?> type) {
        this.markUnsaved();
        if (type.isPersistent() && this.getPersistedStatus().equals(ChunkStatus.EMPTY)) {
            AttachmentEntrypoint.LOGGER.warn("Attaching persistent attachment {} to chunk {} with chunk status EMPTY. Attachment might be discarded.", (Object)type.identifier(), (Object)this.getPos());
        }
    }

    @Override
    public boolean fabric_shouldTryToSync() {
        return false;
    }

    @Override
    public RegistryAccess fabric_getRegistryAccess() {
        throw new UnsupportedOperationException("Chunk does not have a RegistryAccess.");
    }
}

