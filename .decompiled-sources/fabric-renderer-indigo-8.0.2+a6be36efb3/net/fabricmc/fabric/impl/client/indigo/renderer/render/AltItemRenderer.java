/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.indigo.renderer.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricSubmitNodeCollection;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.mixin.client.indigo.renderer.ItemFeatureRendererAccessor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.jspecify.annotations.Nullable;

public class AltItemRenderer {
    private final MutableQuadViewImpl emitter = new MutableQuadViewImpl(this){
        final /* synthetic */ AltItemRenderer this$0;
        {
            AltItemRenderer altItemRenderer = this$0;
            Objects.requireNonNull(altItemRenderer);
            this.this$0 = altItemRenderer;
            this.data = new int[EncodingFormat.TOTAL_STRIDE];
            this.clear();
        }

        @Override
        protected void emitDirectly() {
            this.this$0.bufferQuad(this);
        }
    };
    private MultiBufferSource bufferSource;
    private OutlineBufferSource outlineBufferSource;
    private boolean translucent;
    private FabricSubmitNodeCollection.ExtendedItemSubmit submit;
    private  @Nullable PoseStack.Pose foilDecalPose;

    public void prepare(MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, boolean translucent) {
        this.bufferSource = bufferSource;
        this.outlineBufferSource = outlineBufferSource;
        this.translucent = translucent;
    }

    public void clear() {
        this.bufferSource = null;
        this.outlineBufferSource = null;
    }

    public void renderItem(FabricSubmitNodeCollection.ExtendedItemSubmit submit) {
        this.submit = submit;
        if (submit.outlineColor() != 0) {
            this.outlineBufferSource.setColor(submit.outlineColor());
        }
        this.bufferQuads(submit.quads(), submit.mesh());
        this.foilDecalPose = null;
    }

    private void bufferQuads(List<BakedQuad> vanillaQuads, MeshView mesh) {
        MutableQuadViewImpl emitter = this.emitter;
        emitter.clear();
        for (int i = 0; i < vanillaQuads.size(); ++i) {
            BakedQuad q = vanillaQuads.get(i);
            emitter.fromBakedQuad(q);
            emitter.emit();
        }
        mesh.outputTo(emitter);
    }

    private void bufferQuad(MutableQuadViewImpl quad) {
        ItemStackRenderState.FoilType foilType;
        RenderType renderType = quad.itemRenderType();
        if (renderType.hasBlending() != this.translucent) {
            return;
        }
        this.shadeQuad(quad, quad.emissive());
        this.tintQuad(quad);
        FabricSubmitNodeCollection.ExtendedItemSubmit submit = this.submit;
        ItemStackRenderState.FoilType foilType2 = foilType = quad.foilType() == null ? submit.foilType() : quad.foilType();
        if (foilType != ItemStackRenderState.FoilType.NONE) {
            PoseStack.Pose foilDecalPose;
            if (foilType == ItemStackRenderState.FoilType.SPECIAL) {
                if (this.foilDecalPose == null) {
                    this.foilDecalPose = ItemFeatureRendererAccessor.fabric_computeFoilDecalPose(submit.displayContext(), submit.pose());
                }
                foilDecalPose = this.foilDecalPose;
            } else {
                foilDecalPose = null;
            }
            VertexConsumer foilBuffer = ItemFeatureRendererAccessor.fabric_getFoilBuffer(this.bufferSource, renderType, foilDecalPose);
            quad.buffer(submit.overlayCoords(), submit.pose(), foilBuffer);
        }
        if (submit.outlineColor() != 0) {
            quad.buffer(submit.overlayCoords(), submit.pose(), this.outlineBufferSource.getBuffer(renderType));
        }
        quad.buffer(submit.overlayCoords(), submit.pose(), this.bufferSource.getBuffer(renderType));
    }

    private void shadeQuad(MutableQuadViewImpl quad, boolean emissive) {
        if (emissive) {
            quad.lightmap(0xF000F0, 0xF000F0, 0xF000F0, 0xF000F0);
        } else {
            quad.minLightmap(this.submit.lightCoords());
        }
    }

    private void tintQuad(MutableQuadViewImpl quad) {
        int tintIndex = quad.tintIndex();
        if (tintIndex >= 0 && tintIndex < this.submit.tintLayers().length) {
            quad.multiplyColor(this.submit.tintLayers()[tintIndex]);
        }
    }
}

