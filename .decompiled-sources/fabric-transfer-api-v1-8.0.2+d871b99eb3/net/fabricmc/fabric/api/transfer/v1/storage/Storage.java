/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.storage;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;

public interface Storage<T>
extends Iterable<StorageView<T>> {
    public static <T> Storage<T> empty() {
        return TransferApiImpl.EMPTY_STORAGE;
    }

    default public boolean supportsInsertion() {
        return true;
    }

    public long insert(T var1, long var2, TransactionContext var4);

    default public boolean supportsExtraction() {
        return true;
    }

    public long extract(T var1, long var2, TransactionContext var4);

    @Override
    public Iterator<StorageView<T>> iterator();

    default public Iterator<StorageView<T>> nonEmptyIterator() {
        return Iterators.filter(this.iterator(), view -> view.getAmount() > 0L && !view.isResourceBlank());
    }

    default public Iterable<StorageView<T>> nonEmptyViews() {
        return this::nonEmptyIterator;
    }

    default public long getVersion() {
        if (Transaction.isOpen()) {
            throw new IllegalStateException("getVersion() may not be called during a transaction.");
        }
        return TransferApiImpl.version.getAndIncrement();
    }

    public static <T> Class<Storage<T>> asClass() {
        return Storage.class;
    }
}

