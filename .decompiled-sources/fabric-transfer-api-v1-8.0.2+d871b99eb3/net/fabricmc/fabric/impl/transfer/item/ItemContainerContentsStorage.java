/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedSlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.mixin.transfer.ItemContainerContentsAccessor;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;

public class ItemContainerContentsStorage
extends CombinedSlottedStorage<ItemVariant, SingleSlotStorage<ItemVariant>> {
    final ContainerItemContext ctx;
    private final Item originalItem;

    public ItemContainerContentsStorage(ContainerItemContext ctx, int slots) {
        super(Collections.emptyList());
        this.ctx = ctx;
        this.originalItem = ctx.getItemVariant().getItem();
        ArrayList<ContainerSlotWrapper> backingList = new ArrayList<ContainerSlotWrapper>(slots);
        for (int i = 0; i < slots; ++i) {
            backingList.add(new ContainerSlotWrapper(this, i));
        }
        this.parts = Collections.unmodifiableList(backingList);
    }

    ItemContainerContents container() {
        return this.ctx.getItemVariant().getComponents().getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
    }

    ItemContainerContentsAccessor containerAccessor() {
        return (ItemContainerContentsAccessor)((Object)this.container());
    }

    private boolean isStillValid() {
        return this.ctx.getItemVariant().getItem() == this.originalItem;
    }

    private class ContainerSlotWrapper
    implements SingleSlotStorage<ItemVariant> {
        final int slot;
        final /* synthetic */ ItemContainerContentsStorage this$0;

        ContainerSlotWrapper(ItemContainerContentsStorage itemContainerContentsStorage, int slot) {
            ItemContainerContentsStorage itemContainerContentsStorage2 = itemContainerContentsStorage;
            Objects.requireNonNull(itemContainerContentsStorage2);
            this.this$0 = itemContainerContentsStorage2;
            this.slot = slot;
        }

        private ItemStack getStack() {
            List<Optional<ItemStackTemplate>> stacks = this.this$0.containerAccessor().fabric_getItems();
            if (stacks.size() <= this.slot) {
                return ItemStack.EMPTY;
            }
            return stacks.get(this.slot).map(ItemStackTemplate::create).orElse(ItemStack.EMPTY);
        }

        protected boolean setStack(ItemStack stack, TransactionContext transaction) {
            List<ItemStack> stacks = this.this$0.container().allItemsCopyStream().collect(Collectors.toList());
            while (stacks.size() <= this.slot) {
                stacks.add(ItemStack.EMPTY);
            }
            stacks.set(this.slot, stack);
            ContainerItemContext ctx = this.this$0.ctx;
            ItemVariant newVariant = ctx.getItemVariant().withComponents(DataComponentPatch.builder().set(DataComponents.CONTAINER, ItemContainerContents.fromItems(stacks)).build());
            return ctx.exchange(newVariant, 1L, transaction) == 1L;
        }

        @Override
        public long insert(ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
            int insertedAmount;
            StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);
            if (!this.this$0.isStillValid()) {
                return 0L;
            }
            ItemStack currentStack = this.getStack();
            if ((insertedVariant.matches(currentStack) || currentStack.isEmpty()) && insertedVariant.getItem().canFitInsideContainerItems() && (insertedAmount = (int)Math.min(maxAmount, this.getCapacity() - (long)currentStack.getCount())) > 0) {
                currentStack = this.getStack().copy();
                if (currentStack.isEmpty()) {
                    currentStack = insertedVariant.toStack(insertedAmount);
                } else {
                    currentStack.grow(insertedAmount);
                }
                if (!this.setStack(currentStack, transaction)) {
                    return 0L;
                }
                return insertedAmount;
            }
            return 0L;
        }

        @Override
        public long extract(ItemVariant variant, long maxAmount, TransactionContext transaction) {
            int extracted;
            StoragePreconditions.notBlankNotNegative(variant, maxAmount);
            if (!this.this$0.isStillValid()) {
                return 0L;
            }
            ItemStack currentStack = this.getStack();
            if (variant.matches(currentStack) && (extracted = (int)Math.min((long)currentStack.getCount(), maxAmount)) > 0) {
                currentStack = this.getStack().copy();
                currentStack.shrink(extracted);
                if (!this.setStack(currentStack, transaction)) {
                    return 0L;
                }
                return extracted;
            }
            return 0L;
        }

        @Override
        public boolean isResourceBlank() {
            return this.getStack().isEmpty();
        }

        @Override
        public ItemVariant getResource() {
            return ItemVariant.of(this.getStack());
        }

        @Override
        public long getAmount() {
            return this.getStack().getCount();
        }

        @Override
        public long getCapacity() {
            return this.getStack().getMaxStackSize();
        }

        public String toString() {
            return "ContainerSlotWrapper[%s#%d]".formatted(this.this$0.ctx.getItemVariant(), this.slot);
        }
    }
}

