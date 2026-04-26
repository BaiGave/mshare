/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.render.pip;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.gui.pip.GuiSkinRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix4fStack;

@Environment(value=EnvType.CLIENT)
public class GuiSkinRenderer
extends PictureInPictureRenderer<GuiSkinRenderState> {
    public GuiSkinRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<GuiSkinRenderState> getRenderStateClass() {
        return GuiSkinRenderState.class;
    }

    @Override
    protected void renderToTexture(GuiSkinRenderState skinState, PoseStack modelStack) {
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.PLAYER_SKIN);
        int guiScale = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.guiScale;
        Matrix4fStack viewStack = RenderSystem.getModelViewStack();
        viewStack.pushMatrix();
        float scale = skinState.scale() * (float)guiScale;
        viewStack.rotateAround(Axis.XP.rotationDegrees(skinState.rotationX()), 0.0f, scale * -skinState.pivotY(), 0.0f);
        modelStack.mulPose(Axis.YP.rotationDegrees(-skinState.rotationY()));
        modelStack.translate(0.0f, -1.6010001f, 0.0f);
        RenderType skinRenderType = skinState.playerModel().renderType(skinState.texture());
        skinState.playerModel().renderToBuffer(modelStack, this.bufferSource.getBuffer(skinRenderType), 0xF000F0, OverlayTexture.NO_OVERLAY);
        this.bufferSource.endBatch();
        viewStack.popMatrix();
    }

    @Override
    protected String getTextureLabel() {
        return "player skin";
    }
}

