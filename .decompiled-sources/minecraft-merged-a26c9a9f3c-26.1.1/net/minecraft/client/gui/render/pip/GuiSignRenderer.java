/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.render.pip;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.state.gui.pip.GuiSignRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;

@Environment(value=EnvType.CLIENT)
public class GuiSignRenderer
extends PictureInPictureRenderer<GuiSignRenderState> {
    private final SpriteGetter sprites;

    public GuiSignRenderer(MultiBufferSource.BufferSource bufferSource, SpriteGetter sprites) {
        super(bufferSource);
        this.sprites = sprites;
    }

    @Override
    public Class<GuiSignRenderState> getRenderStateClass() {
        return GuiSignRenderState.class;
    }

    @Override
    protected void renderToTexture(GuiSignRenderState renderState, PoseStack poseStack) {
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_FLAT);
        poseStack.translate(0.0f, -0.75f, 0.0f);
        SpriteId sprite = Sheets.getSignSprite(renderState.woodType());
        Model.Simple model = renderState.signModel();
        VertexConsumer buffer = sprite.buffer(this.sprites, this.bufferSource, model.renderType());
        model.renderToBuffer(poseStack, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY);
    }

    @Override
    protected String getTextureLabel() {
        return "sign";
    }
}

