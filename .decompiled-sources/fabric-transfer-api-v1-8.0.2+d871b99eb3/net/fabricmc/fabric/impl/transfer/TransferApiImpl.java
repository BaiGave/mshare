/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer;

import java.util.AbstractList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferApiImpl {
    public static final Logger LOGGER = LoggerFactory.getLogger("fabric-transfer-api-v1");
    public static final AtomicLong version = new AtomicLong();
    public static final Storage EMPTY_STORAGE = new Storage(){

        @Override
        public boolean supportsInsertion() {
            return false;
        }

        public long insert(Object resource, long maxAmount, TransactionContext transaction) {
            return 0L;
        }

        @Override
        public boolean supportsExtraction() {
            return false;
        }

        public long extract(Object resource, long maxAmount, TransactionContext transaction) {
            return 0L;
        }

        @Override
        public Iterator<StorageView> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public long getVersion() {
            return 0L;
        }

        public String toString() {
            return "EmptyStorage";
        }
    };

    public static <T> Iterator<T> singletonIterator(final T it) {
        return new Iterator<T>(){
            boolean hasNext = true;

            @Override
            public boolean hasNext() {
                return this.hasNext;
            }

            @Override
            public T next() {
                if (!this.hasNext) {
                    throw new NoSuchElementException();
                }
                this.hasNext = false;
                return it;
            }
        };
    }

    public static <T> List<SingleSlotStorage<T>> makeListView(final SlottedStorage<T> storage) {
        return new AbstractList<SingleSlotStorage<T>>(){

            @Override
            public SingleSlotStorage<T> get(int index) {
                return storage.getSlot(index);
            }

            @Override
            public int size() {
                return storage.getSlotCount();
            }
        };
    }

    public static DataComponentPatch mergePatches(DataComponentPatch base, DataComponentPatch applied) {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        TransferApiImpl.writeChangesTo(base, builder);
        TransferApiImpl.writeChangesTo(applied, builder);
        return builder.build();
    }

    private static void writeChangesTo(DataComponentPatch changes, DataComponentPatch.Builder builder) {
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : changes.entrySet()) {
            if (entry.getValue().isPresent()) {
                builder.set(entry.getKey(), entry.getValue().get());
                continue;
            }
            builder.remove(entry.getKey());
        }
    }
}

