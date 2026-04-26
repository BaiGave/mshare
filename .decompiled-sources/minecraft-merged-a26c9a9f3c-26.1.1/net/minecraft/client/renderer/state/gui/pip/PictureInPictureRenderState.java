/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.state.gui.pip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.ScreenArea;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface PictureInPictureRenderState
extends ScreenArea {
    public static final Matrix3x2f IDENTITY_POSE = new Matrix3x2f();

    public int x0();

    public int x1();

    public int y0();

    public int y1();

    public float scale();

    default public Matrix3x2f pose() {
        return IDENTITY_POSE;
    }

    public @Nullable ScreenRectangle scissorArea();

    public static @Nullable ScreenRectangle getBounds(int x0, int y0, int x1, int y1, @Nullable ScreenRectangle scissorArea) {
        ScreenRectangle bounds = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0);
        return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
    }
}

