/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.storage.base;

import java.util.Iterator;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;

public interface SingleSlotStorage<T>
extends SlottedStorage<T>,
StorageView<T> {
    @Override
    default public Iterator<StorageView<T>> iterator() {
        return TransferApiImpl.singletonIterator(this);
    }

    @Override
    default public int getSlotCount() {
        return 1;
    }

    @Override
    default public SingleSlotStorage<T> getSlot(int slot) {
        if (slot != 0) {
            throw new IndexOutOfBoundsException("Slot " + slot + " does not exist in a single-slot storage.");
        }
        return this;
    }
}

