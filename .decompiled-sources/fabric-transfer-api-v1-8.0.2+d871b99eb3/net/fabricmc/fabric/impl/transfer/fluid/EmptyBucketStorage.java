/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.fluid;

import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.BlankVariantView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.mixin.transfer.BucketItemAccessor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class EmptyBucketStorage
implements InsertionOnlyStorage<FluidVariant> {
    private final ContainerItemContext context;
    private final List<StorageView<FluidVariant>> blankView = List.of(new BlankVariantView<FluidVariant>(FluidVariant.blank(), 81000L));

    public EmptyBucketStorage(ContainerItemContext context) {
        this.context = context;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        ItemVariant newVariant;
        BucketItemAccessor accessor;
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        if (!this.context.getItemVariant().isOf(Items.BUCKET)) {
            return 0L;
        }
        Item fullBucket = resource.getFluid().getBucket();
        if (fullBucket instanceof BucketItemAccessor && resource.isOf((accessor = (BucketItemAccessor)((Object)fullBucket)).fabric_getContent()) && maxAmount >= 81000L && this.context.exchange(newVariant = ItemVariant.of(fullBucket, this.context.getItemVariant().getComponentsPatch()), 1L, transaction) == 1L) {
            return 81000L;
        }
        return 0L;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return this.blankView.iterator();
    }

    public String toString() {
        return "EmptyBucketStorage[" + String.valueOf(this.context) + "]";
    }
}

