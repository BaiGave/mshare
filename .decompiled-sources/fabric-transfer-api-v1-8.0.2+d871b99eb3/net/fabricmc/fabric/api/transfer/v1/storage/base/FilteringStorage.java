/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.storage.base;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public abstract class FilteringStorage<T>
implements Storage<T> {
    protected final Supplier<Storage<T>> backingStorage;

    public static <T> Storage<T> insertOnlyOf(Storage<T> backingStorage) {
        return FilteringStorage.of(backingStorage, true, false);
    }

    public static <T> Storage<T> extractOnlyOf(Storage<T> backingStorage) {
        return FilteringStorage.of(backingStorage, false, true);
    }

    public static <T> Storage<T> readOnlyOf(Storage<T> backingStorage) {
        return FilteringStorage.of(backingStorage, false, false);
    }

    public static <T> Storage<T> of(Storage<T> backingStorage, final boolean allowInsert, final boolean allowExtract) {
        if (allowInsert && allowExtract) {
            return backingStorage;
        }
        return new FilteringStorage<T>(backingStorage){

            @Override
            protected boolean canInsert(T resource) {
                return allowInsert;
            }

            @Override
            protected boolean canExtract(T resource) {
                return allowExtract;
            }

            @Override
            public boolean supportsInsertion() {
                return allowInsert && super.supportsInsertion();
            }

            @Override
            public boolean supportsExtraction() {
                return allowExtract && super.supportsExtraction();
            }
        };
    }

    public FilteringStorage(Storage<T> backingStorage) {
        this(() -> backingStorage);
    }

    public FilteringStorage(Supplier<Storage<T>> backingStorage) {
        this.backingStorage = backingStorage;
    }

    protected boolean canInsert(T resource) {
        return true;
    }

    protected boolean canExtract(T resource) {
        return true;
    }

    @Override
    public boolean supportsInsertion() {
        return this.backingStorage.get().supportsInsertion();
    }

    @Override
    public long insert(T resource, long maxAmount, TransactionContext transaction) {
        if (this.canInsert(resource)) {
            return this.backingStorage.get().insert(resource, maxAmount, transaction);
        }
        return 0L;
    }

    @Override
    public boolean supportsExtraction() {
        return this.backingStorage.get().supportsExtraction();
    }

    @Override
    public long extract(T resource, long maxAmount, TransactionContext transaction) {
        if (this.canExtract(resource)) {
            return this.backingStorage.get().extract(resource, maxAmount, transaction);
        }
        return 0L;
    }

    @Override
    public Iterator<StorageView<T>> iterator() {
        return Iterators.transform(this.backingStorage.get().iterator(), x$0 -> new FilteringStorageView(this, x$0));
    }

    @Override
    public long getVersion() {
        return this.backingStorage.get().getVersion();
    }

    public String toString() {
        return "FilteringStorage[" + String.valueOf(this.backingStorage.get()) + "/" + String.valueOf(this.backingStorage) + "]";
    }

    private class FilteringStorageView
    implements StorageView<T> {
        private final StorageView<T> backingView;
        final /* synthetic */ FilteringStorage this$0;

        private FilteringStorageView(FilteringStorage filteringStorage, StorageView<T> backingView) {
            FilteringStorage filteringStorage2 = filteringStorage;
            Objects.requireNonNull(filteringStorage2);
            this.this$0 = filteringStorage2;
            this.backingView = backingView;
        }

        @Override
        public long extract(T resource, long maxAmount, TransactionContext transaction) {
            if (this.this$0.canExtract(resource)) {
                return this.backingView.extract(resource, maxAmount, transaction);
            }
            return 0L;
        }

        @Override
        public boolean isResourceBlank() {
            return this.backingView.isResourceBlank();
        }

        @Override
        public T getResource() {
            return this.backingView.getResource();
        }

        @Override
        public long getAmount() {
            return this.backingView.getAmount();
        }

        @Override
        public long getCapacity() {
            return this.backingView.getCapacity();
        }

        @Override
        public StorageView<T> getUnderlyingView() {
            return this.backingView.getUnderlyingView();
        }
    }
}

