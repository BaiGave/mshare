/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.render;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public final class ChunkSectionLayerHelper {
    private ChunkSectionLayerHelper() {
    }

    public static RenderType getMovingBlockRenderType(ChunkSectionLayer layer) {
        return switch (layer) {
            default -> throw new MatchException(null, null);
            case ChunkSectionLayer.SOLID -> RenderTypes.solidMovingBlock();
            case ChunkSectionLayer.CUTOUT -> RenderTypes.cutoutMovingBlock();
            case ChunkSectionLayer.TRANSLUCENT -> RenderTypes.translucentMovingBlock();
        };
    }

    public static RenderType getRenderType(boolean translucent) {
        return translucent ? Sheets.translucentBlockSheet() : Sheets.cutoutBlockSheet();
    }
}

