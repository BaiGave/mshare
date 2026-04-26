/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;

@Environment(value=EnvType.CLIENT)
public class GameRenderState
implements FabricRenderState {
    public final LevelRenderState levelRenderState = new LevelRenderState();
    public final LightmapRenderState lightmapRenderState = new LightmapRenderState();
    public final GuiRenderState guiRenderState = new GuiRenderState();
    public final OptionsRenderState optionsRenderState = new OptionsRenderState();
    public final WindowRenderState windowRenderState = new WindowRenderState();
    public int framerateLimit;
}

