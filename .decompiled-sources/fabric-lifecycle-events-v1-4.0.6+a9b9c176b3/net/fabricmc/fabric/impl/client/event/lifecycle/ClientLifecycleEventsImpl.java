/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.event.lifecycle;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.impl.event.lifecycle.LoadedChunksCache;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ClientLifecycleEventsImpl
implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> ((LoadedChunksCache)((Object)level)).fabric_markLoaded(chunk));
        ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> ((LoadedChunksCache)((Object)level)).fabric_markUnloaded(chunk));
        ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
            for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, level);
            }
        });
    }
}

