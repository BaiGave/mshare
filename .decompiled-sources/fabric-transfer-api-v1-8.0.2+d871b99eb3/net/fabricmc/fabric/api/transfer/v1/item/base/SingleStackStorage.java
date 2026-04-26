/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.item.base;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.fabricmc.fabric.impl.transfer.item.ItemVariantImpl;
import net.minecraft.world.item.ItemStack;

public abstract class SingleStackStorage
extends SnapshotParticipant<ItemStack>
implements SingleSlotStorage<ItemVariant> {
    protected abstract ItemStack getStack();

    protected abstract void setStack(ItemStack var1);

    protected boolean canInsert(ItemVariant itemVariant) {
        return true;
    }

    protected boolean canExtract(ItemVariant itemVariant) {
        return true;
    }

    protected int getCapacity(ItemVariant itemVariant) {
        return ItemVariantImpl.getMaxStackSize(itemVariant);
    }

    @Override
    public boolean isResourceBlank() {
        return this.getStack().isEmpty();
    }

    @Override
    public ItemVariant getResource() {
        return ItemVariant.of(this.getStack());
    }

    @Override
    public long getAmount() {
        return this.getStack().getCount();
    }

    @Override
    public long getCapacity() {
        return this.getCapacity(this.getResource());
    }

    @Override
    public long insert(ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
        int insertedAmount;
        StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);
        ItemStack currentStack = this.getStack();
        if ((insertedVariant.matches(currentStack) || currentStack.isEmpty()) && this.canInsert(insertedVariant) && (insertedAmount = (int)Math.min(maxAmount, (long)(this.getCapacity(insertedVariant) - currentStack.getCount()))) > 0) {
            this.updateSnapshots(transaction);
            currentStack = this.getStack();
            if (currentStack.isEmpty()) {
                currentStack = insertedVariant.toStack(insertedAmount);
            } else {
                currentStack.grow(insertedAmount);
            }
            this.setStack(currentStack);
            return insertedAmount;
        }
        return 0L;
    }

    @Override
    public long extract(ItemVariant variant, long maxAmount, TransactionContext transaction) {
        int extracted;
        StoragePreconditions.notBlankNotNegative(variant, maxAmount);
        ItemStack currentStack = this.getStack();
        if (variant.matches(currentStack) && this.canExtract(variant) && (extracted = (int)Math.min((long)currentStack.getCount(), maxAmount)) > 0) {
            this.updateSnapshots(transaction);
            currentStack = this.getStack();
            currentStack.shrink(extracted);
            this.setStack(currentStack);
            return extracted;
        }
        return 0L;
    }

    @Override
    protected ItemStack createSnapshot() {
        ItemStack original = this.getStack();
        this.setStack(original.copy());
        return original;
    }

    @Override
    protected void readSnapshot(ItemStack snapshot) {
        this.setStack(snapshot);
    }

    public String toString() {
        return "SingleStackStorage[" + String.valueOf(this.getStack()) + "]";
    }
}

