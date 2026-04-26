/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1.level;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface AbstractLevelRenderContext {
    public GameRenderer gameRenderer();

    public LevelRenderer levelRenderer();

    public LevelRenderState levelState();
}

