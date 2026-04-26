/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.client.fluid;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.lookup.v1.custom.ApiProviderMap;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public final class FluidVariantRendering {
    private static final ApiProviderMap<Fluid, FluidVariantRenderHandler> HANDLERS = ApiProviderMap.create();
    private static final FluidVariantRenderHandler DEFAULT_HANDLER = new FluidVariantRenderHandler(){};

    private FluidVariantRendering() {
    }

    public static void register(Fluid fluid, FluidVariantRenderHandler handler) {
        if (HANDLERS.putIfAbsent(fluid, handler) != null) {
            throw new IllegalArgumentException("Duplicate handler registration for fluid " + String.valueOf(fluid));
        }
    }

    public static @Nullable FluidVariantRenderHandler getHandler(Fluid fluid) {
        return HANDLERS.get(fluid);
    }

    public static FluidVariantRenderHandler getHandlerOrDefault(Fluid fluid) {
        FluidVariantRenderHandler handler = HANDLERS.get(fluid);
        return handler == null ? DEFAULT_HANDLER : handler;
    }

    public static List<Component> getTooltip(FluidVariant fluidVariant) {
        return FluidVariantRendering.getTooltip(fluidVariant, Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
    }

    public static List<Component> getTooltip(FluidVariant fluidVariant, TooltipFlag flag) {
        ArrayList<Component> tooltip = new ArrayList<Component>();
        tooltip.add(FluidVariantAttributes.getName(fluidVariant));
        FluidVariantRendering.getHandlerOrDefault(fluidVariant.getFluid()).appendTooltip(fluidVariant, tooltip, flag);
        if (flag.isAdvanced()) {
            tooltip.add(Component.literal(BuiltInRegistries.FLUID.getKey(fluidVariant.getFluid()).toString()).withStyle(ChatFormatting.DARK_GRAY));
        }
        return tooltip;
    }

    public static int getColor(FluidVariant fluidVariant) {
        return FluidVariantRendering.getColor(fluidVariant, null, null);
    }

    public static int getColor(FluidVariant fluidVariant, @Nullable BlockAndTintGetter view, @Nullable BlockPos pos) {
        return FluidVariantRendering.getHandlerOrDefault(fluidVariant.getFluid()).getColor(fluidVariant, view, pos);
    }
}

