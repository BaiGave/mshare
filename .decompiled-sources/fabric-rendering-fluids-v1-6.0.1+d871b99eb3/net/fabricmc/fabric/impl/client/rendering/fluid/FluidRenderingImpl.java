/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.rendering.fluid;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRendering;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class FluidRenderingImpl {
    private static final ScopedValue<FluidRendering.DefaultRenderer> CURRENT_DEFAULT_RENDERER = ScopedValue.newInstance();
    public static final ScopedValue<Void> IS_RENDERING_VANILLA_DEFAULT = ScopedValue.newInstance();

    public static void render(FluidRenderer fluidRenderer, FluidRenderHandler handler, BlockAndTintGetter level, BlockPos pos, FluidRenderer.Output output, BlockState blockState, FluidState fluidState, FluidRendering.DefaultRenderer defaultRenderer) {
        ScopedValue.where(CURRENT_DEFAULT_RENDERER, defaultRenderer).run(() -> handler.renderFluid(fluidRenderer, pos, level, output, blockState, fluidState));
    }

    public static void renderDefault(FluidRenderer fluidRenderer, FluidRenderHandler handler, BlockAndTintGetter level, BlockPos pos, FluidRenderer.Output output, BlockState blockState, FluidState fluidState) {
        if (CURRENT_DEFAULT_RENDERER.isBound()) {
            CURRENT_DEFAULT_RENDERER.get().render(fluidRenderer, handler, level, pos, output, blockState, fluidState);
        } else {
            FluidRenderingImpl.renderVanillaDefault(fluidRenderer, level, pos, output, blockState, fluidState);
        }
    }

    public static void renderVanillaDefault(FluidRenderer fluidRenderer, BlockAndTintGetter level, BlockPos pos, FluidRenderer.Output output, BlockState blockState, FluidState fluidState) {
        ScopedValue.where(IS_RENDERING_VANILLA_DEFAULT, null).run(() -> fluidRenderer.tesselate(level, pos, output, blockState, fluidState));
    }
}

