/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.state.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface ScreenArea {
    public @Nullable ScreenRectangle bounds();
}

