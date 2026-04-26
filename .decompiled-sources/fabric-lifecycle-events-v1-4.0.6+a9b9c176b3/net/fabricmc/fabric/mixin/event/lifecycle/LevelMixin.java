/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.lifecycle;

import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.impl.event.lifecycle.LoadedChunksCache;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={Level.class})
public abstract class LevelMixin
implements LoadedChunksCache {
    @Unique
    private final Set<LevelChunk> loadedChunks = new HashSet<LevelChunk>();

    @Override
    public Set<LevelChunk> fabric_getLoadedChunks() {
        return this.loadedChunks;
    }

    @Override
    public void fabric_markLoaded(LevelChunk chunk) {
        this.loadedChunks.add(chunk);
    }

    @Override
    public void fabric_markUnloaded(LevelChunk chunk) {
        this.loadedChunks.remove(chunk);
    }
}

