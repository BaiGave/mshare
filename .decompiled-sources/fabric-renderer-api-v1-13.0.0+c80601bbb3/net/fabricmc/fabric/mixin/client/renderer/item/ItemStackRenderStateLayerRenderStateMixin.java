/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricLayerRenderState;
import net.fabricmc.fabric.impl.client.renderer.LayerRenderStateExtension;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemStackRenderState.LayerRenderState.class})
abstract class ItemStackRenderStateLayerRenderStateMixin
implements FabricLayerRenderState,
LayerRenderStateExtension {
    @Unique
    private @Nullable MutableMesh mutableMesh;

    ItemStackRenderStateLayerRenderStateMixin() {
    }

    @Override
    public QuadEmitter emitter() {
        if (this.mutableMesh == null) {
            this.mutableMesh = Renderer.get().mutableMesh();
        }
        return this.mutableMesh.emitter();
    }

    @Override
    public @Nullable MutableMesh fabric_getMutableMesh() {
        return this.mutableMesh;
    }

    @Inject(method={"clear()V"}, at={@At(value="RETURN")})
    private void onReturnClear(CallbackInfo ci) {
        if (this.mutableMesh != null) {
            this.mutableMesh.clear();
        }
    }

    @Redirect(method={"submit"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/SubmitNodeCollector;submitItem(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;III[ILjava/util/List;Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;)V"))
    private void submitItemProxy(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, ItemDisplayContext displayContext, int light, int overlay, int outlineColor, int[] tints, List<BakedQuad> quads, ItemStackRenderState.FoilType foilType) {
        if (this.mutableMesh != null && this.mutableMesh.size() > 0) {
            submitNodeCollector.submitItem(poseStack, displayContext, light, overlay, outlineColor, tints, quads, this.mutableMesh, foilType);
        } else {
            submitNodeCollector.submitItem(poseStack, displayContext, light, overlay, outlineColor, tints, quads, foilType);
        }
    }
}

