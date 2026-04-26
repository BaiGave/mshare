/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.PolygonMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderPipeline;
import net.fabricmc.fabric.impl.client.rendering.FabricRenderPipelineImpl;
import net.fabricmc.fabric.impl.client.rendering.FabricRenderPipelineInternals;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={RenderPipeline.Builder.class})
class RenderPipelineBuilderMixin
implements FabricRenderPipeline.Builder {
    @Unique
    private Optional<Boolean> usePipelineDrawModeForGui = Optional.empty();

    RenderPipelineBuilderMixin() {
    }

    @Override
    public RenderPipeline.Builder withUsePipelineDrawModeForGui(boolean usePipelineDrawMode) {
        this.usePipelineDrawModeForGui = Optional.of(usePipelineDrawMode);
        return (RenderPipeline.Builder)((Object)this);
    }

    @Override
    public RenderPipeline.Builder withoutUsePipelineDrawModeForGui() {
        this.usePipelineDrawModeForGui = Optional.empty();
        return (RenderPipeline.Builder)((Object)this);
    }

    @Inject(method={"withSnippet"}, at={@At(value="TAIL")})
    private void copyUsePipelineDrawModeForGuiFromSnippet(RenderPipeline.Snippet snippet, CallbackInfo ci) {
        snippet.usePipelineDrawModeForGui().ifPresent(value -> {
            this.usePipelineDrawModeForGui = Optional.of(value);
        });
    }

    @WrapOperation(method={"buildSnippet"}, at={@At(value="NEW", target="Lcom/mojang/blaze3d/pipeline/RenderPipeline$Snippet;")})
    private RenderPipeline.Snippet copyUsePipelineDrawModeForGuiToSnippet(Optional<Identifier> vertexShader, Optional<Identifier> fragmentShader, Optional<ShaderDefines> shaderDefines, Optional<List<String>> samplers, Optional<List<RenderPipeline.UniformDescription>> uniforms, Optional<ColorTargetState> colorTargetState, Optional<DepthStencilState> depthStencilState, Optional<PolygonMode> polygonMode, Optional<Boolean> cull, Optional<VertexFormat> vertexFormat, Optional<VertexFormat.Mode> vertexFormatMode, Operation<RenderPipeline.Snippet> original) {
        return FabricRenderPipelineInternals.withSnippetUsePipelineVertexFormatForGui(() -> (RenderPipeline.Snippet)original.call(vertexShader, fragmentShader, shaderDefines, samplers, uniforms, colorTargetState, depthStencilState, polygonMode, cull, vertexFormat, vertexFormatMode), this.usePipelineDrawModeForGui);
    }

    @ModifyReturnValue(method={"build"}, at={@At(value="RETURN")})
    private RenderPipeline copyUsePipelineDrawModeForGuiToPipeline(RenderPipeline original) {
        ((FabricRenderPipelineImpl)((Object)original)).fabric$setUsePipelineDrawModeForGuiSetter(this.usePipelineDrawModeForGui.orElse(false));
        return original;
    }
}

