/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.attachment;

import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentTargetInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BlockEntity.class})
abstract class BlockEntityMixin
implements AttachmentTargetImpl {
    @Shadow
    @Final
    protected BlockPos worldPosition;
    @Shadow
    protected @Nullable Level level;

    BlockEntityMixin() {
    }

    @Shadow
    public abstract void setChanged();

    @Shadow
    public abstract boolean hasLevel();

    @Inject(method={"loadWithComponents"}, at={@At(value="RETURN")})
    private void readBlockEntityAttachments(ValueInput input, CallbackInfo ci) {
        this.fabric_readAttachmentsFromNbt(input);
    }

    @Inject(method={"saveWithoutMetadata(Lnet/minecraft/world/level/storage/ValueOutput;)V"}, at={@At(value="TAIL")})
    private void writeBlockEntityAttachments(ValueOutput output, CallbackInfo ci) {
        this.fabric_writeAttachmentsToNbt(output);
    }

    @Override
    public void fabric_markChanged(AttachmentType<?> type) {
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            ChunkHolder chunkHolder = serverLevel.getChunkSource().chunkMap.getUpdatingChunkIfPresent(ChunkPos.pack(this.worldPosition));
            if (chunkHolder == null) {
                return;
            }
            CompletableFuture<ChunkResult<LevelChunk>> chunkFuture = chunkHolder.getFullChunkFuture();
            if (chunkFuture.isDone()) {
                chunkFuture.thenAccept(chunkResult -> chunkResult.ifSuccess(levelChunk -> this.setChanged()));
            } else {
                MinecraftServer server = serverLevel.getServer();
                server.schedule(server.wrapRunnable(() -> this.fabric_markChanged(type)));
            }
        } else {
            this.setChanged();
        }
    }

    @Override
    public AttachmentTargetInfo<?> fabric_getSyncTargetInfo() {
        return new AttachmentTargetInfo.BlockEntityTarget(this.worldPosition);
    }

    @Override
    public void fabric_syncChange(AttachmentType<?> type, AttachmentChange change) {
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            serverLevel.getChunkSource().blockChanged(this.worldPosition);
        }
    }

    @Override
    public boolean fabric_shouldTryToSync() {
        return !this.hasLevel() || !this.level.isClientSide();
    }

    @Override
    public boolean fabric_shouldDeferSync() {
        return true;
    }

    @Override
    public RegistryAccess fabric_getRegistryAccess() {
        return this.level.registryAccess();
    }
}

