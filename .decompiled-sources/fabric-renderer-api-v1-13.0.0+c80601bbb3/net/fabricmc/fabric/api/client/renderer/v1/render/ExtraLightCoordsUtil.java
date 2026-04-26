/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.render;

import net.minecraft.util.LightCoordsUtil;

public final class ExtraLightCoordsUtil {
    private ExtraLightCoordsUtil() {
    }

    public static int smoothMax(int coords1, int coords2) {
        int block1 = LightCoordsUtil.smoothBlock(coords1);
        int block2 = LightCoordsUtil.smoothBlock(coords2);
        int sky1 = LightCoordsUtil.smoothSky(coords1);
        int sky2 = LightCoordsUtil.smoothSky(coords2);
        return LightCoordsUtil.smoothPack(Math.max(block1, block2), Math.max(sky1, sky2));
    }
}

