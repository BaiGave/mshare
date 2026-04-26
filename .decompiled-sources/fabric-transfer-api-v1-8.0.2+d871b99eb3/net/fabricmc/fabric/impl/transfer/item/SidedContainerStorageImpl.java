/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.item;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.impl.transfer.item.ContainerStorageImpl;
import net.fabricmc.fabric.impl.transfer.item.WorldlyContainerSlotWrapper;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;

class SidedContainerStorageImpl
extends CombinedStorage<ItemVariant, SingleSlotStorage<ItemVariant>>
implements ContainerStorage {
    private final ContainerStorageImpl backingStorage;

    SidedContainerStorageImpl(ContainerStorageImpl storage, Direction direction) {
        super(Collections.unmodifiableList(SidedContainerStorageImpl.createWrapperList(storage, direction)));
        this.backingStorage = storage;
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getSlots() {
        return this.parts;
    }

    private static List<SingleSlotStorage<ItemVariant>> createWrapperList(ContainerStorageImpl storage, Direction direction) {
        WorldlyContainer inventory = (WorldlyContainer)storage.container;
        int[] availableSlots = inventory.getSlotsForFace(direction);
        WorldlyContainerSlotWrapper[] slots = new WorldlyContainerSlotWrapper[availableSlots.length];
        for (int i = 0; i < availableSlots.length; ++i) {
            slots[i] = new WorldlyContainerSlotWrapper(storage.backingList.get(availableSlots[i]), inventory, direction);
        }
        return Arrays.asList(slots);
    }

    @Override
    public String toString() {
        return this.backingStorage.toString();
    }
}

