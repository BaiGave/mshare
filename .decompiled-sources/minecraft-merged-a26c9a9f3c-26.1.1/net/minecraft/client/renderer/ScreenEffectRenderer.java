/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ScreenEffectRenderer {
    private static final Identifier UNDERWATER_LOCATION = Identifier.withDefaultNamespace("textures/misc/underwater.png");
    private final Minecraft minecraft;
    private final SpriteGetter sprites;
    private final MultiBufferSource bufferSource;
    public static final int ITEM_ACTIVATION_ANIMATION_LENGTH = 40;
    private @Nullable ItemStack itemActivationItem;
    private int itemActivationTicks;
    private float itemActivationOffX;
    private float itemActivationOffY;

    public ScreenEffectRenderer(Minecraft minecraft, SpriteGetter sprites, MultiBufferSource bufferSource) {
        this.minecraft = minecraft;
        this.sprites = sprites;
        this.bufferSource = bufferSource;
    }

    public void tick() {
        if (this.itemActivationTicks > 0) {
            --this.itemActivationTicks;
            if (this.itemActivationTicks == 0) {
                this.itemActivationItem = null;
            }
        }
    }

    public void renderScreenEffect(boolean isFirstPerson, boolean isSleeping, float partialTicks, SubmitNodeCollector submitNodeCollector, boolean hideGui) {
        PoseStack poseStack = new PoseStack();
        LocalPlayer player = this.minecraft.player;
        if (isFirstPerson && !isSleeping) {
            BlockState blockState;
            if (!player.noPhysics && (blockState = ScreenEffectRenderer.getViewBlockingState(player)) != null) {
                ScreenEffectRenderer.renderTex(this.minecraft.getModelManager().getBlockStateModelSet().getParticleMaterial(blockState).sprite(), poseStack, this.bufferSource);
            }
            if (!this.minecraft.player.isSpectator()) {
                if (this.minecraft.player.isEyeInFluid(FluidTags.WATER)) {
                    ScreenEffectRenderer.renderWater(this.minecraft, poseStack, this.bufferSource);
                }
                if (this.minecraft.player.isOnFire()) {
                    TextureAtlasSprite fireSprite = this.sprites.get(ModelBakery.FIRE_1);
                    ScreenEffectRenderer.renderFire(poseStack, this.bufferSource, fireSprite);
                }
            }
        }
        if (!hideGui) {
            this.renderItemActivationAnimation(poseStack, partialTicks, submitNodeCollector);
        }
    }

    private void renderItemActivationAnimation(PoseStack poseStack, float partialTicks, SubmitNodeCollector submitNodeCollector) {
        if (this.itemActivationItem == null || this.itemActivationTicks <= 0) {
            return;
        }
        int tick = 40 - this.itemActivationTicks;
        float scale = ((float)tick + partialTicks) / 40.0f;
        float ts = scale * scale;
        float tc = scale * ts;
        float smoothScale = 10.25f * tc * ts - 24.95f * ts * ts + 25.5f * tc - 13.8f * ts + 4.0f * scale;
        float piScale = smoothScale * (float)Math.PI;
        WindowRenderState windowState = this.minecraft.gameRenderer.getGameRenderState().windowRenderState;
        float aspectRatio = (float)windowState.width / (float)windowState.height;
        float offX = this.itemActivationOffX * 0.3f * aspectRatio;
        float offY = this.itemActivationOffY * 0.3f;
        poseStack.pushPose();
        poseStack.translate(offX * Mth.abs(Mth.sin(piScale * 2.0f)), offY * Mth.abs(Mth.sin(piScale * 2.0f)), -10.0f + 9.0f * Mth.sin(piScale));
        float size = 0.8f;
        poseStack.scale(0.8f, 0.8f, 0.8f);
        poseStack.mulPose(Axis.YP.rotationDegrees(900.0f * Mth.abs(Mth.sin(piScale))));
        poseStack.mulPose(Axis.XP.rotationDegrees(6.0f * Mth.cos(scale * 8.0f)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(6.0f * Mth.cos(scale * 8.0f)));
        this.minecraft.gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
        ItemStackRenderState itemState = new ItemStackRenderState();
        this.minecraft.getItemModelResolver().updateForTopItem(itemState, this.itemActivationItem, ItemDisplayContext.FIXED, this.minecraft.level, null, 0);
        itemState.submit(poseStack, submitNodeCollector, 0xF000F0, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    public void resetItemActivation() {
        this.itemActivationItem = null;
    }

    public void displayItemActivation(ItemStack itemStack, RandomSource random) {
        this.itemActivationItem = itemStack;
        this.itemActivationTicks = 40;
        this.itemActivationOffX = random.nextFloat() * 2.0f - 1.0f;
        this.itemActivationOffY = random.nextFloat() * 2.0f - 1.0f;
    }

    private static @Nullable BlockState getViewBlockingState(Player player) {
        BlockPos.MutableBlockPos testPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 8; ++i) {
            double xo = player.getX() + (double)(((float)((i >> 0) % 2) - 0.5f) * player.getBbWidth() * 0.8f);
            double yo = player.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5f) * 0.1f * player.getScale());
            double zo = player.getZ() + (double)(((float)((i >> 2) % 2) - 0.5f) * player.getBbWidth() * 0.8f);
            testPos.set(xo, yo, zo);
            BlockState blockState = player.level().getBlockState(testPos);
            if (blockState.getRenderShape() == RenderShape.INVISIBLE || !blockState.isViewBlocking(player.level(), testPos)) continue;
            return blockState;
        }
        return null;
    }

    private static void renderTex(TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource bufferSource) {
        float br = 0.1f;
        int color = ARGB.colorFromFloat(1.0f, 0.1f, 0.1f, 0.1f);
        float x0 = -1.0f;
        float x1 = 1.0f;
        float y0 = -1.0f;
        float y1 = 1.0f;
        float z0 = -0.5f;
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        Matrix4f pose = poseStack.last().pose();
        VertexConsumer builder = bufferSource.getBuffer(RenderTypes.blockScreenEffect(sprite.atlasLocation()));
        builder.addVertex(pose, -1.0f, -1.0f, -0.5f).setUv(u1, v1).setColor(color);
        builder.addVertex(pose, 1.0f, -1.0f, -0.5f).setUv(u0, v1).setColor(color);
        builder.addVertex(pose, 1.0f, 1.0f, -0.5f).setUv(u0, v0).setColor(color);
        builder.addVertex(pose, -1.0f, 1.0f, -0.5f).setUv(u1, v0).setColor(color);
    }

    private static void renderWater(Minecraft minecraft, PoseStack poseStack, MultiBufferSource bufferSource) {
        BlockPos pos = BlockPos.containing(minecraft.player.getX(), minecraft.player.getEyeY(), minecraft.player.getZ());
        float br = Lightmap.getBrightness(minecraft.player.level().dimensionType(), minecraft.player.level().getMaxLocalRawBrightness(pos));
        int color = ARGB.colorFromFloat(0.1f, br, br, br);
        float size = 4.0f;
        float x0 = -1.0f;
        float x1 = 1.0f;
        float y0 = -1.0f;
        float y1 = 1.0f;
        float z0 = -0.5f;
        float uo = -minecraft.player.getYRot() / 64.0f;
        float vo = minecraft.player.getXRot() / 64.0f;
        Matrix4f pose = poseStack.last().pose();
        VertexConsumer builder = bufferSource.getBuffer(RenderTypes.blockScreenEffect(UNDERWATER_LOCATION));
        builder.addVertex(pose, -1.0f, -1.0f, -0.5f).setUv(4.0f + uo, 4.0f + vo).setColor(color);
        builder.addVertex(pose, 1.0f, -1.0f, -0.5f).setUv(0.0f + uo, 4.0f + vo).setColor(color);
        builder.addVertex(pose, 1.0f, 1.0f, -0.5f).setUv(0.0f + uo, 0.0f + vo).setColor(color);
        builder.addVertex(pose, -1.0f, 1.0f, -0.5f).setUv(4.0f + uo, 0.0f + vo).setColor(color);
    }

    private static void renderFire(PoseStack poseStack, MultiBufferSource bufferSource, TextureAtlasSprite sprite) {
        VertexConsumer builder = bufferSource.getBuffer(RenderTypes.fireScreenEffect(sprite.atlasLocation()));
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        float size = 1.0f;
        for (int i = 0; i < 2; ++i) {
            poseStack.pushPose();
            float x0 = -0.5f;
            float x1 = 0.5f;
            float y0 = -0.5f;
            float y1 = 0.5f;
            float z0 = -0.5f;
            poseStack.translate((float)(-(i * 2 - 1)) * 0.24f, -0.3f, 0.0f);
            poseStack.mulPose(Axis.YP.rotationDegrees((float)(i * 2 - 1) * 10.0f));
            Matrix4f pose = poseStack.last().pose();
            builder.addVertex(pose, -0.5f, -0.5f, -0.5f).setUv(u1, v1).setColor(1.0f, 1.0f, 1.0f, 0.9f);
            builder.addVertex(pose, 0.5f, -0.5f, -0.5f).setUv(u0, v1).setColor(1.0f, 1.0f, 1.0f, 0.9f);
            builder.addVertex(pose, 0.5f, 0.5f, -0.5f).setUv(u0, v0).setColor(1.0f, 1.0f, 1.0f, 0.9f);
            builder.addVertex(pose, -0.5f, 0.5f, -0.5f).setUv(u1, v0).setColor(1.0f, 1.0f, 1.0f, 0.9f);
            poseStack.popPose();
        }
    }
}

