/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.block.render;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexSorting;
import java.util.Map;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.AltModelBlockRenderer;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={SectionCompiler.class})
abstract class SectionCompilerMixin {
    @Shadow
    @Final
    private boolean ambientOcclusion;
    @Shadow
    @Final
    private BlockColors blockColors;

    SectionCompilerMixin() {
    }

    @Shadow
    protected abstract BufferBuilder getOrBeginLayer(Map<ChunkSectionLayer, BufferBuilder> var1, SectionBufferBuilderPack var2, ChunkSectionLayer var3);

    @Inject(method={"compile"}, at={@At(value="INVOKE", target="Lnet/minecraft/core/BlockPos;betweenClosed(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Ljava/lang/Iterable;")})
    private void beforeLoopCompile(SectionPos sectionPos, RenderSectionRegion region, VertexSorting vertexSorting, SectionBufferBuilderPack builders, CallbackInfoReturnable<SectionCompiler.Results> cir, @Local(name={"startedLayers"}) Map<ChunkSectionLayer, BufferBuilder> startedLayers, @Share(value="altBlockRenderer") LocalRef<AltModelBlockRenderer> altBlockRenderer, @Share(value="altQuadOutput") LocalRef<QuadEmitter> altQuadOutput) {
        altBlockRenderer.set(Renderer.get().altModelBlockRenderer(this.ambientOcclusion, true, this.blockColors));
        altQuadOutput.set(Renderer.get().quadEmitter(quad -> {
            BufferBuilder builder = this.getOrBeginLayer(startedLayers, builders, quad.chunkLayer());
            quad.buffer(OverlayTexture.NO_OVERLAY, builder);
        }));
    }

    @Redirect(method={"compile"}, at=@At(value="INVOKE", target="net/minecraft/client/renderer/block/ModelBlockRenderer.tesselateBlock(Lnet/minecraft/client/renderer/block/BlockQuadOutput;FFFLnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;J)V"))
    private void tesselateBlockProxy(ModelBlockRenderer blockRenderer, BlockQuadOutput output, float x, float y, float z, BlockAndTintGetter level, BlockPos pos, BlockState blockState, BlockStateModel model, long seed, @Share(value="altBlockRenderer") LocalRef<AltModelBlockRenderer> altBlockRenderer, @Share(value="altQuadOutput") LocalRef<QuadEmitter> altQuadOutput) {
        altBlockRenderer.get().tesselateBlock(altQuadOutput.get(), x, y, z, level, pos, blockState, model, seed);
    }
}

