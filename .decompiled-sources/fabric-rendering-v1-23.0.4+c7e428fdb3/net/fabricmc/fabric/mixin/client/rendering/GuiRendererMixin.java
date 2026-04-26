/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.impl.client.rendering.GuiRendererExtensions;
import net.fabricmc.fabric.impl.client.rendering.PictureInPictureRendererPool;
import net.fabricmc.fabric.impl.client.rendering.PictureInPictureRendererRegistryImpl;
import net.fabricmc.fabric.mixin.client.rendering.DrawAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GuiRenderer.class})
abstract class GuiRendererMixin
implements GuiRendererExtensions {
    @Shadow
    @Final
    @Mutable
    private Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRenderer<?>> pictureInPictureRenderers;
    @Shadow
    @Final
    private MultiBufferSource.BufferSource bufferSource;
    @Unique
    private boolean hasFabricInitialized = false;
    @Unique
    private final Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRendererPool<?>> pipRendererPools = new HashMap();
    @Unique
    private SubmitNodeCollector submitNodeStorage = null;

    GuiRendererMixin() {
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void mutableSpecialElementRenderers(GuiRenderState state, MultiBufferSource.BufferSource bufferSource, SubmitNodeCollector submitNodeCollector, FeatureRenderDispatcher renderDispatcher, List list, CallbackInfo ci) {
        this.pictureInPictureRenderers = new IdentityHashMap(this.pictureInPictureRenderers);
    }

    @Override
    public void fabric_onReady(SubmitNodeStorage submitNodeStorage) {
        this.submitNodeStorage = submitNodeStorage;
        PictureInPictureRendererRegistryImpl.onReady(Minecraft.getInstance(), this.bufferSource, submitNodeStorage, this.pictureInPictureRenderers);
        this.hasFabricInitialized = true;
    }

    @Inject(method={"preparePictureInPicture"}, at={@At(value="HEAD")})
    private void prePrepareSpecialElements(CallbackInfo ci) {
        this.pipRendererPools.values().forEach(PictureInPictureRendererPool::newFrame);
    }

    @Inject(method={"preparePictureInPicture"}, at={@At(value="RETURN")})
    private void postPrepareSpecialElements(CallbackInfo ci) {
        this.pipRendererPools.values().forEach(PictureInPictureRendererPool::cleanUpUnusedRenderers);
    }

    @ModifyVariable(method={"preparePictureInPictureState"}, at=@At(value="STORE"), name={"renderer"})
    private <T extends PictureInPictureRenderState> PictureInPictureRenderer<T> substituteSpecialElementRenderer(PictureInPictureRenderer<T> original, T elementState) {
        if (original == null || !this.hasFabricInitialized) {
            return original;
        }
        PictureInPictureRendererPool rendererPool = this.pipRendererPools.computeIfAbsent(original.getRenderStateClass(), k -> new PictureInPictureRendererPool());
        return rendererPool.substitute(original, elementState, Minecraft.getInstance(), this.bufferSource, Objects.requireNonNull(this.submitNodeStorage, "renderDispatcher"));
    }

    @Inject(method={"close"}, at={@At(value="RETURN")})
    private void closeRendererPools(CallbackInfo ci) {
        this.pipRendererPools.values().forEach(PictureInPictureRendererPool::close);
    }

    @WrapOperation(method={"executeDraw(Lnet/minecraft/client/gui/render/GuiRenderer$Draw;Lcom/mojang/blaze3d/systems/RenderPass;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;)V"}, at={@At(value="INVOKE", target="Lcom/mojang/blaze3d/systems/RenderPass;setIndexBuffer(Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;)V")})
    private void fixNonQuadIndexing(RenderPass instance, GpuBuffer buffer, VertexFormat.IndexType indexType, Operation<Void> original, @Coerce DrawAccessor draw) {
        RenderPipeline pipeline = draw.fabric$pipeline();
        if (pipeline.usePipelineDrawModeForGui() && pipeline.getVertexFormatMode() != VertexFormat.Mode.QUADS) {
            RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
            buffer = shapeIndexBuffer.getBuffer(draw.fabric$indexCount());
            indexType = shapeIndexBuffer.type();
        }
        original.call(new Object[]{instance, buffer, indexType});
    }

    @ModifyExpressionValue(method={"addElementToMesh"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/render/GuiRenderer;scissorChanged(Lnet/minecraft/client/gui/navigation/ScreenRectangle;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)Z")})
    private boolean uploadPrimitivesIndividually(boolean original, @Local RenderPipeline pipeline) {
        return original || pipeline.getVertexFormatMode().connectedPrimitives;
    }
}

