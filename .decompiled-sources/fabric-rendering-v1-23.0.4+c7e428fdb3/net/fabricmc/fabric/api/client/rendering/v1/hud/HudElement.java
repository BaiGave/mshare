/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface HudElement {
    public void extractRenderState(GuiGraphicsExtractor var1, DeltaTracker var2);
}

