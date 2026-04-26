/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.render.fluid.v1;

import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderingImpl;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public interface FluidRenderHandler {
    default public void renderFluid(FluidRenderer fluidRenderer, BlockPos pos, BlockAndTintGetter level, FluidRenderer.Output output, BlockState blockState, FluidState fluidState) {
        FluidRenderingImpl.renderDefault(fluidRenderer, this, level, pos, output, blockState, fluidState);
    }
}

