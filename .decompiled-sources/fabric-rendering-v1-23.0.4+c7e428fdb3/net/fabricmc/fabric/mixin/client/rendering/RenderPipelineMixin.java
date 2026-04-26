/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.fabric.impl.client.rendering.FabricRenderPipelineImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={RenderPipeline.class})
class RenderPipelineMixin
implements FabricRenderPipelineImpl {
    @Unique
    private boolean usePipelineDrawModeForGui = false;

    RenderPipelineMixin() {
    }

    @Override
    public boolean usePipelineDrawModeForGui() {
        return this.usePipelineDrawModeForGui;
    }

    @Override
    public void fabric$setUsePipelineDrawModeForGuiSetter(boolean usePipelineDrawModeForGui) {
        this.usePipelineDrawModeForGui = usePipelineDrawModeForGui;
    }
}

