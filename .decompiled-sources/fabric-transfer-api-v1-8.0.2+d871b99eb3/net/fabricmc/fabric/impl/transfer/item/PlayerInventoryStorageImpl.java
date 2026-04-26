/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.fabricmc.fabric.impl.transfer.DebugMessages;
import net.fabricmc.fabric.impl.transfer.item.ContainerStorageImpl;
import net.fabricmc.fabric.impl.transfer.item.ItemVariantImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;

class PlayerInventoryStorageImpl
extends ContainerStorageImpl
implements PlayerInventoryStorage {
    private final DroppedStacks droppedStacks = new DroppedStacks(this);
    private final Inventory inventory;

    PlayerInventoryStorageImpl(Inventory inventory) {
        super(inventory);
        this.inventory = inventory;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return this.offer(resource, maxAmount, transaction);
    }

    @Override
    public long offer(ItemVariant resource, long amount, TransactionContext tx) {
        StoragePreconditions.notBlankNotNegative(resource, amount);
        long initialAmount = amount;
        List<SingleSlotStorage<ItemVariant>> mainSlots = this.getSlots().subList(0, 36);
        for (InteractionHand hand : InteractionHand.values()) {
            SingleSlotStorage<ItemVariant> handSlot = this.getHandSlot(hand);
            if (!((ItemVariant)handSlot.getResource()).equals(resource) || (amount -= handSlot.insert(resource, amount, tx)) != 0L) continue;
            return initialAmount;
        }
        amount -= StorageUtil.insertStacking(mainSlots, resource, amount, tx);
        return initialAmount - amount;
    }

    @Override
    public void drop(ItemVariant variant, long amount, boolean throwRandomly, boolean retainOwnership, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(variant, amount);
        if (amount > 0L && !this.inventory.player.level().isClientSide()) {
            this.droppedStacks.addDrop(variant, amount, throwRandomly, retainOwnership, transaction);
        }
    }

    @Override
    public SingleSlotStorage<ItemVariant> getHandSlot(InteractionHand hand) {
        if (Objects.requireNonNull(hand) == InteractionHand.MAIN_HAND) {
            if (Inventory.isHotbarSlot(this.inventory.getSelectedSlot())) {
                return this.getSlot(this.inventory.getSelectedSlot());
            }
            throw new RuntimeException("Unexpected player selected slot: " + this.inventory.getSelectedSlot());
        }
        if (hand == InteractionHand.OFF_HAND) {
            return this.getSlot(40);
        }
        throw new UnsupportedOperationException("Unknown hand: " + String.valueOf((Object)hand));
    }

    @Override
    public String toString() {
        return "PlayerInventoryStorage[" + DebugMessages.forInventory(this.inventory) + "]";
    }

    private class DroppedStacks
    extends SnapshotParticipant<Integer> {
        final List<Entry> entries;
        final /* synthetic */ PlayerInventoryStorageImpl this$0;

        private DroppedStacks(PlayerInventoryStorageImpl playerInventoryStorageImpl) {
            PlayerInventoryStorageImpl playerInventoryStorageImpl2 = playerInventoryStorageImpl;
            Objects.requireNonNull(playerInventoryStorageImpl2);
            this.this$0 = playerInventoryStorageImpl2;
            this.entries = new ArrayList<Entry>();
        }

        void addDrop(ItemVariant key, long amount, boolean throwRandomly, boolean retainOwnership, TransactionContext transaction) {
            this.updateSnapshots(transaction);
            this.entries.add(new Entry(key, amount, throwRandomly, retainOwnership));
        }

        @Override
        protected Integer createSnapshot() {
            return this.entries.size();
        }

        @Override
        protected void readSnapshot(Integer snapshot) {
            int previousSize = snapshot;
            while (this.entries.size() > previousSize) {
                this.entries.remove(this.entries.size() - 1);
            }
        }

        @Override
        protected void onFinalCommit() {
            for (Entry entry : this.entries) {
                int dropped;
                for (long remainder = entry.amount; remainder > 0L; remainder -= (long)dropped) {
                    dropped = (int)Math.min((long)ItemVariantImpl.getMaxStackSize(entry.key), remainder);
                    this.this$0.inventory.player.drop(entry.key.toStack(dropped), entry.throwRandomly, entry.retainOwnership);
                }
            }
            this.entries.clear();
        }

        private record Entry(ItemVariant key, long amount, boolean throwRandomly, boolean retainOwnership) {
        }
    }
}

