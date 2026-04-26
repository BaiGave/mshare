/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.render.fluid.v1;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderingRegistryImpl;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public final class FluidRenderingRegistry {
    private FluidRenderingRegistry() {
    }

    public static FluidRenderHandler get(Fluid fluid) {
        return FluidRenderingRegistryImpl.get(fluid);
    }

    public static @Nullable FluidRenderHandler getOverride(Fluid fluid) {
        return FluidRenderingRegistryImpl.getOverride(fluid);
    }

    public static void register(Fluid fluid, FluidModel.Unbaked model, FluidRenderHandler renderer) {
        FluidRenderingRegistryImpl.register(fluid, model, renderer);
    }

    public static void register(Fluid fluid, FluidModel.Unbaked model) {
        FluidRenderingRegistryImpl.register(fluid, model);
    }

    public static void register(Fluid still, Fluid flow, FluidModel.Unbaked model, FluidRenderHandler renderer) {
        FluidRenderingRegistry.register(still, model, renderer);
        FluidRenderingRegistry.register(flow, model, renderer);
    }

    public static void register(Fluid still, Fluid flow, FluidModel.Unbaked model) {
        FluidRenderingRegistry.register(still, model);
        FluidRenderingRegistry.register(flow, model);
    }

    public static void setBlockTransparency(Block block, boolean transparent) {
        FluidRenderingRegistryImpl.setBlockTransparency(block, transparent);
    }

    public static boolean isBlockTransparent(Block block) {
        return FluidRenderingRegistryImpl.isBlockTransparent(block);
    }
}

