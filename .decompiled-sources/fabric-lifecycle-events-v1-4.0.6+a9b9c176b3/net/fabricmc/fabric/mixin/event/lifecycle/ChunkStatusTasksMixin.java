/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.lifecycle;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.impl.event.lifecycle.FullChunkStatusEventTracker;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ChunkStatusTasks.class})
abstract class ChunkStatusTasksMixin {
    @Unique
    private static final FullChunkStatus[] fabric_FULL_CHUNK_STATUSES = FullChunkStatus.values();

    ChunkStatusTasksMixin() {
    }

    @Inject(method={"lambda$full$0"}, at={@At(value="TAIL")})
    private static void onChunkLoad(ChunkAccess chunk, WorldGenContext worldGenContext, GenerationChunkHolder chunkHolder, CallbackInfoReturnable<ChunkAccess> callbackInfoReturnable) {
        LevelChunk levelChunk = (LevelChunk)callbackInfoReturnable.getReturnValue();
        boolean generated = !(chunk instanceof ImposterProtoChunk);
        ServerChunkEvents.CHUNK_LOAD.invoker().onChunkLoad(worldGenContext.level(), levelChunk, generated);
        if (generated) {
            ServerChunkEvents.CHUNK_GENERATE.invoker().onChunkGenerate(worldGenContext.level(), levelChunk);
        }
        FullChunkStatusEventTracker chunkStatusTracker = (FullChunkStatusEventTracker)((Object)chunkHolder);
        for (int i = chunkStatusTracker.fabric_getCurrentEventFullChunkStatus().ordinal(); i < chunkHolder.getFullStatus().ordinal(); ++i) {
            FullChunkStatus oldStatus = fabric_FULL_CHUNK_STATUSES[i];
            FullChunkStatus newStatus = fabric_FULL_CHUNK_STATUSES[i + 1];
            ServerChunkEvents.FULL_CHUNK_STATUS_CHANGE.invoker().onFullChunkStatusChange(worldGenContext.level(), levelChunk, oldStatus, newStatus);
            chunkStatusTracker.fabric_setCurrentEventFullChunkStatus(newStatus);
        }
    }
}

