/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.feature;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

@Environment(value=EnvType.CLIENT)
public class LeashFeatureRenderer {
    private static final int LEASH_RENDER_STEPS = 24;
    private static final float LEASH_WIDTH = 0.05f;

    public void renderSolid(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource) {
        for (SubmitNodeStorage.LeashSubmit leashSubmit : nodeCollection.getLeashSubmits()) {
            LeashFeatureRenderer.renderLeash(leashSubmit.pose(), bufferSource, leashSubmit.leashState());
        }
    }

    private static void renderLeash(Matrix4f pose, MultiBufferSource bufferSource, EntityRenderState.LeashState leashState) {
        int k;
        float dx = (float)(leashState.end.x - leashState.start.x);
        float dy = (float)(leashState.end.y - leashState.start.y);
        float dz = (float)(leashState.end.z - leashState.start.z);
        float offsetFactor = Mth.invSqrt(dx * dx + dz * dz) * 0.05f / 2.0f;
        float dxOff = dz * offsetFactor;
        float dzOff = dx * offsetFactor;
        pose.translate((float)leashState.offset.x, (float)leashState.offset.y, (float)leashState.offset.z);
        VertexConsumer builder = bufferSource.getBuffer(RenderTypes.leash());
        for (k = 0; k <= 24; ++k) {
            LeashFeatureRenderer.addVertexPair(builder, pose, dx, dy, dz, 0.05f, dxOff, dzOff, k, false, leashState);
        }
        for (k = 24; k >= 0; --k) {
            LeashFeatureRenderer.addVertexPair(builder, pose, dx, dy, dz, 0.0f, dxOff, dzOff, k, true, leashState);
        }
    }

    private static void addVertexPair(VertexConsumer builder, Matrix4fc pose, float dx, float dy, float dz, float fudge, float dxOff, float dzOff, int k, boolean backwards, EntityRenderState.LeashState state) {
        float progress = (float)k / 24.0f;
        int block = (int)Mth.lerp(progress, state.startBlockLight, state.endBlockLight);
        int sky = (int)Mth.lerp(progress, state.startSkyLight, state.endSkyLight);
        int lightCoords = LightCoordsUtil.pack(block, sky);
        float colorModifier = k % 2 == (backwards ? 1 : 0) ? 0.7f : 1.0f;
        float r = 0.5f * colorModifier;
        float g = 0.4f * colorModifier;
        float b = 0.3f * colorModifier;
        float x = dx * progress;
        float y = state.slack ? (dy > 0.0f ? dy * progress * progress : dy - dy * (1.0f - progress) * (1.0f - progress)) : dy * progress;
        float z = dz * progress;
        builder.addVertex(pose, x - dxOff, y + fudge, z + dzOff).setColor(r, g, b, 1.0f).setLight(lightCoords);
        builder.addVertex(pose, x + dxOff, y + 0.05f - fudge, z - dzOff).setColor(r, g, b, 1.0f).setLight(lightCoords);
    }
}

