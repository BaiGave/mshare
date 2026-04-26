/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.impl.client.rendering.LevelRendererExtensions;
import net.fabricmc.fabric.impl.client.rendering.level.LevelExtractionContextImpl;
import net.fabricmc.fabric.impl.client.rendering.level.LevelRenderContextImpl;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LevelRenderer.class})
public abstract class LevelRendererMixin
implements LevelRendererExtensions {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private RenderBuffers renderBuffers;
    @Shadow
    @Final
    private LevelRenderState levelRenderState;
    @Shadow
    private @Nullable ClientLevel level;
    @Shadow
    @Final
    private SubmitNodeStorage submitNodeStorage;
    @Unique
    private final LevelRenderContextImpl renderContext = new LevelRenderContextImpl();
    @Unique
    private final LevelExtractionContextImpl extractionContext = new LevelExtractionContextImpl();

    @Override
    public void fabric_prepareLevelExtractionContext(DeltaTracker deltaTracker) {
        this.extractionContext.prepare(this.minecraft.gameRenderer, this.minecraft.levelRenderer, this.levelRenderState, this.level, deltaTracker, this.minecraft.gameRenderer.getMainCamera());
    }

    @Inject(method={"renderLevel"}, at={@At(value="HEAD")})
    private void beforeRender(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, ChunkSectionsToRender chunkSectionsToRender, CallbackInfo ci) {
        this.renderContext.prepare(this.minecraft.gameRenderer, (LevelRenderer)((Object)this), this.levelRenderState, chunkSectionsToRender, this.submitNodeStorage, this.renderBuffers.bufferSource());
    }

    @Inject(method={"extractBlockOutline"}, at={@At(value="RETURN")})
    private void afterBlockOutlineExtraction(Camera camera, LevelRenderState renderStates, CallbackInfo ci) {
        LevelRenderEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.invoker().afterBlockOutlineExtraction(this.extractionContext, this.minecraft.hitResult);
    }

    @Inject(method={"extractLevel"}, at={@At(value="RETURN")})
    private void afterExtractLevel(DeltaTracker deltaTracker, Camera camera, float deltaPartialTick, CallbackInfo ci) {
        LevelRenderEvents.END_EXTRACTION.invoker().endExtraction(this.extractionContext);
    }

    @WrapOperation(method={"lambda$addMainPass$0"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/blaze3d/textures/GpuSampler;)V", ordinal=0)})
    private void wrapRenderOpaqueTerrain(ChunkSectionsToRender chunkSectionsToRender, ChunkSectionLayerGroup group, GpuSampler sampler, Operation<Void> original) {
        LevelRenderEvents.START_MAIN.invoker().startMain(this.renderContext);
        original.call(new Object[]{chunkSectionsToRender, group, sampler});
        LevelRenderEvents.AFTER_OPAQUE_TERRAIN.invoker().afterOpaqueTerrain(this.renderContext);
    }

    @ModifyExpressionValue(method={"lambda$addMainPass$0"}, at={@At(value="NEW", target="Lcom/mojang/blaze3d/vertex/PoseStack;")})
    private PoseStack onCreatePoseStack(PoseStack poseStack) {
        this.renderContext.setPoseStack(poseStack);
        return poseStack;
    }

    @Inject(method={"lambda$addMainPass$0"}, at={@At(value="INVOKE_STRING", target="Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args={"ldc=renderSolidFeatures"})})
    private void afterCollectSubmits(CallbackInfo ci) {
        LevelRenderEvents.COLLECT_SUBMITS.invoker().collectSubmits(this.renderContext);
    }

    @Inject(method={"lambda$addMainPass$0"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V", ordinal=0)})
    private void afterRenderSolidFeatures(CallbackInfo ci) {
        LevelRenderEvents.AFTER_SOLID_FEATURES.invoker().afterSolidFeatures(this.renderContext);
    }

    @Inject(method={"lambda$addMainPass$0"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderTranslucentFeatures()V", shift=At.Shift.AFTER)})
    private void afterRenderTranslucentFeatures(CallbackInfo ci) {
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.invoker().afterTranslucentFeatures(this.renderContext);
    }

    @Inject(method={"renderBlockOutline"}, at={@At(value="FIELD", target="Lnet/minecraft/client/renderer/state/level/CameraRenderState;pos:Lnet/minecraft/world/phys/Vec3;", opcode=180)}, cancellable=true)
    private void beforeRenderBlockOutline(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, boolean translucent, LevelRenderState levelRenderState, CallbackInfo ci) {
        if (!LevelRenderEvents.BEFORE_BLOCK_OUTLINE.invoker().beforeBlockOutline(this.renderContext, this.renderContext.levelState().blockOutlineRenderState)) {
            bufferSource.endLastBatch();
            ci.cancel();
        }
    }

    @Inject(method={"lambda$addMainPass$0"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/LevelRenderer;finalizeGizmoCollection()V")})
    private void beforeCollectGizmos(CallbackInfo ci) {
        LevelRenderEvents.BEFORE_GIZMOS.invoker().beforeGizmos(this.renderContext);
    }

    @WrapOperation(method={"lambda$addMainPass$0"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/blaze3d/textures/GpuSampler;)V", ordinal=1)})
    private void wrapRenderTranslucentTerrain(ChunkSectionsToRender chunkSectionsToRender, ChunkSectionLayerGroup group, GpuSampler sampler, Operation<Void> original) {
        LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.invoker().beforeTranslucentTerrain(this.renderContext);
        original.call(new Object[]{chunkSectionsToRender, group, sampler});
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.invoker().afterTranslucentTerrain(this.renderContext);
    }

    @Inject(method={"lambda$addMainPass$0"}, at={@At(value="RETURN")})
    private void endMainRender(CallbackInfo ci) {
        LevelRenderEvents.END_MAIN.invoker().endMain(this.renderContext);
    }

    @Inject(method={"allChanged()V"}, at={@At(value="HEAD")})
    private void onReload(CallbackInfo ci) {
        InvalidateRenderStateCallback.EVENT.invoker().onInvalidate();
    }
}

