/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.storage.base;

import java.util.List;
import java.util.StringJoiner;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;

public class CombinedSlottedStorage<T, S extends SlottedStorage<T>>
extends CombinedStorage<T, S>
implements SlottedStorage<T> {
    public CombinedSlottedStorage(List<S> parts) {
        super(parts);
    }

    @Override
    public int getSlotCount() {
        int count = 0;
        for (SlottedStorage part : this.parts) {
            count += part.getSlotCount();
        }
        return count;
    }

    @Override
    public SingleSlotStorage<T> getSlot(int slot) {
        int updatedSlot = slot;
        for (SlottedStorage part : this.parts) {
            if (updatedSlot < part.getSlotCount()) {
                return part.getSlot(updatedSlot);
            }
            updatedSlot -= part.getSlotCount();
        }
        throw new IndexOutOfBoundsException("Slot " + slot + " is out of bounds. This storage has size " + this.getSlotCount());
    }

    @Override
    public String toString() {
        StringJoiner partNames = new StringJoiner(", ");
        for (SlottedStorage part : this.parts) {
            partNames.add(part.toString());
        }
        return "CombinedSlottedStorage[" + String.valueOf(partNames) + "]";
    }
}

