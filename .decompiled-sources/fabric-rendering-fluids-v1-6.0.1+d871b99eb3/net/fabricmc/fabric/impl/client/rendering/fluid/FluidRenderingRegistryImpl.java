/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.rendering.fluid;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public final class FluidRenderingRegistryImpl {
    private static final Map<Fluid, FluidRenderHandler> HANDLERS = new IdentityHashMap<Fluid, FluidRenderHandler>();
    private static final Map<Fluid, FluidModel.Unbaked> MODELS = new IdentityHashMap<Fluid, FluidModel.Unbaked>();
    private static final Object2BooleanMap<Block> TRANSPARENCY_FOR_OVERLAY = new Object2BooleanOpenHashMap<Block>();
    private static final FluidRenderHandler DEFAULT_RENDER_HANDLER = new FluidRenderHandler(){};

    private FluidRenderingRegistryImpl() {
    }

    public static FluidRenderHandler get(Fluid fluid) {
        return HANDLERS.getOrDefault(fluid, DEFAULT_RENDER_HANDLER);
    }

    public static @Nullable FluidRenderHandler getOverride(Fluid fluid) {
        return HANDLERS.get(fluid);
    }

    public static void register(Fluid fluid, FluidModel.Unbaked model, FluidRenderHandler renderer) {
        Objects.requireNonNull(fluid, "fluid cannot be null");
        Objects.requireNonNull(model, "model cannot be null");
        Objects.requireNonNull(renderer, "renderer cannot be null");
        HANDLERS.put(fluid, renderer);
        MODELS.put(fluid, model);
    }

    public static void register(Fluid fluid, FluidModel.Unbaked model) {
        Objects.requireNonNull(fluid, "fluid cannot be null");
        Objects.requireNonNull(model, "model cannot be null");
        MODELS.put(fluid, model);
    }

    public static void setBlockTransparency(Block block, boolean transparent) {
        TRANSPARENCY_FOR_OVERLAY.put(block, transparent);
    }

    public static boolean isBlockTransparent(Block block) {
        return TRANSPARENCY_FOR_OVERLAY.getOrDefault((Object)block, block instanceof HalfTransparentBlock || block instanceof LeavesBlock);
    }

    public static Map<Fluid, FluidModel.Unbaked> getUnbakedModels() {
        return Collections.unmodifiableMap(MODELS);
    }
}

