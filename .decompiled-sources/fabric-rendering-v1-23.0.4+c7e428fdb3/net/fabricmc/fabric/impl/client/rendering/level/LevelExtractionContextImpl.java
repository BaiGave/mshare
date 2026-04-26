/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.rendering.level;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.jspecify.annotations.Nullable;

public class LevelExtractionContextImpl
implements LevelExtractionContext {
    private GameRenderer gameRenderer;
    private LevelRenderer levelRenderer;
    private LevelRenderState levelRenderState;
    private ClientLevel level;
    private Camera camera;
    private @Nullable DeltaTracker deltaTracker;

    public void prepare(GameRenderer gameRenderer, LevelRenderer levelRenderer, LevelRenderState levelRenderState, ClientLevel level, DeltaTracker deltaTracker, Camera camera) {
        this.gameRenderer = gameRenderer;
        this.levelRenderer = levelRenderer;
        this.levelRenderState = levelRenderState;
        this.level = level;
        this.deltaTracker = deltaTracker;
        this.camera = camera;
    }

    @Override
    public GameRenderer gameRenderer() {
        return this.gameRenderer;
    }

    @Override
    public LevelRenderer levelRenderer() {
        return this.levelRenderer;
    }

    @Override
    public LevelRenderState levelState() {
        return this.levelRenderState;
    }

    @Override
    public ClientLevel level() {
        return this.level;
    }

    @Override
    public Camera camera() {
        return this.camera;
    }

    @Override
    public DeltaTracker deltaTracker() {
        return this.deltaTracker;
    }
}

