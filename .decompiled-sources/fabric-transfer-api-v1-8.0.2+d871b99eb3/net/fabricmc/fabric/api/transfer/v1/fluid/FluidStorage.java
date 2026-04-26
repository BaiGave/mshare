/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.fluid;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.CauldronFluidContent;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.impl.transfer.fluid.CombinedProvidersImpl;
import net.fabricmc.fabric.impl.transfer.fluid.EmptyBucketStorage;
import net.fabricmc.fabric.impl.transfer.fluid.WaterPotionStorage;
import net.fabricmc.fabric.mixin.transfer.BucketItemAccessor;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public final class FluidStorage {
    public static final BlockApiLookup<Storage<FluidVariant>, @Nullable Direction> SIDED = BlockApiLookup.get(Identifier.fromNamespaceAndPath("fabric", "sided_fluid_storage"), Storage.asClass(), Direction.class);
    public static final ItemApiLookup<Storage<FluidVariant>, ContainerItemContext> ITEM = ItemApiLookup.get(Identifier.fromNamespaceAndPath("fabric", "fluid_storage"), Storage.asClass(), ContainerItemContext.class);
    public static final Event<CombinedItemApiProvider> GENERAL_COMBINED_PROVIDER = CombinedProvidersImpl.createEvent(false);

    public static Event<CombinedItemApiProvider> combinedItemApiProvider(Item item) {
        return CombinedProvidersImpl.getOrCreateItemEvent(item);
    }

    private FluidStorage() {
    }

    static {
        CauldronFluidContent.getForFluid(Fluids.WATER);
        SIDED.registerFallback((level, pos, state, blockEntity, direction) -> {
            if (blockEntity instanceof SidedStorageBlockEntity) {
                SidedStorageBlockEntity sidedStorageBlockEntity = (SidedStorageBlockEntity)((Object)blockEntity);
                return sidedStorageBlockEntity.getFluidStorage((Direction)direction);
            }
            return null;
        });
        ITEM.registerFallback((stack, context) -> GENERAL_COMBINED_PROVIDER.invoker().find((ContainerItemContext)context));
        FluidStorage.combinedItemApiProvider(Items.BUCKET).register(EmptyBucketStorage::new);
        GENERAL_COMBINED_PROVIDER.register(context -> {
            BucketItem bucketItem;
            Fluid bucketFluid;
            Item patt0$temp = context.getItemVariant().getItem();
            if (patt0$temp instanceof BucketItem && (bucketFluid = ((BucketItemAccessor)((Object)(bucketItem = (BucketItem)patt0$temp))).fabric_getContent()) != null && bucketFluid.getBucket() == bucketItem) {
                return new FullItemFluidStorage(context, Items.BUCKET, FluidVariant.of(bucketFluid), 81000L);
            }
            return null;
        });
        FluidStorage.combinedItemApiProvider(Items.GLASS_BOTTLE).register(context -> new EmptyItemFluidStorage(context, emptyBottle -> {
            ItemStack newStack = emptyBottle.toStack();
            newStack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
            return ItemVariant.of(Items.POTION, newStack.getComponentsPatch());
        }, (Fluid)Fluids.WATER, 27000L));
        FluidStorage.combinedItemApiProvider(Items.POTION).register(WaterPotionStorage::find);
    }

    @FunctionalInterface
    public static interface CombinedItemApiProvider {
        public @Nullable Storage<FluidVariant> find(ContainerItemContext var1);
    }
}

