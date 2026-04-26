/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.event.lifecycle;

import java.util.Set;
import net.minecraft.world.level.chunk.LevelChunk;

public interface LoadedChunksCache {
    public Set<LevelChunk> fabric_getLoadedChunks();

    public void fabric_markLoaded(LevelChunk var1);

    public void fabric_markUnloaded(LevelChunk var1);
}

