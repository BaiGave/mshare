/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractSignRenderer<S extends SignRenderState>
implements BlockEntityRenderer<SignBlockEntity, S> {
    private static final int BLACK_TEXT_OUTLINE_COLOR = -988212;
    private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);
    private final Font font;
    private final SpriteGetter sprites;

    public AbstractSignRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
        this.sprites = context.sprites();
    }

    protected abstract Model.Simple getSignModel(S var1);

    protected abstract SpriteId getSignSprite(WoodType var1);

    @Override
    public void submit(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        this.submitSignWithText(state, poseStack, ((SignRenderState)state).breakProgress, submitNodeCollector);
    }

    private void submitSignWithText(S state, PoseStack poseStack, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress, SubmitNodeCollector submitNodeCollector) {
        Model.Simple bodyModel = this.getSignModel(state);
        poseStack.pushPose();
        poseStack.mulPose(((SignRenderState)state).transformations.body());
        this.submitSign(poseStack, ((SignRenderState)state).lightCoords, ((SignRenderState)state).woodType, bodyModel, breakProgress, submitNodeCollector);
        poseStack.popPose();
        if (((SignRenderState)state).frontText != null) {
            poseStack.pushPose();
            poseStack.mulPose(((SignRenderState)state).transformations.frontText());
            this.submitSignText(state, poseStack, submitNodeCollector, ((SignRenderState)state).frontText);
            poseStack.popPose();
        }
        if (((SignRenderState)state).backText != null) {
            poseStack.pushPose();
            poseStack.mulPose(((SignRenderState)state).transformations.backText());
            this.submitSignText(state, poseStack, submitNodeCollector, ((SignRenderState)state).backText);
            poseStack.popPose();
        }
    }

    protected void submitSign(PoseStack poseStack, int lightCoords, WoodType type, Model.Simple signModel, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress, SubmitNodeCollector submitNodeCollector) {
        SpriteId sprite = this.getSignSprite(type);
        submitNodeCollector.submitModel(signModel, Unit.INSTANCE, poseStack, lightCoords, OverlayTexture.NO_OVERLAY, -1, sprite, this.sprites, 0, breakProgress);
    }

    private void submitSignText(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, SignText signText) {
        int lightVal;
        boolean drawOutline;
        int textColor;
        int darkColor = AbstractSignRenderer.getDarkColor(signText);
        int signMidpoint = 4 * ((SignRenderState)state).textLineHeight / 2;
        FormattedCharSequence[] formattedLines = signText.getRenderMessages(((SignRenderState)state).isTextFilteringEnabled, input -> {
            List<FormattedCharSequence> components = this.font.split((FormattedText)input, state.maxTextLineWidth);
            return components.isEmpty() ? FormattedCharSequence.EMPTY : components.get(0);
        });
        if (signText.hasGlowingText()) {
            textColor = signText.getColor().getTextColor();
            drawOutline = textColor == DyeColor.BLACK.getTextColor() || ((SignRenderState)state).drawOutline;
            lightVal = 0xF000F0;
        } else {
            textColor = darkColor;
            drawOutline = false;
            lightVal = ((SignRenderState)state).lightCoords;
        }
        for (int i = 0; i < 4; ++i) {
            FormattedCharSequence actualLine = formattedLines[i];
            float x1 = -this.font.width(actualLine) / 2;
            submitNodeCollector.submitText(poseStack, x1, i * ((SignRenderState)state).textLineHeight - signMidpoint, actualLine, false, Font.DisplayMode.POLYGON_OFFSET, lightVal, textColor, 0, drawOutline ? darkColor : 0);
        }
    }

    private static boolean isOutlineVisible(BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player != null && minecraft.options.getCameraType().isFirstPerson() && player.isScoping()) {
            return true;
        }
        Entity camera = minecraft.getCameraEntity();
        return camera != null && camera.distanceToSqr(Vec3.atCenterOf(pos)) < (double)OUTLINE_RENDER_DISTANCE;
    }

    public static int getDarkColor(SignText signText) {
        int color = signText.getColor().getTextColor();
        if (color == DyeColor.BLACK.getTextColor() && signText.hasGlowingText()) {
            return -988212;
        }
        return ARGB.scaleRGB(color, 0.4f);
    }

    @Override
    public void extractRenderState(SignBlockEntity blockEntity, S state, float partialTicks, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        ((SignRenderState)state).maxTextLineWidth = blockEntity.getMaxTextLineWidth();
        ((SignRenderState)state).textLineHeight = blockEntity.getTextLineHeight();
        ((SignRenderState)state).frontText = blockEntity.getFrontText();
        ((SignRenderState)state).backText = blockEntity.getBackText();
        ((SignRenderState)state).isTextFilteringEnabled = Minecraft.getInstance().isTextFilteringEnabled();
        ((SignRenderState)state).drawOutline = AbstractSignRenderer.isOutlineVisible(blockEntity.getBlockPos());
        ((SignRenderState)state).woodType = SignBlock.getWoodType(blockEntity.getBlockState().getBlock());
    }
}

