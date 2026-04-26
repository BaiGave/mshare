/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MeshView;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;
import org.jspecify.annotations.Nullable;

public interface FabricSubmitNodeCollection {
    default public List<ExtendedBlockModelSubmit> getExtendedBlockModelSubmits() {
        throw new UnsupportedOperationException("Implemented via Mixin.");
    }

    default public List<ExtendedItemSubmit> getExtendedItemSubmits() {
        throw new UnsupportedOperationException("Implemented via Mixin.");
    }

    public record ExtendedItemSubmit(PoseStack.Pose pose, ItemDisplayContext displayContext, int lightCoords, int overlayCoords, int outlineColor, int[] tintLayers, List<BakedQuad> quads, MeshView mesh, ItemStackRenderState.FoilType foilType) {
    }

    public record ExtendedBlockModelSubmit(PoseStack.Pose pose, Function<ChunkSectionLayer, RenderType> renderTypeFunction, boolean translucent, List<BlockStateModelPart> modelParts, @Nullable Mesh mesh, int[] tintLayers, int lightCoords, int overlayCoords, int outlineColor) {
    }
}

