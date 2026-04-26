/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.ModelBakery;

@Environment(value=EnvType.CLIENT)
public class ModelPartFeatureRenderer {
    private final PoseStack poseStack = new PoseStack();

    public void renderSolid(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, MultiBufferSource.BufferSource crumblingBufferSource) {
        Storage storage = nodeCollection.getModelPartSubmits();
        this.render(storage.solidModelPartSubmits, bufferSource, outlineBufferSource, crumblingBufferSource);
    }

    public void renderTranslucent(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, MultiBufferSource.BufferSource crumblingBufferSource) {
        Storage storage = nodeCollection.getModelPartSubmits();
        this.render(storage.translucentModelPartSubmits, bufferSource, outlineBufferSource, crumblingBufferSource);
    }

    private void render(Map<RenderType, List<SubmitNodeStorage.ModelPartSubmit>> modelPartSubmitsMap, MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, MultiBufferSource.BufferSource crumblingBufferSource) {
        for (Map.Entry<RenderType, List<SubmitNodeStorage.ModelPartSubmit>> entry : modelPartSubmitsMap.entrySet()) {
            RenderType renderType = entry.getKey();
            List<SubmitNodeStorage.ModelPartSubmit> modelPartSubmits = entry.getValue();
            VertexConsumer buffer = bufferSource.getBuffer(renderType);
            for (SubmitNodeStorage.ModelPartSubmit modelPartSubmit : modelPartSubmits) {
                VertexConsumer actualBuffer = modelPartSubmit.sprite() != null ? (modelPartSubmit.hasFoil() ? modelPartSubmit.sprite().wrap(ItemFeatureRenderer.getFoilBuffer(bufferSource, renderType, modelPartSubmit.sheeted(), true)) : modelPartSubmit.sprite().wrap(buffer)) : (modelPartSubmit.hasFoil() ? ItemFeatureRenderer.getFoilBuffer(bufferSource, renderType, modelPartSubmit.sheeted(), true) : buffer);
                this.poseStack.last().set(modelPartSubmit.pose());
                modelPartSubmit.modelPart().render(this.poseStack, actualBuffer, modelPartSubmit.lightCoords(), modelPartSubmit.overlayCoords(), modelPartSubmit.tintedColor());
                if (modelPartSubmit.outlineColor() != 0 && (renderType.outline().isPresent() || renderType.isOutline())) {
                    outlineBufferSource.setColor(modelPartSubmit.outlineColor());
                    VertexConsumer outlineBuffer = outlineBufferSource.getBuffer(renderType);
                    modelPartSubmit.modelPart().render(this.poseStack, modelPartSubmit.sprite() == null ? outlineBuffer : modelPartSubmit.sprite().wrap(outlineBuffer), modelPartSubmit.lightCoords(), modelPartSubmit.overlayCoords(), modelPartSubmit.tintedColor());
                }
                if (modelPartSubmit.crumblingOverlay() == null) continue;
                SheetedDecalTextureGenerator breakingBuffer = new SheetedDecalTextureGenerator(crumblingBufferSource.getBuffer(ModelBakery.DESTROY_TYPES.get(modelPartSubmit.crumblingOverlay().progress())), modelPartSubmit.crumblingOverlay().cameraPose(), 1.0f);
                modelPartSubmit.modelPart().render(this.poseStack, breakingBuffer, modelPartSubmit.lightCoords(), modelPartSubmit.overlayCoords(), modelPartSubmit.tintedColor());
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Storage {
        private final Map<RenderType, List<SubmitNodeStorage.ModelPartSubmit>> solidModelPartSubmits = new HashMap<RenderType, List<SubmitNodeStorage.ModelPartSubmit>>();
        private final Map<RenderType, List<SubmitNodeStorage.ModelPartSubmit>> translucentModelPartSubmits = new HashMap<RenderType, List<SubmitNodeStorage.ModelPartSubmit>>();
        private final Set<RenderType> solidModelPartSubmitsUsage = new ObjectOpenHashSet<RenderType>();
        private final Set<RenderType> translucentModelPartSubmitsUsage = new ObjectOpenHashSet<RenderType>();

        public void add(RenderType renderType, SubmitNodeStorage.ModelPartSubmit submit) {
            if (!renderType.hasBlending()) {
                this.solidModelPartSubmits.computeIfAbsent(renderType, ignored -> new ArrayList()).add(submit);
            } else {
                this.translucentModelPartSubmits.computeIfAbsent(renderType, ignored -> new ArrayList()).add(submit);
            }
        }

        public void clear() {
            for (Map.Entry<RenderType, List<SubmitNodeStorage.ModelPartSubmit>> entry : this.solidModelPartSubmits.entrySet()) {
                if (entry.getValue().isEmpty()) continue;
                this.solidModelPartSubmitsUsage.add(entry.getKey());
                entry.getValue().clear();
            }
            for (Map.Entry<RenderType, List<SubmitNodeStorage.ModelPartSubmit>> entry : this.translucentModelPartSubmits.entrySet()) {
                if (entry.getValue().isEmpty()) continue;
                this.translucentModelPartSubmitsUsage.add(entry.getKey());
                entry.getValue().clear();
            }
        }

        public void endFrame() {
            this.solidModelPartSubmits.keySet().removeIf(renderType -> !this.solidModelPartSubmitsUsage.contains(renderType));
            this.solidModelPartSubmitsUsage.clear();
            this.translucentModelPartSubmits.keySet().removeIf(renderType -> !this.translucentModelPartSubmitsUsage.contains(renderType));
            this.translucentModelPartSubmitsUsage.clear();
        }
    }
}

