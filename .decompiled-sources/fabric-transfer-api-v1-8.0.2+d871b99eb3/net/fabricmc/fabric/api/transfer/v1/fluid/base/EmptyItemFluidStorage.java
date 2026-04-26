/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.fluid.base;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.BlankVariantView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public final class EmptyItemFluidStorage
implements InsertionOnlyStorage<FluidVariant> {
    private final ContainerItemContext context;
    private final Item emptyItem;
    private final Function<ItemVariant, ItemVariant> emptyToFullMapping;
    private final Fluid insertableFluid;
    private final long insertableAmount;
    private final List<StorageView<FluidVariant>> blankView;

    public EmptyItemFluidStorage(ContainerItemContext context, Item fullItem, Fluid insertableFluid, long insertableAmount) {
        this(context, (ItemVariant emptyVariant) -> ItemVariant.of(fullItem, emptyVariant.getComponentsPatch()), insertableFluid, insertableAmount);
    }

    public EmptyItemFluidStorage(ContainerItemContext context, Function<ItemVariant, ItemVariant> emptyToFullMapping, Fluid insertableFluid, long insertableAmount) {
        StoragePreconditions.notNegative(insertableAmount);
        this.context = context;
        this.emptyItem = context.getItemVariant().getItem();
        this.emptyToFullMapping = emptyToFullMapping;
        this.insertableFluid = insertableFluid;
        this.insertableAmount = insertableAmount;
        this.blankView = List.of(new BlankVariantView<FluidVariant>(FluidVariant.blank(), insertableAmount));
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        ItemVariant newVariant;
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        if (!this.context.getItemVariant().isOf(this.emptyItem)) {
            return 0L;
        }
        if (resource.isOf(this.insertableFluid) && maxAmount >= this.insertableAmount && this.context.exchange(newVariant = this.emptyToFullMapping.apply(this.context.getItemVariant()), 1L, transaction) == 1L) {
            return this.insertableAmount;
        }
        return 0L;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return this.blankView.iterator();
    }

    public String toString() {
        return "EmptyItemFluidStorage[context=%s, insertableFluid=%s, insertableAmount=%d]".formatted(this.context, this.insertableFluid, this.insertableAmount);
    }
}

