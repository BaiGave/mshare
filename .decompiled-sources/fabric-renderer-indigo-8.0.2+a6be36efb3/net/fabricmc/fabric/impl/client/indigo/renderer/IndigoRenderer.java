/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.indigo.renderer;

import java.util.Objects;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.AltModelBlockRenderer;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableMeshImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AltModelBlockRendererImpl;
import net.minecraft.client.color.block.BlockColors;

public class IndigoRenderer
implements Renderer {
    public static final IndigoRenderer INSTANCE = new IndigoRenderer();

    private IndigoRenderer() {
    }

    @Override
    public QuadEmitter quadEmitter(final Consumer<? super MutableQuadView> consumer) {
        return new MutableQuadViewImpl(this){
            {
                Objects.requireNonNull(this$0);
                this.data = new int[EncodingFormat.TOTAL_STRIDE];
                this.clear();
            }

            @Override
            protected void emitDirectly() {
                consumer.accept(this);
            }
        };
    }

    @Override
    public MutableMesh mutableMesh() {
        return new MutableMeshImpl();
    }

    @Override
    public AltModelBlockRenderer altModelBlockRenderer(boolean ambientOcclusion, boolean cull, BlockColors blockColors) {
        return new AltModelBlockRendererImpl(ambientOcclusion, cull, blockColors);
    }
}

