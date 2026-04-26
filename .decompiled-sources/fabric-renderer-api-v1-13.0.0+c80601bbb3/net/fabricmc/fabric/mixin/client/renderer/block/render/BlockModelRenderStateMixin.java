/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.block.render;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricBlockModelRenderState;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={BlockModelRenderState.class})
public abstract class BlockModelRenderStateMixin
implements FabricBlockModelRenderState {
    @Shadow
    private @Nullable List<BlockStateModelPart> modelParts;
    @Shadow
    private @Nullable Matrix4fc transformation;
    @Shadow
    private @Nullable RenderType renderType;
    @Shadow
    private @Nullable IntList tintLayers;
    @Shadow
    private @Nullable RandomSource randomSource;
    @Unique
    private @Nullable MutableMesh mesh;

    @Shadow
    private static @Nullable Matrix4fc identityToNull(Matrix4fc transformation) {
        return null;
    }

    @Override
    public QuadEmitter setupMesh(Matrix4fc transformation, boolean hasTranslucency) {
        this.transformation = BlockModelRenderStateMixin.identityToNull(transformation);
        RenderType renderType = this.renderType = hasTranslucency ? Sheets.translucentBlockSheet() : Sheets.cutoutBlockSheet();
        if (this.mesh == null) {
            this.mesh = Renderer.get().mutableMesh();
        } else {
            this.mesh.clear();
        }
        if (this.modelParts != null) {
            this.modelParts.clear();
        }
        return this.mesh.emitter();
    }

    @Inject(method={"clear()V"}, at={@At(value="RETURN")})
    private void onReturnClear(CallbackInfo ci) {
        this.mesh = null;
    }

    @ModifyReturnValue(method={"isEmpty()Z"}, at={@At(value="RETURN")})
    private boolean modifyIsEmpty(boolean original) {
        return original && this.mesh == null;
    }

    @Inject(method={"setupModel"}, at={@At(value="RETURN")})
    private void onReturnSetupModel(CallbackInfoReturnable<List<BlockStateModelPart>> cir) {
        if (this.mesh != null) {
            this.mesh.clear();
        }
    }

    @Inject(method={"submitModel"}, at={@At(value="HEAD")}, cancellable=true)
    private void submitMesh(RenderType renderType, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, int outlineColor, CallbackInfo ci) {
        if (this.mesh != null && this.mesh.size() > 0) {
            int[] tints;
            ObjectArrayList<BlockStateModelPart> modelPartsCopy = this.modelParts != null && !this.modelParts.isEmpty() ? new ObjectArrayList<BlockStateModelPart>(this.modelParts) : Collections.emptyList();
            Mesh meshCopy = this.mesh.immutableCopy();
            int[] nArray = tints = this.tintLayers != null ? this.tintLayers.toArray(BlockModelRenderState.EMPTY_TINTS) : BlockModelRenderState.EMPTY_TINTS;
            if (this.transformation != null) {
                poseStack.pushPose();
                poseStack.mulPose(this.transformation);
                submitNodeCollector.submitBlockModel(poseStack, chunkSectionLayer -> renderType, renderType.hasBlending(), modelPartsCopy, meshCopy, tints, lightCoords, overlayCoords, outlineColor);
                poseStack.popPose();
            } else {
                submitNodeCollector.submitBlockModel(poseStack, chunkSectionLayer -> renderType, renderType.hasBlending(), modelPartsCopy, meshCopy, tints, lightCoords, overlayCoords, outlineColor);
            }
            ci.cancel();
        }
    }
}

