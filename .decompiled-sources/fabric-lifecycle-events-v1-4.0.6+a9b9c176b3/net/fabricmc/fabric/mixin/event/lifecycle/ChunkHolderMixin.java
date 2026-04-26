/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.lifecycle;

import java.util.concurrent.Executor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.impl.event.lifecycle.FullChunkStatusEventTracker;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ChunkHolder.class})
public abstract class ChunkHolderMixin
extends GenerationChunkHolder
implements FullChunkStatusEventTracker {
    @Shadow
    @Final
    private LevelHeightAccessor levelHeightAccessor;
    @Shadow
    private int oldTicketLevel;
    @Unique
    private static final FullChunkStatus[] fabric_FULL_CHUNK_STATUSES = FullChunkStatus.values();
    @Unique
    private FullChunkStatus fabric_currentEventFullChunkStatus = FullChunkStatus.INACCESSIBLE;

    private ChunkHolderMixin(ChunkPos pos) {
        super(pos);
    }

    @Inject(method={"updateFutures"}, at={@At(value="INVOKE", target="Lnet/minecraft/server/level/ChunkHolder;addSaveDependency(Ljava/util/concurrent/CompletableFuture;)V", shift=At.Shift.AFTER, ordinal=0)})
    private void updateFutures$inaccessibleToFull(ChunkMap chunkMap, Executor executor, CallbackInfo ci) {
        if (this.getChunkIfPresentUnchecked(ChunkStatus.FULL) instanceof LevelChunk && this.fabric_currentEventFullChunkStatus == FullChunkStatus.INACCESSIBLE) {
            ServerChunkEvents.FULL_CHUNK_STATUS_CHANGE.invoker().onFullChunkStatusChange((ServerLevel)this.levelHeightAccessor, (LevelChunk)this.getChunkIfPresentUnchecked(ChunkStatus.FULL), FullChunkStatus.INACCESSIBLE, FullChunkStatus.FULL);
            this.fabric_currentEventFullChunkStatus = FullChunkStatus.FULL;
        }
    }

    @Inject(method={"updateFutures"}, at={@At(value="INVOKE", target="Lnet/minecraft/server/level/ChunkHolder;addSaveDependency(Ljava/util/concurrent/CompletableFuture;)V", shift=At.Shift.AFTER, ordinal=1)})
    private void updateFutures$fullToBlockTicking(ChunkMap chunkMap, Executor executor, CallbackInfo ci) {
        if (this.fabric_currentEventFullChunkStatus == FullChunkStatus.FULL) {
            ServerChunkEvents.FULL_CHUNK_STATUS_CHANGE.invoker().onFullChunkStatusChange((ServerLevel)this.levelHeightAccessor, (LevelChunk)this.getChunkIfPresentUnchecked(ChunkStatus.FULL), FullChunkStatus.FULL, FullChunkStatus.BLOCK_TICKING);
            this.fabric_currentEventFullChunkStatus = FullChunkStatus.BLOCK_TICKING;
        }
    }

    @Inject(method={"updateFutures"}, at={@At(value="INVOKE", target="Lnet/minecraft/server/level/ChunkHolder;addSaveDependency(Ljava/util/concurrent/CompletableFuture;)V", shift=At.Shift.AFTER, ordinal=2)})
    private void updateFutures$blockTickingToEntityTicking(ChunkMap chunkMap, Executor executor, CallbackInfo ci) {
        if (this.fabric_currentEventFullChunkStatus == FullChunkStatus.BLOCK_TICKING) {
            ServerChunkEvents.FULL_CHUNK_STATUS_CHANGE.invoker().onFullChunkStatusChange((ServerLevel)this.levelHeightAccessor, (LevelChunk)this.getChunkIfPresentUnchecked(ChunkStatus.FULL), FullChunkStatus.BLOCK_TICKING, FullChunkStatus.ENTITY_TICKING);
            this.fabric_currentEventFullChunkStatus = FullChunkStatus.ENTITY_TICKING;
        }
    }

    @Inject(method={"demoteFullChunk"}, at={@At(value="HEAD")})
    private void decreaseLevel(ChunkMap chunkMap, FullChunkStatus target, CallbackInfo ci) {
        FullChunkStatus previous = ChunkLevel.fullStatus(this.oldTicketLevel);
        ServerLevel serverLevel = (ServerLevel)this.levelHeightAccessor;
        for (int i = previous.ordinal(); i > target.ordinal(); --i) {
            FullChunkStatus oldStatus = fabric_FULL_CHUNK_STATUSES[i];
            FullChunkStatus newStatus = fabric_FULL_CHUNK_STATUSES[i - 1];
            if (!this.fabric_currentEventFullChunkStatus.isOrAfter(oldStatus)) continue;
            ServerChunkEvents.FULL_CHUNK_STATUS_CHANGE.invoker().onFullChunkStatusChange(serverLevel, (LevelChunk)this.getChunkIfPresentUnchecked(ChunkStatus.FULL), oldStatus, newStatus);
            this.fabric_currentEventFullChunkStatus = newStatus;
        }
    }

    @Override
    public void fabric_setCurrentEventFullChunkStatus(FullChunkStatus chunkStatus) {
        this.fabric_currentEventFullChunkStatus = chunkStatus;
    }

    @Override
    public FullChunkStatus fabric_getCurrentEventFullChunkStatus() {
        return this.fabric_currentEventFullChunkStatus;
    }
}

