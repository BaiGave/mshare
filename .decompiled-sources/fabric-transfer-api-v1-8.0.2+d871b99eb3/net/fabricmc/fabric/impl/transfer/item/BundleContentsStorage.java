/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.item;

import com.mojang.serialization.DataResult;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.mixin.transfer.BundleContentsAccessor;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;

public class BundleContentsStorage
implements Storage<ItemVariant> {
    private final ContainerItemContext ctx;
    private final List<BundleSlotWrapper> slotCache = new ArrayList<BundleSlotWrapper>();
    private List<StorageView<ItemVariant>> slots = List.of();
    private final Item originalItem;

    public BundleContentsStorage(ContainerItemContext ctx) {
        this.ctx = ctx;
        this.originalItem = ctx.getItemVariant().getItem();
    }

    private boolean updateStack(DataComponentPatch patch, TransactionContext transaction) {
        ItemVariant newVariant = this.ctx.getItemVariant().withComponents(patch);
        return this.ctx.exchange(newVariant, 1L, transaction) > 0L;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        ItemStack stack;
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        if (!this.isStillValid()) {
            return 0L;
        }
        if (maxAmount > Integer.MAX_VALUE) {
            maxAmount = Integer.MAX_VALUE;
        }
        if (!BundleContents.canItemBeInBundle(stack = resource.toStack((int)maxAmount))) {
            return 0L;
        }
        BundleContents.Mutable builder = new BundleContents.Mutable(this.bundleContents());
        int inserted = builder.tryInsert(stack);
        if (inserted == 0) {
            return 0L;
        }
        DataComponentPatch changes = DataComponentPatch.builder().set(DataComponents.BUNDLE_CONTENTS, builder.toImmutable()).build();
        if (!this.updateStack(changes, transaction)) {
            return 0L;
        }
        return inserted;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);
        if (!this.isStillValid()) {
            return 0L;
        }
        this.updateSlotsIfNeeded();
        long amount = 0L;
        for (StorageView<ItemVariant> slot : this.slots) {
            if ((amount += slot.extract(resource, maxAmount - amount, transaction)) == maxAmount) break;
        }
        return amount;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        this.updateSlotsIfNeeded();
        return this.slots.iterator();
    }

    private boolean isStillValid() {
        return this.ctx.getItemVariant().getItem() == this.originalItem;
    }

    private void updateSlotsIfNeeded() {
        int bundleSize = this.bundleContents().size();
        if (this.slots.size() != bundleSize) {
            while (bundleSize > this.slotCache.size()) {
                this.slotCache.add(new BundleSlotWrapper(this, this.slotCache.size()));
            }
            this.slots = Collections.unmodifiableList(this.slotCache.subList(0, bundleSize));
        }
    }

    BundleContents bundleContents() {
        return this.ctx.getItemVariant().getComponents().getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
    }

    private class BundleSlotWrapper
    implements StorageView<ItemVariant> {
        private final int index;
        final /* synthetic */ BundleContentsStorage this$0;

        private BundleSlotWrapper(BundleContentsStorage bundleContentsStorage, int index) {
            BundleContentsStorage bundleContentsStorage2 = bundleContentsStorage;
            Objects.requireNonNull(bundleContentsStorage2);
            this.this$0 = bundleContentsStorage2;
            this.index = index;
        }

        private ItemStack getStack() {
            if (this.this$0.bundleContents().size() <= this.index) {
                return ItemStack.EMPTY;
            }
            return this.this$0.bundleContents().items().get(this.index).create();
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notNegative(maxAmount);
            if (!this.this$0.isStillValid()) {
                return 0L;
            }
            if (this.this$0.bundleContents().size() <= this.index) {
                return 0L;
            }
            if (!resource.matches(this.getStack())) {
                return 0L;
            }
            ArrayList<ItemStackTemplate> stacksCopy = new ArrayList<ItemStackTemplate>(this.this$0.bundleContents().items());
            ItemStackTemplate toSrink = stacksCopy.get(this.index);
            int extracted = (int)Math.min((long)toSrink.count(), maxAmount);
            if (toSrink.count() - extracted <= 1) {
                stacksCopy.remove(this.index);
            } else {
                stacksCopy.set(this.index, new ItemStackTemplate(toSrink.item(), toSrink.count() - extracted, toSrink.components()));
            }
            DataComponentPatch changes = DataComponentPatch.builder().set(DataComponents.BUNDLE_CONTENTS, new BundleContents(stacksCopy)).build();
            if (!this.this$0.updateStack(changes, transaction)) {
                return 0L;
            }
            return extracted;
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
            Fraction remainingSpace = Fraction.ONE.subtract(BundleSlotWrapper.getWeight(this.this$0.bundleContents().weight()));
            int extraAllowed = Math.max(remainingSpace.divideBy(BundleSlotWrapper.getWeight(BundleContentsAccessor.getWeight(this.getStack()))).intValue(), 0);
            return this.getAmount() + (long)extraAllowed;
        }

        private static Fraction getWeight(DataResult<Fraction> weight) {
            DataResult<Fraction> dataResult = weight;
            Objects.requireNonNull(dataResult);
            DataResult<Fraction> dataResult2 = dataResult;
            int n = 0;
            return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{DataResult.Success.class, DataResult.Error.class}, dataResult2, n)) {
                default -> throw new MatchException(null, null);
                case 0 -> {
                    DataResult.Success success = (DataResult.Success)dataResult2;
                    yield (Fraction)success.value();
                }
                case 1 -> {
                    DataResult.Error ignored = (DataResult.Error)dataResult2;
                    yield Fraction.ONE;
                }
            };
        }
    }
}

