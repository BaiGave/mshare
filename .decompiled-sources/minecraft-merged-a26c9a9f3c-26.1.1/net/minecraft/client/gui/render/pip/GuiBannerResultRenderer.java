/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.render.pip;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.pip.GuiBannerResultRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;

@Environment(value=EnvType.CLIENT)
public class GuiBannerResultRenderer
extends PictureInPictureRenderer<GuiBannerResultRenderState> {
    private final SpriteGetter sprites;

    public GuiBannerResultRenderer(MultiBufferSource.BufferSource bufferSource, SpriteGetter sprites) {
        super(bufferSource);
        this.sprites = sprites;
    }

    @Override
    public Class<GuiBannerResultRenderState> getRenderStateClass() {
        return GuiBannerResultRenderState.class;
    }

    @Override
    protected void renderToTexture(GuiBannerResultRenderState renderState, PoseStack poseStack) {
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_FLAT);
        poseStack.translate(0.0f, 0.25f, 0.0f);
        FeatureRenderDispatcher featureRenderDispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
        SubmitNodeStorage submitNodeStorage = featureRenderDispatcher.getSubmitNodeStorage();
        submitNodeStorage.submitModel(renderState.flag(), Float.valueOf(0.0f), poseStack, 0xF000F0, OverlayTexture.NO_OVERLAY, -1, Sheets.BANNER_BASE, this.sprites, 0, null);
        BannerRenderer.submitPatterns(this.sprites, poseStack, submitNodeStorage, 0xF000F0, OverlayTexture.NO_OVERLAY, renderState.flag(), Float.valueOf(0.0f), true, renderState.baseColor(), renderState.resultBannerPatterns(), null);
        featureRenderDispatcher.renderAllFeatures();
    }

    @Override
    protected String getTextureLabel() {
        return "banner result";
    }
}

