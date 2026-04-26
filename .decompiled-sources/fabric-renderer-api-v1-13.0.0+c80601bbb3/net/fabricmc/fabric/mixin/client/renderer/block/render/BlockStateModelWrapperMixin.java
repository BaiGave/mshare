/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.block.render;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockStateModelWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={BlockStateModelWrapper.class})
abstract class BlockStateModelWrapperMixin
implements BlockModel {
    @Shadow
    @Final
    private BlockStateModel model;
    @Shadow
    @Final
    private Matrix4fc transformation;

    BlockStateModelWrapperMixin() {
    }

    @Shadow
    abstract void updateTints(BlockModelRenderState var1, BlockState var2);

    @Override
    @Overwrite
    public void update(BlockModelRenderState output, BlockState blockState, BlockDisplayContext displayContext, long seed) {
        QuadEmitter emitter = output.setupMesh(this.transformation, this.model.hasMaterialFlag(BlockAndTintGetter.EMPTY, BlockPos.ZERO, blockState, output.scratchRandomSource(seed), 1));
        this.model.emitQuads(emitter, BlockAndTintGetter.EMPTY, BlockPos.ZERO, blockState, output.scratchRandomSource(seed), direction -> false);
        this.updateTints(output, blockState);
    }
}

