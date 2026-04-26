/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.gametest.v1.context;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface TestClientLevelContext {
    public static final int DEFAULT_CHUNK_LOAD_TIMEOUT = 1200;

    default public int waitForChunksDownload() {
        return this.waitForChunksDownload(1200);
    }

    public int waitForChunksDownload(int var1);

    default public int waitForChunksRender() {
        return this.waitForChunksRender(1200);
    }

    default public int waitForChunksRender(int timeout) {
        return this.waitForChunksRender(true, timeout);
    }

    default public int waitForChunksRender(boolean waitForDownload) {
        return this.waitForChunksRender(waitForDownload, 1200);
    }

    public int waitForChunksRender(boolean var1, int var2);
}

