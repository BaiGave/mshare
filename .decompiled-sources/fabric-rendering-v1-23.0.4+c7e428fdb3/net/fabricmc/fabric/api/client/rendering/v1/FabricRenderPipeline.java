/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.Optional;

public interface FabricRenderPipeline {
    default public boolean usePipelineDrawModeForGui() {
        throw new AssertionError((Object)"Implemented in Mixin");
    }

    public static interface Snippet {
        default public Optional<Boolean> usePipelineDrawModeForGui() {
            throw new AssertionError((Object)"Implemented in Mixin");
        }

        public static RenderPipeline.Snippet withPipelineDrawModeForGui(RenderPipeline.Snippet base, boolean usePipelineDrawMode) {
            return RenderPipeline.builder(base).withUsePipelineDrawModeForGui(usePipelineDrawMode).buildSnippet();
        }

        public static RenderPipeline.Snippet withoutPipelineDrawModeForGui(RenderPipeline.Snippet base) {
            return RenderPipeline.builder(base).withoutUsePipelineDrawModeForGui().buildSnippet();
        }
    }

    public static interface Builder {
        default public RenderPipeline.Builder withUsePipelineDrawModeForGui(boolean usePipelineDrawMode) {
            throw new AssertionError((Object)"Implemented in Mixin");
        }

        default public RenderPipeline.Builder withoutUsePipelineDrawModeForGui() {
            throw new AssertionError((Object)"Implemented in Mixin");
        }
    }
}

