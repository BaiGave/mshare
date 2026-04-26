/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.event.lifecycle;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.impl.event.lifecycle.LoadedChunksCache;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

public final class LifecycleEventsImpl
implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> ((LoadedChunksCache)((Object)level)).fabric_markLoaded(chunk));
        ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> ((LoadedChunksCache)((Object)level)).fabric_markUnloaded(chunk));
        ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
            for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, level);
            }
        });
        ServerLevelEvents.UNLOAD.register((server, level) -> {
            for (LevelChunk chunk : ((LoadedChunksCache)((Object)level)).fabric_getLoadedChunks()) {
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, level);
                }
            }
            for (Entity entity : level.getAllEntities()) {
                ServerEntityEvents.ENTITY_UNLOAD.invoker().onUnload(entity, level);
            }
        });
    }
}

