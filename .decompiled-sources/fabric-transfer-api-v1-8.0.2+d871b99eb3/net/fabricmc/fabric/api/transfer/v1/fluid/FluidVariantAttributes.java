/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.fluid;

import java.util.Optional;
import net.fabricmc.fabric.api.lookup.v1.custom.ApiProviderMap;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public final class FluidVariantAttributes {
    private static final ApiProviderMap<Fluid, FluidVariantAttributeHandler> HANDLERS = ApiProviderMap.create();
    private static final FluidVariantAttributeHandler DEFAULT_HANDLER = new FluidVariantAttributeHandler(){};
    private static volatile boolean coloredVanillaFluidNames = false;

    private FluidVariantAttributes() {
    }

    public static void register(Fluid fluid, FluidVariantAttributeHandler handler) {
        if (HANDLERS.putIfAbsent(fluid, handler) != null) {
            throw new IllegalArgumentException("Duplicate handler registration for fluid " + String.valueOf(fluid));
        }
    }

    public static void enableColoredVanillaFluidNames() {
        coloredVanillaFluidNames = true;
    }

    public static @Nullable FluidVariantAttributeHandler getHandler(Fluid fluid) {
        return HANDLERS.get(fluid);
    }

    public static FluidVariantAttributeHandler getHandlerOrDefault(Fluid fluid) {
        FluidVariantAttributeHandler handler = HANDLERS.get(fluid);
        return handler == null ? DEFAULT_HANDLER : handler;
    }

    public static Component getName(FluidVariant variant) {
        return FluidVariantAttributes.getHandlerOrDefault(variant.getFluid()).getName(variant);
    }

    public static SoundEvent getFillSound(FluidVariant variant) {
        return FluidVariantAttributes.getHandlerOrDefault(variant.getFluid()).getFillSound(variant).or(() -> variant.getFluid().getPickupSound()).orElse(SoundEvents.BUCKET_FILL);
    }

    public static SoundEvent getEmptySound(FluidVariant variant) {
        return FluidVariantAttributes.getHandlerOrDefault(variant.getFluid()).getEmptySound(variant).orElse(SoundEvents.BUCKET_EMPTY);
    }

    public static int getLuminance(FluidVariant variant) {
        int luminance = FluidVariantAttributes.getHandlerOrDefault(variant.getFluid()).getLightEmission(variant);
        if (luminance < 0 || luminance > 15) {
            TransferApiImpl.LOGGER.warn("Broken FluidVariantAttributeHandler. Invalid luminance %d for fluid variant %s".formatted(luminance, variant));
            return DEFAULT_HANDLER.getLightEmission(variant);
        }
        return luminance;
    }

    public static int getTemperature(FluidVariant variant) {
        int temperature = FluidVariantAttributes.getHandlerOrDefault(variant.getFluid()).getTemperature(variant);
        if (temperature < 0) {
            TransferApiImpl.LOGGER.warn("Broken FluidVariantAttributeHandler. Invalid temperature %d for fluid variant %s".formatted(temperature, variant));
            return DEFAULT_HANDLER.getTemperature(variant);
        }
        return temperature;
    }

    public static int getViscosity(FluidVariant variant, @Nullable Level level) {
        int viscosity = FluidVariantAttributes.getHandlerOrDefault(variant.getFluid()).getViscosity(variant, level);
        if (viscosity <= 0) {
            TransferApiImpl.LOGGER.warn("Broken FluidVariantAttributeHandler. Invalid viscosity %d for fluid variant %s".formatted(viscosity, variant));
            return DEFAULT_HANDLER.getViscosity(variant, level);
        }
        return viscosity;
    }

    public static boolean isLighterThanAir(FluidVariant variant) {
        return FluidVariantAttributes.getHandlerOrDefault(variant.getFluid()).isLighterThanAir(variant);
    }

    static {
        FluidVariantAttributes.register(Fluids.WATER, new FluidVariantAttributeHandler(){

            @Override
            public Component getName(FluidVariant fluidVariant) {
                if (coloredVanillaFluidNames) {
                    return Blocks.WATER.getName().setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE));
                }
                return FluidVariantAttributeHandler.super.getName(fluidVariant);
            }

            @Override
            public Optional<SoundEvent> getEmptySound(FluidVariant variant) {
                return Optional.of(SoundEvents.BUCKET_EMPTY);
            }
        });
        FluidVariantAttributes.register(Fluids.LAVA, new FluidVariantAttributeHandler(){

            @Override
            public Component getName(FluidVariant fluidVariant) {
                if (coloredVanillaFluidNames) {
                    return Blocks.LAVA.getName().setStyle(Style.EMPTY.withColor(ChatFormatting.RED));
                }
                return FluidVariantAttributeHandler.super.getName(fluidVariant);
            }

            @Override
            public Optional<SoundEvent> getFillSound(FluidVariant variant) {
                return Optional.of(SoundEvents.BUCKET_FILL_LAVA);
            }

            @Override
            public Optional<SoundEvent> getEmptySound(FluidVariant variant) {
                return Optional.of(SoundEvents.BUCKET_EMPTY_LAVA);
            }

            @Override
            public int getTemperature(FluidVariant variant) {
                return 1300;
            }

            @Override
            public int getViscosity(FluidVariant variant, @Nullable Level level) {
                if (level != null && level.environmentAttributes().getDimensionValue(EnvironmentAttributes.FAST_LAVA).booleanValue()) {
                    return 2000;
                }
                return 6000;
            }
        });
    }
}

