/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.fluid;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.impl.transfer.VariantCodecs;
import net.fabricmc.fabric.impl.transfer.fluid.FluidVariantImpl;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface FluidVariant
extends TransferVariant<Fluid> {
    public static final Codec<FluidVariant> CODEC = VariantCodecs.FLUID_CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidVariant> PACKET_CODEC = VariantCodecs.FLUID_PACKET_CODEC;

    public static FluidVariant blank() {
        return FluidVariant.of(Fluids.EMPTY);
    }

    public static FluidVariant of(Fluid fluid) {
        return FluidVariant.of(fluid, DataComponentPatch.EMPTY);
    }

    public static FluidVariant of(Fluid fluid, DataComponentPatch components) {
        return FluidVariantImpl.of(fluid, components);
    }

    default public Fluid getFluid() {
        return (Fluid)this.getObject();
    }

    @Override
    default public Holder<Fluid> typeHolder() {
        return this.getFluid().builtInRegistryHolder();
    }

    public FluidVariant withComponents(DataComponentPatch var1);
}

