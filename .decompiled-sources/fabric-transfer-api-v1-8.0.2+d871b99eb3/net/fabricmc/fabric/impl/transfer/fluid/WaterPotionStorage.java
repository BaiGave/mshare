/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.fluid;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public class WaterPotionStorage
implements ExtractionOnlyStorage<FluidVariant>,
SingleSlotStorage<FluidVariant> {
    private static final FluidVariant CONTAINED_FLUID = FluidVariant.of(Fluids.WATER);
    private static final long CONTAINED_AMOUNT = 27000L;
    private final ContainerItemContext context;

    public static @Nullable WaterPotionStorage find(ContainerItemContext context) {
        return WaterPotionStorage.isWaterPotion(context) ? new WaterPotionStorage(context) : null;
    }

    private static boolean isWaterPotion(ContainerItemContext context) {
        ItemVariant variant = context.getItemVariant();
        PotionContents potionContents = variant.getComponents().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return variant.isOf(Items.POTION) && potionContents.potion().orElse(null) == Potions.WATER;
    }

    private WaterPotionStorage(ContainerItemContext context) {
        this.context = context;
    }

    private boolean isWaterPotion() {
        return WaterPotionStorage.isWaterPotion(this.context);
    }

    private ItemVariant mapToGlassBottle() {
        ItemStack newStack = this.context.getItemVariant().toStack();
        newStack.set(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return ItemVariant.of(Items.GLASS_BOTTLE, newStack.getComponentsPatch());
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        if (!this.isWaterPotion()) {
            return 0L;
        }
        if (resource.equals(CONTAINED_FLUID) && maxAmount >= 27000L && this.context.exchange(this.mapToGlassBottle(), 1L, transaction) == 1L) {
            return 27000L;
        }
        return 0L;
    }

    @Override
    public boolean isResourceBlank() {
        return this.getResource().isBlank();
    }

    @Override
    public FluidVariant getResource() {
        if (this.isWaterPotion()) {
            return CONTAINED_FLUID;
        }
        return FluidVariant.blank();
    }

    @Override
    public long getAmount() {
        if (this.isWaterPotion()) {
            return 27000L;
        }
        return 0L;
    }

    @Override
    public long getCapacity() {
        return this.getAmount();
    }

    public String toString() {
        return "WaterPotionStorage[" + String.valueOf(this.context) + "]";
    }
}

