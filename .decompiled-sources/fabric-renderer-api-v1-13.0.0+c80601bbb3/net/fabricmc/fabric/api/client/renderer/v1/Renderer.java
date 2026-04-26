/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1;

import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.AltModelBlockRenderer;
import net.fabricmc.fabric.impl.client.renderer.RendererManager;
import net.minecraft.client.color.block.BlockColors;

public interface Renderer {
    public static Renderer get() {
        return RendererManager.getRenderer();
    }

    public static void register(Renderer renderer) {
        RendererManager.registerRenderer(renderer);
    }

    public QuadEmitter quadEmitter(Consumer<? super MutableQuadView> var1);

    public MutableMesh mutableMesh();

    public AltModelBlockRenderer altModelBlockRenderer(boolean var1, boolean var2, BlockColors var3);
}

