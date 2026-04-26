/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.model;

import com.mojang.blaze3d.platform.Transparency;
import java.util.Arrays;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public final class ModelHelper {
    private static final Direction[] FACES = Arrays.copyOf(Direction.values(), 7);
    public static final int NULL_FACE_ID = 6;

    private ModelHelper() {
    }

    public static int toFaceIndex(@Nullable Direction face) {
        return face == null ? 6 : face.get3DDataValue();
    }

    public static @Nullable Direction faceFromIndex(int faceIndex) {
        return FACES[faceIndex];
    }

    public static Transparency computeTransparency(TextureAtlasSprite sprite, QuadView quad) {
        float minU = Float.MAX_VALUE;
        float minV = Float.MAX_VALUE;
        float maxU = 0.0f;
        float maxV = 0.0f;
        for (int i = 0; i < 4; ++i) {
            float u = quad.u(i);
            float v = quad.v(i);
            if (u < minU) {
                minU = u;
            }
            if (u > maxU) {
                maxU = u;
            }
            if (v < minV) {
                minV = v;
            }
            if (!(v > maxV)) continue;
            maxV = v;
        }
        float width = 1.0f / (sprite.getU1() - sprite.getU0());
        float height = 1.0f / (sprite.getV1() - sprite.getV0());
        minU = (minU - sprite.getU0()) * width;
        minV = (minV - sprite.getV0()) * height;
        maxU = (maxU - sprite.getU0()) * width;
        maxV = (maxV - sprite.getV0()) * height;
        return sprite.contents().computeTransparency(minU, minV, maxU, maxV);
    }

    @BakedQuad.MaterialFlags
    public static int computeMaterialFlags(QuadView quad) {
        @BakedQuad.MaterialFlags int flags = 0;
        if (quad.chunkLayer().translucent()) {
            flags |= 1;
        }
        if (quad.animated() || quad.foilType() != null && quad.foilType() != ItemStackRenderState.FoilType.NONE) {
            flags |= 2;
        }
        return flags;
    }
}

