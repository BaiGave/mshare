/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.fluid.base;

import java.util.function.Function;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.Item;

public final class FullItemFluidStorage
implements ExtractionOnlyStorage<FluidVariant>,
SingleSlotStorage<FluidVariant> {
    private final ContainerItemContext context;
    private final Item fullItem;
    private final Function<ItemVariant, ItemVariant> fullToEmptyMapping;
    private final FluidVariant containedFluid;
    private final long containedAmount;

    public FullItemFluidStorage(ContainerItemContext context, Item emptyItem, FluidVariant containedFluid, long containedAmount) {
        this(context, (ItemVariant fullVariant) -> ItemVariant.of(emptyItem, fullVariant.getComponentsPatch()), containedFluid, containedAmount);
    }

    public FullItemFluidStorage(ContainerItemContext context, Function<ItemVariant, ItemVariant> fullToEmptyMapping, FluidVariant containedFluid, long containedAmount) {
        StoragePreconditions.notBlankNotNegative(containedFluid, containedAmount);
        this.context = context;
        this.fullItem = context.getItemVariant().getItem();
        this.fullToEmptyMapping = fullToEmptyMapping;
        this.containedFluid = containedFluid;
        this.containedAmount = containedAmount;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        ItemVariant newVariant;
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        if (!this.context.getItemVariant().isOf(this.fullItem)) {
            return 0L;
        }
        if (resource.equals(this.containedFluid) && maxAmount >= this.containedAmount && this.context.exchange(newVariant = this.fullToEmptyMapping.apply(this.context.getItemVariant()), 1L, transaction) == 1L) {
            return this.containedAmount;
        }
        return 0L;
    }

    @Override
    public boolean isResourceBlank() {
        return this.getResource().isBlank();
    }

    @Override
    public FluidVariant getResource() {
        if (this.context.getItemVariant().isOf(this.fullItem)) {
            return this.containedFluid;
        }
        return FluidVariant.blank();
    }

    @Override
    public long getAmount() {
        if (this.context.getItemVariant().isOf(this.fullItem)) {
            return this.containedAmount;
        }
        return 0L;
    }

    @Override
    public long getCapacity() {
        return this.getAmount();
    }

    public String toString() {
        return "FullItemFluidStorage[context=%s, fluid=%s, amount=%d]".formatted(this.context, this.containedFluid, this.containedAmount);
    }
}

