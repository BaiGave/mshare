/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.fluid;

import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

public interface FluidVariantAttributeHandler {
    default public Component getName(FluidVariant fluidVariant) {
        Block fluidBlock = fluidVariant.getFluid().defaultFluidState().createLegacyBlock().getBlock();
        if (!fluidVariant.isBlank() && fluidBlock == Blocks.AIR) {
            return Component.translatable(Util.makeDescriptionId("block", BuiltInRegistries.FLUID.getKey(fluidVariant.getFluid())));
        }
        return fluidBlock.getName();
    }

    default public Optional<SoundEvent> getFillSound(FluidVariant variant) {
        return Optional.empty();
    }

    default public Optional<SoundEvent> getEmptySound(FluidVariant variant) {
        return Optional.empty();
    }

    default public int getLightEmission(FluidVariant variant) {
        return variant.getFluid().defaultFluidState().createLegacyBlock().getLightEmission();
    }

    default public int getTemperature(FluidVariant variant) {
        return 300;
    }

    default public int getViscosity(FluidVariant variant, @Nullable Level level) {
        return 1000;
    }

    default public boolean isLighterThanAir(FluidVariant variant) {
        return false;
    }
}

