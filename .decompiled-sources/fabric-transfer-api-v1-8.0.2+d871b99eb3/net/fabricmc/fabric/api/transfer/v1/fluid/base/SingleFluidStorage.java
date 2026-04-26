/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.fluid.base;

import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class SingleFluidStorage
extends SingleVariantStorage<FluidVariant> {
    public static SingleFluidStorage withFixedCapacity(final long capacity, final Runnable onChange) {
        StoragePreconditions.notNegative(capacity);
        Objects.requireNonNull(onChange, "onChange may not be null");
        return new SingleFluidStorage(){

            @Override
            protected long getCapacity(FluidVariant variant) {
                return capacity;
            }

            @Override
            protected void onFinalCommit() {
                onChange.run();
            }
        };
    }

    @Override
    protected final FluidVariant getBlankVariant() {
        return FluidVariant.blank();
    }

    public void readValue(ValueInput value) {
        SingleVariantStorage.readValue(this, FluidVariant.CODEC, FluidVariant::blank, value);
    }

    public void writeValue(ValueOutput value) {
        SingleVariantStorage.writeValue(this, FluidVariant.CODEC, value);
    }
}

