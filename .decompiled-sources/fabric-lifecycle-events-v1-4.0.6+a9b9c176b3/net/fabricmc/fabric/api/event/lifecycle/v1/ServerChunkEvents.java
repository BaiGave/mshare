/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.lifecycle.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

public final class ServerChunkEvents {
    public static final Event<Load> CHUNK_LOAD = EventFactory.createArrayBacked(Load.class, callbacks -> (serverLevel, chunk, generated) -> {
        for (Load callback : callbacks) {
            callback.onChunkLoad(serverLevel, chunk, generated);
        }
    });
    @Deprecated
    public static final Event<Generate> CHUNK_GENERATE = EventFactory.createArrayBacked(Generate.class, callbacks -> (serverLevel, chunk) -> {
        for (Generate callback : callbacks) {
            callback.onChunkGenerate(serverLevel, chunk);
        }
    });
    public static final Event<Unload> CHUNK_UNLOAD = EventFactory.createArrayBacked(Unload.class, callbacks -> (serverLevel, chunk) -> {
        for (Unload callback : callbacks) {
            callback.onChunkUnload(serverLevel, chunk);
        }
    });
    public static final Event<FullChunkStatusChange> FULL_CHUNK_STATUS_CHANGE = EventFactory.createArrayBacked(FullChunkStatusChange.class, (level, chunk, oldChunkStatus, newChunkStatus) -> {}, callbacks -> (serverLevel, chunk, oldChunkStatus, newChunkStatus) -> {
        for (FullChunkStatusChange callback : callbacks) {
            callback.onFullChunkStatusChange(serverLevel, chunk, oldChunkStatus, newChunkStatus);
        }
    });

    private ServerChunkEvents() {
    }

    @FunctionalInterface
    public static interface FullChunkStatusChange {
        public void onFullChunkStatusChange(ServerLevel var1, LevelChunk var2, FullChunkStatus var3, FullChunkStatus var4);
    }

    @FunctionalInterface
    public static interface Unload {
        public void onChunkUnload(ServerLevel var1, LevelChunk var2);
    }

    @FunctionalInterface
    public static interface Generate {
        public void onChunkGenerate(ServerLevel var1, LevelChunk var2);
    }

    @FunctionalInterface
    public static interface Load {
        public void onChunkLoad(ServerLevel var1, LevelChunk var2, boolean var3);
    }
}

