/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.storage.base;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class SingleVariantStorage<T extends TransferVariant<?>>
extends SnapshotParticipant<ResourceAmount<T>>
implements SingleSlotStorage<T> {
    public T variant = this.getBlankVariant();
    public long amount = 0L;

    protected abstract T getBlankVariant();

    protected abstract long getCapacity(T var1);

    protected boolean canInsert(T variant) {
        return true;
    }

    protected boolean canExtract(T variant) {
        return true;
    }

    @Override
    public long insert(T insertedVariant, long maxAmount, TransactionContext transaction) {
        long insertedAmount;
        StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);
        if ((insertedVariant.equals(this.variant) || this.variant.isBlank()) && this.canInsert(insertedVariant) && (insertedAmount = Math.min(maxAmount, this.getCapacity(insertedVariant) - this.amount)) > 0L) {
            this.updateSnapshots(transaction);
            if (this.variant.isBlank()) {
                this.variant = insertedVariant;
                this.amount = insertedAmount;
            } else {
                this.amount += insertedAmount;
            }
            return insertedAmount;
        }
        return 0L;
    }

    @Override
    public long extract(T extractedVariant, long maxAmount, TransactionContext transaction) {
        long extractedAmount;
        StoragePreconditions.notBlankNotNegative(extractedVariant, maxAmount);
        if (extractedVariant.equals(this.variant) && this.canExtract(extractedVariant) && (extractedAmount = Math.min(maxAmount, this.amount)) > 0L) {
            this.updateSnapshots(transaction);
            this.amount -= extractedAmount;
            if (this.amount == 0L) {
                this.variant = this.getBlankVariant();
            }
            return extractedAmount;
        }
        return 0L;
    }

    @Override
    public boolean isResourceBlank() {
        return this.variant.isBlank();
    }

    @Override
    public T getResource() {
        return this.variant;
    }

    @Override
    public long getAmount() {
        return this.amount;
    }

    @Override
    public long getCapacity() {
        return this.getCapacity(this.variant);
    }

    @Override
    protected ResourceAmount<T> createSnapshot() {
        return new ResourceAmount<T>(this.variant, this.amount);
    }

    @Override
    protected void readSnapshot(ResourceAmount<T> snapshot) {
        this.variant = (TransferVariant)snapshot.resource();
        this.amount = snapshot.amount();
    }

    public String toString() {
        return "SingleVariantStorage[%d %s]".formatted(this.amount, this.variant);
    }

    public static <T extends TransferVariant<?>> void readValue(SingleVariantStorage<T> storage, Codec<T> codec, Supplier<T> fallback, ValueInput value) {
        storage.variant = (TransferVariant)value.read("variant", codec).orElseGet(fallback);
        storage.amount = value.getLongOr("amount", 0L);
    }

    public static <T extends TransferVariant<?>> void writeValue(SingleVariantStorage<T> storage, Codec<T> codec, ValueOutput value) {
        value.store("variant", codec, storage.variant);
        value.putLong("amount", storage.amount);
    }
}

