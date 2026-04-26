/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.storage;

import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;
import org.jetbrains.annotations.UnmodifiableView;

public interface SlottedStorage<T>
extends Storage<T> {
    public int getSlotCount();

    public SingleSlotStorage<T> getSlot(int var1);

    default public @UnmodifiableView List<SingleSlotStorage<T>> getSlots() {
        return TransferApiImpl.makeListView(this);
    }
}

