/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.storage.base;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public interface ExtractionOnlyStorage<T>
extends Storage<T> {
    @Override
    default public boolean supportsInsertion() {
        return false;
    }

    @Override
    default public long insert(T resource, long maxAmount, TransactionContext transaction) {
        return 0L;
    }
}

