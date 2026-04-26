/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.attachment;

import java.util.Map;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentTargetInfo;
import net.fabricmc.fabric.mixin.attachment.AttachmentTargetsMixin;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={ImposterProtoChunk.class})
abstract class ImposterProtoChunkMixin
extends AttachmentTargetsMixin {
    @Shadow
    @Final
    private LevelChunk wrapped;

    ImposterProtoChunkMixin() {
    }

    @Override
    public <T> @Nullable T getAttached(AttachmentType<T> type) {
        return this.wrapped.getAttached(type);
    }

    @Override
    public <T> @Nullable T setAttached(AttachmentType<T> type, @Nullable T value) {
        return this.wrapped.setAttached(type, value);
    }

    @Override
    public boolean hasAttached(AttachmentType<?> type) {
        return this.wrapped.hasAttached(type);
    }

    @Override
    public void fabric_writeAttachmentsToNbt(ValueOutput output) {
        ((AttachmentTargetImpl)((Object)this.wrapped)).fabric_writeAttachmentsToNbt(output);
    }

    @Override
    public void fabric_readAttachmentsFromNbt(ValueInput input) {
        ((AttachmentTargetImpl)((Object)this.wrapped)).fabric_readAttachmentsFromNbt(input);
    }

    @Override
    public boolean fabric_hasPersistentAttachments() {
        return ((AttachmentTargetImpl)((Object)this.wrapped)).fabric_hasPersistentAttachments();
    }

    @Override
    public Map<AttachmentType<?>, ?> fabric_getAttachments() {
        return ((AttachmentTargetImpl)((Object)this.wrapped)).fabric_getAttachments();
    }

    @Override
    public boolean fabric_shouldTryToSync() {
        return ((AttachmentTargetImpl)((Object)this.wrapped)).fabric_shouldTryToSync();
    }

    @Override
    public void fabric_computeInitialSyncChanges(ServerPlayer player, Consumer<AttachmentChange> changeOutput) {
        ((AttachmentTargetImpl)((Object)this.wrapped)).fabric_computeInitialSyncChanges(player, changeOutput);
    }

    @Override
    public AttachmentTargetInfo<?> fabric_getSyncTargetInfo() {
        return ((AttachmentTargetImpl)((Object)this.wrapped)).fabric_getSyncTargetInfo();
    }

    @Override
    public void fabric_syncChange(AttachmentType<?> type, AttachmentChange change) {
        ((AttachmentTargetImpl)((Object)this.wrapped)).fabric_syncChange(type, change);
    }

    @Override
    public void fabric_markChanged(AttachmentType<?> type) {
        ((AttachmentTargetImpl)((Object)this.wrapped)).fabric_markChanged(type);
    }

    @Override
    public RegistryAccess fabric_getRegistryAccess() {
        return ((AttachmentTargetImpl)((Object)this.wrapped)).fabric_getRegistryAccess();
    }
}

