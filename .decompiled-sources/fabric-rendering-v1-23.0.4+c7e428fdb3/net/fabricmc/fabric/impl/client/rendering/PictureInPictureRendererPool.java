/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.rendering;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.impl.client.rendering.PictureInPictureRendererRegistryImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;

public final class PictureInPictureRendererPool<T extends PictureInPictureRenderState>
implements AutoCloseable {
    private int index = 0;
    private final List<PictureInPictureRenderer<T>> renderers = new ArrayList<PictureInPictureRenderer<T>>();

    public void newFrame() {
        this.index = 0;
    }

    public PictureInPictureRenderer<T> substitute(PictureInPictureRenderer<T> original, T elementState, Minecraft client, MultiBufferSource.BufferSource immediate, SubmitNodeCollector submitNodeCollector) {
        int index;
        if ((index = this.index++) == 0) {
            return original;
        }
        if (index <= this.renderers.size()) {
            return this.renderers.get(index - 1);
        }
        PictureInPictureRenderer<T> newRenderer = PictureInPictureRendererRegistryImpl.createNewRenderer(elementState, client, immediate, submitNodeCollector);
        if (newRenderer == null) {
            return original;
        }
        this.renderers.add(newRenderer);
        return newRenderer;
    }

    public void cleanUpUnusedRenderers() {
        int firstUnusedIndex = Math.max(0, this.index - 1);
        if (firstUnusedIndex >= this.renderers.size()) {
            return;
        }
        for (int i = firstUnusedIndex; i < this.renderers.size(); ++i) {
            this.renderers.get(i).close();
        }
        this.renderers.subList(firstUnusedIndex, this.renderers.size()).clear();
    }

    @Override
    public void close() {
        this.renderers.forEach(PictureInPictureRenderer::close);
        this.index = 0;
        this.renderers.clear();
    }
}

