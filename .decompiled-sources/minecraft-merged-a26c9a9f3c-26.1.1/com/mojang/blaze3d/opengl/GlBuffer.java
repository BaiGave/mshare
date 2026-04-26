/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.jtracy.MemoryPool;
import com.mojang.jtracy.TracyClient;
import java.nio.ByteBuffer;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class GlBuffer
extends GpuBuffer {
    protected static final MemoryPool MEMORY_POOl = TracyClient.createMemoryPool("GPU Buffers");
    protected boolean closed;
    protected final @Nullable Supplier<String> label;
    private final DirectStateAccess dsa;
    protected final int handle;
    protected @Nullable ByteBuffer persistentBuffer;

    protected GlBuffer(@Nullable Supplier<String> label, DirectStateAccess dsa, @GpuBuffer.Usage int usage, long size, int handle, @Nullable ByteBuffer persistentBuffer) {
        super(usage, size);
        this.label = label;
        this.dsa = dsa;
        this.handle = handle;
        this.persistentBuffer = persistentBuffer;
        int clampedSize = (int)Math.min(size, Integer.MAX_VALUE);
        MEMORY_POOl.malloc(handle, clampedSize);
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        if (this.persistentBuffer != null) {
            this.dsa.unmapBuffer(this.handle, this.usage());
            this.persistentBuffer = null;
        }
        GlStateManager._glDeleteBuffers(this.handle);
        MEMORY_POOl.free(this.handle);
    }

    @Environment(value=EnvType.CLIENT)
    public static class GlMappedView
    implements GpuBuffer.MappedView {
        private final Runnable unmap;
        private final GlBuffer buffer;
        private final ByteBuffer data;
        private boolean closed;

        protected GlMappedView(Runnable unmap, GlBuffer buffer, ByteBuffer data) {
            this.unmap = unmap;
            this.buffer = buffer;
            this.data = data;
        }

        @Override
        public ByteBuffer data() {
            return this.data;
        }

        @Override
        public void close() {
            if (this.closed) {
                return;
            }
            this.closed = true;
            this.unmap.run();
        }
    }
}

