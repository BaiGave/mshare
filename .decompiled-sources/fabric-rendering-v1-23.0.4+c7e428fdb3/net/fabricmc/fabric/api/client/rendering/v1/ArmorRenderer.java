/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.client.rendering.v1.TransformCopyingModel;
import net.fabricmc.fabric.impl.client.rendering.ArmorRendererRegistryImpl;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface ArmorRenderer {
    public static void register(Factory factory, ItemLike ... items) {
        ArmorRendererRegistryImpl.register(factory, items);
    }

    public static void register(ArmorRenderer renderer, ItemLike ... items) {
        ArmorRendererRegistryImpl.register(renderer, items);
    }

    public static <S, D> void submitTransformCopyingModel(Model<? super S> sourceModel, S sourceModelState, Model<? super D> delegateModel, D delegateModelState, boolean setDelegateAngles, OrderedSubmitNodeCollector nodeCollector, PoseStack poseStack, RenderType renderType, int light, int overlay, int tintedColor, @Nullable TextureAtlasSprite sprite, int outlineColor,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        nodeCollector.submitModel(TransformCopyingModel.create(sourceModel, delegateModel, setDelegateAngles), Pair.of(sourceModelState, delegateModelState), poseStack, renderType, light, overlay, tintedColor, sprite, outlineColor, crumblingOverlay);
    }

    public static <S, D> void submitTransformCopyingModel(Model<? super S> sourceModel, S sourceModelState, Model<? super D> delegateModel, D delegateModelState, boolean setDelegateAngles, OrderedSubmitNodeCollector nodeCollector, PoseStack poseStack, RenderType renderType, int light, int overlay, int outlineColor,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        nodeCollector.submitModel(TransformCopyingModel.create(sourceModel, delegateModel, setDelegateAngles), Pair.of(sourceModelState, delegateModelState), poseStack, renderType, light, overlay, outlineColor, crumblingOverlay);
    }

    public void render(PoseStack var1, SubmitNodeCollector var2, ItemStack var3, HumanoidRenderState var4, EquipmentSlot var5, int var6, HumanoidModel<HumanoidRenderState> var7);

    default public boolean shouldRenderDefaultHeadItem(LivingEntity entity, ItemStack stack) {
        return true;
    }

    @FunctionalInterface
    public static interface Factory {
        public ArmorRenderer createArmorRenderer(EntityRendererProvider.Context var1);
    }
}

