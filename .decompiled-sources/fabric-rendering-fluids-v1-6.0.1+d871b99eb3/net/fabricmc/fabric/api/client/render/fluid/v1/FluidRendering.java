/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.render.fluid.v1;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderingImpl;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public final class FluidRendering {
    private FluidRendering() {
    }

    public static void render(FluidRenderer fluidRenderer, FluidRenderHandler handler, BlockAndTintGetter level, BlockPos pos, FluidRenderer.Output output, BlockState blockState, FluidState fluidState, DefaultRenderer defaultRenderer) {
        FluidRenderingImpl.render(fluidRenderer, handler, level, pos, output, blockState, fluidState, defaultRenderer);
    }

    public static interface DefaultRenderer {
        default public void render(FluidRenderer fluidRenderer, FluidRenderHandler handler, BlockAndTintGetter level, BlockPos pos, FluidRenderer.Output output, BlockState blockState, FluidState fluidState) {
            FluidRenderingImpl.renderVanillaDefault(fluidRenderer, level, pos, output, blockState, fluidState);
        }
    }
}

