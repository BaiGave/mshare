/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.Optional;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderPipeline;
import net.fabricmc.fabric.impl.client.rendering.FabricRenderPipelineInternals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={RenderPipeline.Snippet.class})
class RenderPipelineSnippetMixin
implements FabricRenderPipeline.Snippet {
    @Unique
    private final Optional<Boolean> usePipelineDrawModeForGui = FabricRenderPipelineInternals.getScopedUsePipelineVertexFormatForGui();

    private RenderPipelineSnippetMixin() {
    }

    @Override
    public Optional<Boolean> usePipelineDrawModeForGui() {
        return this.usePipelineDrawModeForGui;
    }

    @ModifyReturnValue(method={"toString"}, at={@At(value="RETURN")})
    private String modifyToStringToIncludeFabricExtraData(String original) {
        return original.substring(0, original.length() - 1) + ", usePipelineDrawModeForGui=" + String.valueOf(this.usePipelineDrawModeForGui()) + original.substring(original.length() - 1);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @ModifyReturnValue(method={"equals"}, at={@At(value="RETURN")})
    private boolean modifyEqualsToIncludeFabricExtraData(boolean original, Object other) {
        if (!original) return false;
        if (!(other instanceof FabricRenderPipeline.Snippet)) return false;
        FabricRenderPipeline.Snippet otherSnippet = (FabricRenderPipeline.Snippet)other;
        if (!this.usePipelineDrawModeForGui().equals(otherSnippet.usePipelineDrawModeForGui())) return false;
        return true;
    }

    @ModifyReturnValue(method={"hashCode"}, at={@At(value="RETURN")})
    private int modifyHashCodeToIncludeFabricExtraData(int original) {
        return RenderPipelineSnippetMixin.hashCombiner(original, this.usePipelineDrawModeForGui().hashCode());
    }

    @Unique
    private static int hashCombiner(int x, int y) {
        return x * 31 + y;
    }
}

