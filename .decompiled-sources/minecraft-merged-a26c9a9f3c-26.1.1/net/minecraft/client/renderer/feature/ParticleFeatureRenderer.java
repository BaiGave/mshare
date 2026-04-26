/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.feature;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Queue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureManager;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ParticleFeatureRenderer
implements AutoCloseable {
    private final Queue<ParticleBufferCache> availableBuffers = new ArrayDeque<ParticleBufferCache>();
    private final List<ParticleBufferCache> usedBuffers = new ArrayList<ParticleBufferCache>();

    public void renderSolid(SubmitNodeCollection nodeCollection) {
        this.render(nodeCollection, false);
    }

    public void renderTranslucent(SubmitNodeCollection nodeCollection) {
        this.render(nodeCollection, true);
    }

    private void render(SubmitNodeCollection nodeCollection, boolean translucent) {
        if (nodeCollection.getParticleGroupRenderers().isEmpty()) {
            return;
        }
        GpuDevice device = RenderSystem.getDevice();
        Minecraft minecraft = Minecraft.getInstance();
        TextureManager textureManager = minecraft.getTextureManager();
        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        RenderTarget particleTarget = minecraft.levelRenderer.getParticlesTarget();
        for (SubmitNodeCollector.ParticleGroupRenderer particleGroupRenderer : nodeCollection.getParticleGroupRenderers()) {
            if (particleGroupRenderer.isEmpty()) continue;
            ParticleBufferCache buffer = this.availableBuffers.poll();
            if (buffer == null) {
                buffer = new ParticleBufferCache();
            }
            this.usedBuffers.add(buffer);
            QuadParticleRenderState.PreparedBuffers prepared = particleGroupRenderer.prepare(buffer, translucent);
            if (prepared == null) continue;
            boolean useParticleTarget = particleTarget != null && translucent;
            GpuTextureView colorTextureView = useParticleTarget ? particleTarget.getColorTextureView() : mainTarget.getColorTextureView();
            GpuTextureView depthTextureView = useParticleTarget ? particleTarget.getDepthTextureView() : mainTarget.getDepthTextureView();
            RenderPass renderPass = device.createCommandEncoder().createRenderPass(() -> "Particles - " + (translucent ? "Translucent" : "Solid"), colorTextureView, OptionalInt.empty(), depthTextureView, OptionalDouble.empty());
            try {
                this.prepareRenderPass(renderPass);
                particleGroupRenderer.render(prepared, buffer, renderPass, textureManager);
            }
            finally {
                if (renderPass == null) continue;
                renderPass.close();
            }
        }
    }

    public void endFrame() {
        for (ParticleBufferCache usedBuffer : this.usedBuffers) {
            usedBuffer.rotate();
        }
        this.availableBuffers.addAll(this.usedBuffers);
        this.usedBuffers.clear();
    }

    private void prepareRenderPass(RenderPass renderPass) {
        renderPass.setUniform("Projection", RenderSystem.getProjectionMatrixBuffer());
        renderPass.setUniform("Fog", RenderSystem.getShaderFog());
        renderPass.bindTexture("Sampler2", Minecraft.getInstance().gameRenderer.lightmap(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
    }

    @Override
    public void close() {
        this.availableBuffers.forEach(ParticleBufferCache::close);
    }

    @Environment(value=EnvType.CLIENT)
    public static class ParticleBufferCache
    implements AutoCloseable {
        private @Nullable MappableRingBuffer ringBuffer;

        public void write(ByteBuffer byteBuffer) {
            if (this.ringBuffer == null || this.ringBuffer.size() < byteBuffer.remaining()) {
                if (this.ringBuffer != null) {
                    this.ringBuffer.close();
                }
                this.ringBuffer = new MappableRingBuffer(() -> "Particle Vertices", 34, byteBuffer.remaining());
            }
            try (GpuBuffer.MappedView view = RenderSystem.getDevice().createCommandEncoder().mapBuffer(this.ringBuffer.currentBuffer().slice(), false, true);){
                view.data().put(byteBuffer);
            }
        }

        public GpuBuffer get() {
            if (this.ringBuffer == null) {
                throw new IllegalStateException("Can't get buffer before it's made");
            }
            return this.ringBuffer.currentBuffer();
        }

        void rotate() {
            if (this.ringBuffer != null) {
                this.ringBuffer.rotate();
            }
        }

        @Override
        public void close() {
            if (this.ringBuffer != null) {
                this.ringBuffer.close();
            }
        }
    }
}

