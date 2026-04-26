/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.rendering.level;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.AbstractLevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelTerrainRenderContext;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.jspecify.annotations.Nullable;

public final class LevelRenderContextImpl
implements AbstractLevelRenderContext,
LevelTerrainRenderContext,
LevelRenderContext {
    private GameRenderer gameRenderer;
    private LevelRenderer levelRenderer;
    private LevelRenderState levelRenderState;
    private ChunkSectionsToRender sectionsToRender;
    private SubmitNodeCollector nodeCollector;
    private @Nullable PoseStack poseStack;
    private MultiBufferSource.BufferSource bufferSource;

    public void prepare(GameRenderer gameRenderer, LevelRenderer levelRenderer, LevelRenderState levelRenderState, ChunkSectionsToRender sectionsToRender, SubmitNodeCollector nodeCollector, MultiBufferSource.BufferSource bufferSource) {
        this.gameRenderer = gameRenderer;
        this.levelRenderer = levelRenderer;
        this.levelRenderState = levelRenderState;
        this.sectionsToRender = sectionsToRender;
        this.nodeCollector = nodeCollector;
        this.bufferSource = bufferSource;
        this.poseStack = null;
    }

    public void setPoseStack(@Nullable PoseStack poseStack) {
        this.poseStack = poseStack;
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
    public ChunkSectionsToRender sectionsToRender() {
        return this.sectionsToRender;
    }

    @Override
    public SubmitNodeCollector submitNodeCollector() {
        return this.nodeCollector;
    }

    @Override
    public @Nullable PoseStack poseStack() {
        return this.poseStack;
    }

    @Override
    public MultiBufferSource.BufferSource bufferSource() {
        return this.bufferSource;
    }
}

