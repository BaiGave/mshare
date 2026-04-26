/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jspecify.annotations.Nullable;

public class BlockModelBufferCache {
    private final MultiBufferSource.BufferSource bufferSource;
    private final OutlineBufferSource outlineBufferSource;
    private int outlineColor;
    private @Nullable RenderType lastRenderType;
    private @Nullable VertexConsumer lastBuffer;
    private @Nullable VertexConsumer lastOutlineBuffer;

    public BlockModelBufferCache(MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource) {
        this.bufferSource = bufferSource;
        this.outlineBufferSource = outlineBufferSource;
    }

    public void outlineColor(int outlineColor) {
        this.outlineColor = outlineColor;
        this.lastRenderType = null;
    }

    public VertexConsumer getBuffer(RenderType renderType) {
        if (renderType != this.lastRenderType) {
            this.update(renderType);
        }
        return this.lastBuffer;
    }

    public @Nullable VertexConsumer getOutlineBuffer(RenderType renderType) {
        if (renderType != this.lastRenderType) {
            this.update(renderType);
        }
        return this.lastOutlineBuffer;
    }

    private void update(RenderType renderType) {
        this.lastRenderType = renderType;
        this.lastBuffer = this.bufferSource.getBuffer(renderType);
        if (this.outlineColor != 0) {
            this.outlineBufferSource.setColor(this.outlineColor);
            this.lastOutlineBuffer = this.outlineBufferSource.getBuffer(renderType);
        } else {
            this.lastOutlineBuffer = null;
        }
    }
}

