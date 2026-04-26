/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.context;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class SingleSlotContainerItemContext
implements ContainerItemContext {
    private final SingleSlotStorage<ItemVariant> slot;

    public SingleSlotContainerItemContext(SingleSlotStorage<ItemVariant> slot) {
        this.slot = Objects.requireNonNull(slot);
    }

    @Override
    public SingleSlotStorage<ItemVariant> getMainSlot() {
        return this.slot;
    }

    @Override
    public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
        return 0L;
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
        return Collections.emptyList();
    }

    public String toString() {
        return "SingleSlotContainerItemContext[%d %s %s]".formatted(this.slot.getAmount(), this.slot.getResource(), this.slot);
    }
}

