/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.block.model;

import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={SingleVariant.class})
abstract class SingleVariantMixin
implements BlockStateModel {
    @Shadow
    @Final
    private BlockStateModelPart model;

    SingleVariantMixin() {
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        boolean cutoutLeaves = Minecraft.getInstance().options.cutoutLeaves().get();
        boolean forceOpaque = ModelBlockRenderer.forceOpaque(cutoutLeaves, state);
        if (forceOpaque) {
            emitter.pushTransform(quad -> {
                quad.chunkLayer(ChunkSectionLayer.SOLID);
                return true;
            });
        }
        this.model.emitQuads(emitter, cullTest);
        if (forceOpaque) {
            emitter.popTransform();
        }
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return this;
    }
}

