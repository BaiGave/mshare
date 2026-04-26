/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.render.pip;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.gui.pip.GuiBookModelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class GuiBookModelRenderer
extends PictureInPictureRenderer<GuiBookModelRenderState> {
    public GuiBookModelRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<GuiBookModelRenderState> getRenderStateClass() {
        return GuiBookModelRenderState.class;
    }

    @Override
    protected void renderToTexture(GuiBookModelRenderState bookModelState, PoseStack poseStack) {
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(25.0f));
        float open = bookModelState.open();
        poseStack.translate((1.0f - open) * 0.2f, (1.0f - open) * 0.1f, (1.0f - open) * 0.25f);
        poseStack.mulPose(Axis.YP.rotationDegrees(-(1.0f - open) * 90.0f - 90.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        float flip = bookModelState.flip();
        float pageFlip1 = Mth.clamp(Mth.frac(flip + 0.25f) * 1.6f - 0.3f, 0.0f, 1.0f);
        float pageFlip2 = Mth.clamp(Mth.frac(flip + 0.75f) * 1.6f - 0.3f, 0.0f, 1.0f);
        BookModel bookModel = bookModelState.bookModel();
        bookModel.setupAnim(BookModel.State.forAnimation(0.0f, pageFlip1, pageFlip2, open));
        Identifier texture = bookModelState.texture();
        VertexConsumer buffer = this.bufferSource.getBuffer(bookModel.renderType(texture));
        bookModel.renderToBuffer(poseStack, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY);
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return 17 * guiScale;
    }

    @Override
    protected String getTextureLabel() {
        return "book model";
    }
}

