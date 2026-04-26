/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.storage.base;

import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class BlankVariantView<T extends TransferVariant<?>>
implements StorageView<T> {
    private final T blankVariant;
    private final long capacity;

    public BlankVariantView(T blankVariant, long capacity) {
        if (!blankVariant.isBlank()) {
            throw new IllegalArgumentException("Expected a blank variant, received " + String.valueOf(blankVariant));
        }
        this.blankVariant = blankVariant;
        this.capacity = capacity;
    }

    @Override
    public long extract(T resource, long maxAmount, TransactionContext transaction) {
        return 0L;
    }

    @Override
    public boolean isResourceBlank() {
        return true;
    }

    @Override
    public T getResource() {
        return this.blankVariant;
    }

    @Override
    public long getAmount() {
        return 0L;
    }

    @Override
    public long getCapacity() {
        return this.capacity;
    }
}

