/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.storage.base;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.StringJoiner;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class CombinedStorage<T, S extends Storage<T>>
implements Storage<T> {
    public List<S> parts;

    public CombinedStorage(List<S> parts) {
        this.parts = parts;
    }

    @Override
    public boolean supportsInsertion() {
        for (Storage part : this.parts) {
            if (!part.supportsInsertion()) continue;
            return true;
        }
        return false;
    }

    @Override
    public long insert(T resource, long maxAmount, TransactionContext transaction) {
        Storage part;
        StoragePreconditions.notNegative(maxAmount);
        long amount = 0L;
        Iterator<S> iterator = this.parts.iterator();
        while (iterator.hasNext() && (amount += (part = (Storage)iterator.next()).insert(resource, maxAmount - amount, transaction)) != maxAmount) {
        }
        return amount;
    }

    @Override
    public boolean supportsExtraction() {
        for (Storage part : this.parts) {
            if (!part.supportsExtraction()) continue;
            return true;
        }
        return false;
    }

    @Override
    public long extract(T resource, long maxAmount, TransactionContext transaction) {
        Storage part;
        StoragePreconditions.notNegative(maxAmount);
        long amount = 0L;
        Iterator<S> iterator = this.parts.iterator();
        while (iterator.hasNext() && (amount += (part = (Storage)iterator.next()).extract(resource, maxAmount - amount, transaction)) != maxAmount) {
        }
        return amount;
    }

    @Override
    public Iterator<StorageView<T>> iterator() {
        return new CombinedIterator(this);
    }

    public String toString() {
        StringJoiner partNames = new StringJoiner(", ");
        for (Storage part : this.parts) {
            partNames.add(part.toString());
        }
        return "CombinedStorage[" + String.valueOf(partNames) + "]";
    }

    private class CombinedIterator
    implements Iterator<StorageView<T>> {
        final Iterator<S> partIterator;
        Iterator<? extends StorageView<T>> currentPartIterator;
        final /* synthetic */ CombinedStorage this$0;

        CombinedIterator(CombinedStorage combinedStorage) {
            CombinedStorage combinedStorage2 = combinedStorage;
            Objects.requireNonNull(combinedStorage2);
            this.this$0 = combinedStorage2;
            this.partIterator = this.this$0.parts.iterator();
            this.currentPartIterator = null;
            this.advanceCurrentPartIterator();
        }

        @Override
        public boolean hasNext() {
            return this.currentPartIterator != null && this.currentPartIterator.hasNext();
        }

        @Override
        public StorageView<T> next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            StorageView returned = this.currentPartIterator.next();
            if (!this.currentPartIterator.hasNext()) {
                this.advanceCurrentPartIterator();
            }
            return returned;
        }

        private void advanceCurrentPartIterator() {
            while (this.partIterator.hasNext()) {
                this.currentPartIterator = ((Storage)this.partIterator.next()).iterator();
                if (!this.currentPartIterator.hasNext()) continue;
                break;
            }
        }
    }
}

