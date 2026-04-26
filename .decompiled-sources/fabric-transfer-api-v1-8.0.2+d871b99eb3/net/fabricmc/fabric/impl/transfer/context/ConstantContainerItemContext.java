/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.context;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class ConstantContainerItemContext
implements ContainerItemContext {
    private final SingleVariantStorage<ItemVariant> backingSlot = new SingleVariantStorage<ItemVariant>(this){
        {
            Objects.requireNonNull(this$0);
        }

        @Override
        protected ItemVariant getBlankVariant() {
            return ItemVariant.blank();
        }

        @Override
        protected long getCapacity(ItemVariant variant) {
            return Long.MAX_VALUE;
        }

        @Override
        public long insert(ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);
            return 0L;
        }

        @Override
        public long extract(ItemVariant extractedVariant, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(extractedVariant, maxAmount);
            return maxAmount;
        }
    };

    public ConstantContainerItemContext(ItemVariant initialVariant, long initialAmount) {
        this.backingSlot.variant = initialVariant;
        this.backingSlot.amount = initialAmount;
    }

    @Override
    public SingleSlotStorage<ItemVariant> getMainSlot() {
        return this.backingSlot;
    }

    @Override
    public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
        StoragePreconditions.notBlankNotNegative(itemVariant, maxAmount);
        return maxAmount;
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
        return Collections.emptyList();
    }

    public String toString() {
        return "ConstantContainerItemContext[%d %s]".formatted(this.getMainSlot().getAmount(), this.getMainSlot().getResource());
    }
}

