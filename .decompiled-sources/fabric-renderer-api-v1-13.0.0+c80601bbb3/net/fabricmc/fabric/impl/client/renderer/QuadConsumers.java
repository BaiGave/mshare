/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.impl.client.renderer.BlockModelBufferCache;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;

public final class QuadConsumers {
    private QuadConsumers() {
    }

    public static class BreakingBlockModel
    implements Consumer<MutableQuadView> {
        public PoseStack.Pose pose;
        public VertexConsumer buffer;

        @Override
        public void accept(MutableQuadView quad) {
            quad.lightmap(0xF000F0, 0xF000F0, 0xF000F0, 0xF000F0);
            quad.buffer(OverlayTexture.NO_OVERLAY, this.pose, this.buffer);
        }
    }

    public static class BlockModel
    implements Consumer<MutableQuadView> {
        public int[] tintLayers;
        public int lightCoords;
        public int overlayCoords;
        public PoseStack.Pose pose;
        public Function<ChunkSectionLayer, RenderType> renderTypeFunction;
        public BlockModelBufferCache bufferCache;

        @Override
        public void accept(MutableQuadView quad) {
            if (quad.emissive()) {
                quad.lightmap(0xF000F0, 0xF000F0, 0xF000F0, 0xF000F0);
            } else {
                quad.minLightmap(this.lightCoords);
            }
            int tintIndex = quad.tintIndex();
            if (tintIndex != -1 && tintIndex < this.tintLayers.length) {
                quad.multiplyColor(this.tintLayers[tintIndex]);
            }
            RenderType renderType = this.renderTypeFunction.apply(quad.chunkLayer());
            quad.buffer(this.overlayCoords, this.pose, this.bufferCache.getBuffer(renderType));
            VertexConsumer outlineBuffer = this.bufferCache.getOutlineBuffer(renderType);
            if (outlineBuffer != null) {
                quad.buffer(this.overlayCoords, this.pose, outlineBuffer);
            }
        }
    }
}

