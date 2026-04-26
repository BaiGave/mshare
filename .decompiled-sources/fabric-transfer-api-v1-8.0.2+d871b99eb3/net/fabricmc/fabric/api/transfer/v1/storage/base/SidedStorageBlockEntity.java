/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.storage.base;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public interface SidedStorageBlockEntity {
    @ApiStatus.OverrideOnly
    default public @Nullable Storage<FluidVariant> getFluidStorage(@Nullable Direction side) {
        return null;
    }

    @ApiStatus.OverrideOnly
    default public @Nullable Storage<ItemVariant> getItemStorage(@Nullable Direction side) {
        return null;
    }
}

