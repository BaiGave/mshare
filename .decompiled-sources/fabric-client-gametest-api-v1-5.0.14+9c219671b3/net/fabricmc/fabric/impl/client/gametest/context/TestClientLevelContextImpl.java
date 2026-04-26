/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.gametest.context;

import java.util.Objects;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestClientLevelContext;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;
import net.fabricmc.fabric.mixin.client.gametest.ClientChunkCacheAccessor;
import net.fabricmc.fabric.mixin.client.gametest.ClientChunkCacheStorageAccessor;
import net.fabricmc.fabric.mixin.client.gametest.ClientLevelAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public class TestClientLevelContextImpl
implements TestClientLevelContext {
    private final ClientGameTestContext context;

    public TestClientLevelContextImpl(ClientGameTestContext context) {
        this.context = context;
    }

    @Override
    public int waitForChunksDownload(int timeout) {
        ThreadingImpl.checkOnGametestThread("waitForChunksDownload");
        return this.context.waitFor(TestClientLevelContextImpl::areChunksLoaded, timeout);
    }

    @Override
    public int waitForChunksRender(boolean waitForDownload, int timeout) {
        ThreadingImpl.checkOnGametestThread("waitForChunksRender");
        return this.context.waitFor(client -> (!waitForDownload || TestClientLevelContextImpl.areChunksLoaded(client)) && TestClientLevelContextImpl.areChunksRendered(client), timeout);
    }

    private static boolean areChunksLoaded(Minecraft client) {
        int renderDistance = client.options.getEffectiveRenderDistance();
        ClientLevel level = Objects.requireNonNull(client.level);
        ClientChunkCache.Storage chunks = ((ClientChunkCacheAccessor)((Object)level.getChunkSource())).getStorage();
        ClientChunkCacheStorageAccessor chunksAccessor = (ClientChunkCacheStorageAccessor)((Object)chunks);
        int viewCenterX = chunksAccessor.getViewCenterX();
        int viewCenterZ = chunksAccessor.getViewCenterZ();
        for (int dz = -renderDistance; dz <= renderDistance; ++dz) {
            for (int dx = -renderDistance; dx <= renderDistance; ++dx) {
                if (level.getChunk(viewCenterX + dx, viewCenterZ + dz, ChunkStatus.FULL, false) != null) continue;
                return false;
            }
        }
        return true;
    }

    private static boolean areChunksRendered(Minecraft client) {
        ClientLevel level = Objects.requireNonNull(client.level);
        return ((ClientLevelAccessor)((Object)level)).getLightUpdateQueue().isEmpty() && client.levelRenderer.hasRenderedAllSections();
    }
}

