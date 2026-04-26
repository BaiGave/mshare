/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.context;

import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class PlayerContainerItemContext
implements ContainerItemContext {
    private final PlayerInventoryStorage playerWrapper;
    private final SingleSlotStorage<ItemVariant> slot;

    public PlayerContainerItemContext(Player player, InteractionHand hand) {
        this.playerWrapper = PlayerInventoryStorage.of(player);
        this.slot = this.playerWrapper.getHandSlot(hand);
    }

    public PlayerContainerItemContext(Player player, SingleSlotStorage<ItemVariant> slot) {
        this.playerWrapper = PlayerInventoryStorage.of(player);
        this.slot = slot;
    }

    @Override
    public SingleSlotStorage<ItemVariant> getMainSlot() {
        return this.slot;
    }

    @Override
    public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
        this.playerWrapper.offerOrDrop(itemVariant, maxAmount, transactionContext);
        return maxAmount;
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
        return this.playerWrapper.getSlots();
    }

    public String toString() {
        return "PlayerContainerItemContext[%d %s %s/%s]".formatted(this.slot.getAmount(), this.slot.getResource(), this.playerWrapper, this.slot);
    }
}

