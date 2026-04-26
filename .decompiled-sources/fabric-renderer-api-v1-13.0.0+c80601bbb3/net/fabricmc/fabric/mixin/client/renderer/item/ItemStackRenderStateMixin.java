/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.item;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.impl.client.renderer.LayerRenderStateExtension;
import net.fabricmc.fabric.impl.client.renderer.QuadToPosPipe;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemStackRenderState.class})
abstract class ItemStackRenderStateMixin {
    ItemStackRenderStateMixin() {
    }

    @Inject(method={"visitExtents(Ljava/util/function/Consumer;)V"}, at={@At(value="NEW", target="com/mojang/blaze3d/vertex/PoseStack$Pose")})
    private void afterInitVecLoad(Consumer<Vector3fc> posConsumer, CallbackInfo ci, @Local(name={"scratch"}) Vector3f scratch, @Share(value="pipe") LocalRef<QuadToPosPipe> pipeRef) {
        pipeRef.set(new QuadToPosPipe(posConsumer, scratch));
    }

    @Inject(method={"visitExtents(Ljava/util/function/Consumer;)V"}, at={@At(value="INVOKE", target="Lcom/mojang/blaze3d/vertex/PoseStack$Pose;setIdentity()V")})
    private void afterLayerLoad(Consumer<Vector3fc> posConsumer, CallbackInfo ci, @Local(name={"scratch"}) Vector3f vec, @Local(name={"layer"}) ItemStackRenderState.LayerRenderState layer, @Local(name={"poseTransform"}) Matrix4f matrix, @Share(value="pipe") LocalRef<QuadToPosPipe> pipeRef) {
        MutableMesh mutableMesh = ((LayerRenderStateExtension)((Object)layer)).fabric_getMutableMesh();
        if (mutableMesh != null && mutableMesh.size() > 0) {
            QuadToPosPipe pipe = pipeRef.get();
            pipe.matrix = matrix;
            mutableMesh.forEachMutable(pipe);
        }
    }
}

