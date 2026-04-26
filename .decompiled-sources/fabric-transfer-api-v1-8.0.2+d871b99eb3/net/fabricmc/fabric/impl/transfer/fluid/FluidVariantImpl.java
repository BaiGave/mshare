/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.fluid;

import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;
import net.fabricmc.fabric.impl.transfer.fluid.FluidVariantCache;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public class FluidVariantImpl
implements FluidVariant {
    private final Fluid fluid;
    private final DataComponentPatch components;
    private final DataComponentMap componentMap;
    private final int hashCode;

    public static FluidVariant of(Fluid fluid, DataComponentPatch components) {
        Objects.requireNonNull(fluid, "Fluid may not be null.");
        Objects.requireNonNull(components, "Components may not be null.");
        if (!fluid.isSource(fluid.defaultFluidState()) && fluid != Fluids.EMPTY) {
            if (fluid instanceof FlowingFluid) {
                FlowingFluid flowable = (FlowingFluid)fluid;
                fluid = flowable.getSource();
            } else {
                Identifier id = BuiltInRegistries.FLUID.getKey(fluid);
                throw new IllegalArgumentException("Cannot convert flowing fluid %s (%s) into a still fluid.".formatted(id, fluid));
            }
        }
        if (components.isEmpty() || fluid == Fluids.EMPTY) {
            return ((FluidVariantCache)((Object)fluid)).fabric_getCachedFluidVariant();
        }
        return new FluidVariantImpl(fluid, components);
    }

    public static FluidVariant of(Holder<Fluid> fluid, DataComponentPatch components) {
        return FluidVariantImpl.of(fluid.value(), components);
    }

    public FluidVariantImpl(Fluid fluid, DataComponentPatch components) {
        this.fluid = fluid;
        this.components = components;
        this.componentMap = components == DataComponentPatch.EMPTY ? DataComponentMap.EMPTY : PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, components);
        this.hashCode = Objects.hash(fluid, components);
    }

    @Override
    public boolean isBlank() {
        return this.fluid == Fluids.EMPTY;
    }

    @Override
    public Fluid getObject() {
        return this.fluid;
    }

    @Override
    public @Nullable DataComponentPatch getComponentsPatch() {
        return this.components;
    }

    @Override
    public DataComponentMap getComponents() {
        return this.componentMap;
    }

    @Override
    public FluidVariant withComponents(DataComponentPatch patch) {
        return FluidVariantImpl.of(this.fluid, TransferApiImpl.mergePatches(this.getComponentsPatch(), patch));
    }

    public String toString() {
        return "FluidVariant{fluid=" + String.valueOf(this.fluid) + ", components=" + String.valueOf(this.components) + "}";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        FluidVariantImpl fluidVariant = (FluidVariantImpl)o;
        return this.hashCode == fluidVariant.hashCode && this.fluid == fluidVariant.fluid && this.componentsMatch(fluidVariant.components);
    }

    public int hashCode() {
        return this.hashCode;
    }
}

