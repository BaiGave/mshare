/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.client.fluid;

import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

public interface FluidVariantRenderHandler {
    default public void appendTooltip(FluidVariant fluidVariant, List<Component> tooltip, TooltipFlag tooltipFlag) {
    }

    default public int getColor(FluidVariant fluidVariant, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos) {
        FluidState fluidState = fluidVariant.getFluid().defaultFluidState();
        FluidModel fluidModel = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluidState);
        if (fluidModel.tintSource() == null) {
            return -1;
        }
        if (level != null && pos != null) {
            return fluidModel.tintSource().colorInWorld(Blocks.AIR.defaultBlockState(), level, pos);
        }
        return fluidModel.tintSource().color(Blocks.AIR.defaultBlockState());
    }
}

