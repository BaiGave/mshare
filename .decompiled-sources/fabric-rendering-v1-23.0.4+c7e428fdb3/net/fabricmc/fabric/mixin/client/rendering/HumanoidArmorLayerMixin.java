/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.impl.client.rendering.ArmorRendererRegistryImpl;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={HumanoidArmorLayer.class})
public abstract class HumanoidArmorLayerMixin<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>>
extends RenderLayer<S, M> {
    @Unique
    private HumanoidRenderState humanoidRenderState;

    public HumanoidArmorLayerMixin(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Inject(method={"submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V"}, at={@At(value="HEAD")})
    private void render(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S bipedEntityRenderState, float f, float g, CallbackInfo ci) {
        this.humanoidRenderState = bipedEntityRenderState;
    }

    @Inject(method={"renderArmorPiece"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderArmor(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, ItemStack stack, EquipmentSlot armorSlot, int light, S bipedEntityRenderState, CallbackInfo ci) {
        ArmorRenderer renderer = ArmorRendererRegistryImpl.get(stack.getItem());
        if (renderer != null) {
            renderer.render(poseStack, submitNodeCollector, stack, this.humanoidRenderState, armorSlot, light, (HumanoidModel)this.getParentModel());
            ci.cancel();
        }
    }
}

