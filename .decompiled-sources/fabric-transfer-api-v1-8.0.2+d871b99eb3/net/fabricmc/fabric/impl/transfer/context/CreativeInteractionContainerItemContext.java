/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.context;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.transfer.context.ConstantContainerItemContext;
import net.minecraft.world.entity.player.Player;

public class CreativeInteractionContainerItemContext
extends ConstantContainerItemContext {
    private final PlayerInventoryStorage playerInventory;

    public CreativeInteractionContainerItemContext(ItemVariant initialVariant, long initialAmount, Player player) {
        super(initialVariant, initialAmount);
        this.playerInventory = PlayerInventoryStorage.of(player);
    }

    @Override
    public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
        StoragePreconditions.notBlankNotNegative(itemVariant, maxAmount);
        if (maxAmount > 0L) {
            boolean hasItem = false;
            for (SingleSlotStorage<ItemVariant> slot : this.playerInventory.getSlots()) {
                if (!((ItemVariant)slot.getResource()).equals(itemVariant) || slot.getAmount() <= 0L) continue;
                hasItem = true;
                break;
            }
            if (!hasItem) {
                this.playerInventory.offer(itemVariant, 1L, transactionContext);
            }
        }
        return maxAmount;
    }

    @Override
    public String toString() {
        return "CreativeInteractionContainerItemContext[%d %s]".formatted(this.getMainSlot().getAmount(), this.getMainSlot().getResource());
    }
}

