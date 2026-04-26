/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.rendering;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.Optional;
import java.util.function.Supplier;

public final class FabricRenderPipelineInternals {
    private static final ThreadLocal<Optional<Boolean>> SCOPED_SNIPPET_USE_PIPELINE_VERTEX_FORMAT_FOR_GUI = ThreadLocal.withInitial(Optional::empty);

    private FabricRenderPipelineInternals() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static RenderPipeline.Snippet withSnippetUsePipelineVertexFormatForGui(Supplier<RenderPipeline.Snippet> factory, Optional<Boolean> usePipelineVertexFormat) {
        Optional<Boolean> original = SCOPED_SNIPPET_USE_PIPELINE_VERTEX_FORMAT_FOR_GUI.get();
        try {
            SCOPED_SNIPPET_USE_PIPELINE_VERTEX_FORMAT_FOR_GUI.set(usePipelineVertexFormat);
            RenderPipeline.Snippet snippet = factory.get();
            return snippet;
        }
        finally {
            if (original.isEmpty()) {
                SCOPED_SNIPPET_USE_PIPELINE_VERTEX_FORMAT_FOR_GUI.remove();
            } else {
                SCOPED_SNIPPET_USE_PIPELINE_VERTEX_FORMAT_FOR_GUI.set(original);
            }
        }
    }

    public static Optional<Boolean> getScopedUsePipelineVertexFormatForGui() {
        return SCOPED_SNIPPET_USE_PIPELINE_VERTEX_FORMAT_FOR_GUI.get();
    }
}

