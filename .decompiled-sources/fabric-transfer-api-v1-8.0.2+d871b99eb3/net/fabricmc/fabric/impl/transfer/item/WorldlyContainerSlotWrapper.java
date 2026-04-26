/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.item;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.transfer.DebugMessages;
import net.fabricmc.fabric.impl.transfer.item.ContainerSlotWrapper;
import net.fabricmc.fabric.impl.transfer.item.ItemVariantImpl;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;

class WorldlyContainerSlotWrapper
implements SingleSlotStorage<ItemVariant> {
    private final ContainerSlotWrapper slotWrapper;
    private final WorldlyContainer container;
    private final Direction direction;

    WorldlyContainerSlotWrapper(ContainerSlotWrapper slotWrapper, WorldlyContainer container, Direction direction) {
        this.slotWrapper = slotWrapper;
        this.container = container;
        this.direction = direction;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (!this.container.canPlaceItemThroughFace(this.slotWrapper.slot, ((ItemVariantImpl)resource).getCachedStack(), this.direction)) {
            return 0L;
        }
        return this.slotWrapper.insert(resource, maxAmount, transaction);
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (!this.container.canTakeItemThroughFace(this.slotWrapper.slot, ((ItemVariantImpl)resource).getCachedStack(), this.direction)) {
            return 0L;
        }
        return this.slotWrapper.extract(resource, maxAmount, transaction);
    }

    @Override
    public boolean isResourceBlank() {
        return this.slotWrapper.isResourceBlank();
    }

    @Override
    public ItemVariant getResource() {
        return this.slotWrapper.getResource();
    }

    @Override
    public long getAmount() {
        return this.slotWrapper.getAmount();
    }

    @Override
    public long getCapacity() {
        return this.slotWrapper.getCapacity();
    }

    @Override
    public StorageView<ItemVariant> getUnderlyingView() {
        return this.slotWrapper.getUnderlyingView();
    }

    public String toString() {
        return "WorldlyContainerSlotWrapper[%s#%d/%s]".formatted(DebugMessages.forInventory(this.container), this.slotWrapper.slot, this.direction.name());
    }
}

