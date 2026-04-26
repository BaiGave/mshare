/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.storage.base;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.Item;

public abstract class SingleVariantItemStorage<T extends TransferVariant<?>>
implements SingleSlotStorage<T> {
    private final ContainerItemContext context;
    private final Item item;

    public SingleVariantItemStorage(ContainerItemContext context) {
        this.context = context;
        this.item = context.getItemVariant().getItem();
    }

    protected abstract T getBlankResource();

    protected abstract T getResource(ItemVariant var1);

    protected abstract long getAmount(ItemVariant var1);

    protected abstract long getCapacity(T var1);

    protected abstract ItemVariant getUpdatedVariant(ItemVariant var1, T var2, long var3);

    protected boolean canInsert(T resource) {
        return true;
    }

    protected boolean canExtract(T resource) {
        return true;
    }

    private boolean tryUpdateStorage(T newResource, long newAmount, TransactionContext tx) {
        return this.context.exchange(this.getUpdatedVariant(this.context.getItemVariant(), newResource, newAmount), 1L, tx) == 1L;
    }

    @Override
    public boolean supportsInsertion() {
        return this.context.getItemVariant().isOf(this.item);
    }

    @Override
    public long insert(T insertedResource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(insertedResource, maxAmount);
        if (!this.canInsert(insertedResource)) {
            return 0L;
        }
        if (!this.context.getItemVariant().isOf(this.item)) {
            return 0L;
        }
        long amount = this.getAmount(this.context.getItemVariant());
        T resource = this.getResource(this.context.getItemVariant());
        long inserted = 0L;
        if (resource.isBlank() || amount == 0L) {
            inserted = Math.min(this.getCapacity(insertedResource), maxAmount);
        } else if (resource.equals(insertedResource)) {
            inserted = Math.min(this.getCapacity(insertedResource) - amount, maxAmount);
        }
        if (inserted > 0L && this.tryUpdateStorage(insertedResource, amount + inserted, transaction)) {
            return inserted;
        }
        return 0L;
    }

    @Override
    public boolean supportsExtraction() {
        return this.context.getItemVariant().isOf(this.item);
    }

    @Override
    public long extract(T extractedResource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(extractedResource, maxAmount);
        if (!this.canExtract(extractedResource)) {
            return 0L;
        }
        if (!this.context.getItemVariant().isOf(this.item)) {
            return 0L;
        }
        long amount = this.getAmount(this.context.getItemVariant());
        T resource = this.getResource(this.context.getItemVariant());
        long extracted = 0L;
        if (resource.equals(extractedResource)) {
            extracted = Math.min(maxAmount, amount);
        }
        if (extracted > 0L && this.tryUpdateStorage(resource, amount - extracted, transaction)) {
            return extracted;
        }
        return 0L;
    }

    @Override
    public boolean isResourceBlank() {
        return this.getResource().isBlank();
    }

    @Override
    public T getResource() {
        if (this.context.getItemVariant().isOf(this.item)) {
            return this.getResource(this.context.getItemVariant());
        }
        return this.getBlankResource();
    }

    @Override
    public long getAmount() {
        if (this.context.getItemVariant().isOf(this.item)) {
            return this.getAmount(this.context.getItemVariant());
        }
        return 0L;
    }

    @Override
    public long getCapacity() {
        if (this.context.getItemVariant().isOf(this.item)) {
            return this.getCapacity(this.getResource());
        }
        return 0L;
    }

    public String toString() {
        return "SingleVariantItemStorage[" + String.valueOf(this.context) + "/" + String.valueOf(this.item) + "]";
    }
}

