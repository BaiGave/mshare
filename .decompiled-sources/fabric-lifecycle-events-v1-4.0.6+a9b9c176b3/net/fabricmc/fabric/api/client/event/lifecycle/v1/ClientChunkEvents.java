/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.event.lifecycle.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.LevelChunk;

public final class ClientChunkEvents {
    public static final Event<Load> CHUNK_LOAD = EventFactory.createArrayBacked(Load.class, callbacks -> (clientLevel, chunk) -> {
        for (Load callback : callbacks) {
            callback.onChunkLoad(clientLevel, chunk);
        }
    });
    public static final Event<Unload> CHUNK_UNLOAD = EventFactory.createArrayBacked(Unload.class, callbacks -> (clientLevel, chunk) -> {
        for (Unload callback : callbacks) {
            callback.onChunkUnload(clientLevel, chunk);
        }
    });

    private ClientChunkEvents() {
    }

    @FunctionalInterface
    public static interface Unload {
        public void onChunkUnload(ClientLevel var1, LevelChunk var2);
    }

    @FunctionalInterface
    public static interface Load {
        public void onChunkLoad(ClientLevel var1, LevelChunk var2);
    }
}

