/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.submit;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricSubmitNodeCollection;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={SubmitNodeCollection.class})
abstract class SubmitNodeCollectionMixin
implements OrderedSubmitNodeCollector,
FabricSubmitNodeCollection {
    @Shadow
    private boolean wasUsed;
    @Unique
    private final List<FabricSubmitNodeCollection.ExtendedBlockModelSubmit> extendedBlockModelSubmits = new ArrayList<FabricSubmitNodeCollection.ExtendedBlockModelSubmit>();
    @Unique
    private final List<FabricSubmitNodeCollection.ExtendedItemSubmit> extendedItemSubmits = new ArrayList<FabricSubmitNodeCollection.ExtendedItemSubmit>();

    SubmitNodeCollectionMixin() {
    }

    @Override
    public void submitBlockModel(PoseStack poseStack, Function<ChunkSectionLayer, RenderType> renderTypeFunction, boolean translucent, List<BlockStateModelPart> parts, @Nullable Mesh mesh, int[] tintLayers, int lightCoords, int overlayCoords, int outlineColor) {
        this.wasUsed = true;
        this.extendedBlockModelSubmits.add(new FabricSubmitNodeCollection.ExtendedBlockModelSubmit(poseStack.last().copy(), renderTypeFunction, translucent, parts, mesh, tintLayers, lightCoords, overlayCoords, outlineColor));
    }

    @Override
    public void submitItem(PoseStack poseStack, ItemDisplayContext displayContext, int lightCoords, int overlayCoords, int outlineColor, int[] tintLayers, List<BakedQuad> quads, MeshView mesh, ItemStackRenderState.FoilType foilType) {
        this.wasUsed = true;
        this.extendedItemSubmits.add(new FabricSubmitNodeCollection.ExtendedItemSubmit(poseStack.last().copy(), displayContext, lightCoords, overlayCoords, outlineColor, tintLayers, quads, mesh, foilType));
    }

    @Override
    public List<FabricSubmitNodeCollection.ExtendedBlockModelSubmit> getExtendedBlockModelSubmits() {
        return this.extendedBlockModelSubmits;
    }

    @Override
    public List<FabricSubmitNodeCollection.ExtendedItemSubmit> getExtendedItemSubmits() {
        return this.extendedItemSubmits;
    }

    @Inject(method={"clear"}, at={@At(value="RETURN")})
    private void onReturnClear(CallbackInfo ci) {
        this.extendedBlockModelSubmits.clear();
        this.extendedItemSubmits.clear();
    }
}

