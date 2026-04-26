/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@Environment(value=EnvType.CLIENT)
public interface Renderable {
    public void extractRenderState(GuiGraphicsExtractor var1, int var2, int var3, float var4);
}

