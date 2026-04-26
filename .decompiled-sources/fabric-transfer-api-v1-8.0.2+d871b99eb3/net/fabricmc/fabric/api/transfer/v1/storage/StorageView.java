/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.storage;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public interface StorageView<T> {
    public long extract(T var1, long var2, TransactionContext var4);

    public boolean isResourceBlank();

    public T getResource();

    public long getAmount();

    public long getCapacity();

    default public StorageView<T> getUnderlyingView() {
        return this;
    }
}

