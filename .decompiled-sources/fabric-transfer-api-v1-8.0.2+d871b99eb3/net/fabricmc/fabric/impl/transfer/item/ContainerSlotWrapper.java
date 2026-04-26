/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.item;

import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.transfer.DebugMessages;
import net.fabricmc.fabric.impl.transfer.item.ContainerStorageImpl;
import net.fabricmc.fabric.impl.transfer.item.ItemVariantImpl;
import net.fabricmc.fabric.impl.transfer.item.SpecialLogicContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.jspecify.annotations.Nullable;

class ContainerSlotWrapper
extends SingleStackStorage {
    private final ContainerStorageImpl storage;
    final int slot;
    private final @Nullable SpecialLogicContainer specialContainer;
    private ItemStack lastReleasedSnapshot = null;

    ContainerSlotWrapper(ContainerStorageImpl storage, int slot) {
        SpecialLogicContainer special;
        this.storage = storage;
        this.slot = slot;
        Container container = storage.container;
        this.specialContainer = container instanceof SpecialLogicContainer ? (special = (SpecialLogicContainer)((Object)container)) : null;
    }

    @Override
    protected ItemStack getStack() {
        return this.storage.container.getItem(this.slot);
    }

    @Override
    protected void setStack(ItemStack stack) {
        if (this.specialContainer == null) {
            this.storage.container.setItem(this.slot, stack);
        } else {
            this.specialContainer.fabric_setSuppress(true);
            try {
                this.storage.container.setItem(this.slot, stack);
            }
            finally {
                this.specialContainer.fabric_setSuppress(false);
            }
        }
    }

    @Override
    public long insert(ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
        if (!this.canInsert(this.slot, ((ItemVariantImpl)insertedVariant).getCachedStack())) {
            return 0L;
        }
        long ret = super.insert(insertedVariant, maxAmount, transaction);
        if (this.specialContainer != null && ret > 0L) {
            this.specialContainer.fabric_onTransfer(this.slot, transaction);
        }
        return ret;
    }

    private boolean canInsert(int slot, ItemStack stack) {
        Container container = this.storage.container;
        if (container instanceof ShulkerBoxBlockEntity) {
            ShulkerBoxBlockEntity shulker = (ShulkerBoxBlockEntity)container;
            return shulker.canPlaceItemThroughFace(slot, stack, null);
        }
        return this.storage.container.canPlaceItem(slot, stack);
    }

    @Override
    public long extract(ItemVariant variant, long maxAmount, TransactionContext transaction) {
        long ret = super.extract(variant, maxAmount, transaction);
        if (this.specialContainer != null && ret > 0L) {
            this.specialContainer.fabric_onTransfer(this.slot, transaction);
        }
        return ret;
    }

    @Override
    public int getCapacity(ItemVariant variant) {
        if (this.storage.container instanceof AbstractFurnaceBlockEntity && this.slot == 1 && variant.isOf(Items.BUCKET)) {
            return 1;
        }
        if (this.storage.container instanceof BrewingStandBlockEntity && this.slot < 3) {
            return 1;
        }
        return Math.min(this.storage.container.getMaxStackSize(), ItemVariantImpl.getMaxStackSize(variant));
    }

    @Override
    public void updateSnapshots(TransactionContext transaction) {
        ChestBlockEntity chest;
        this.storage.setChangedParticipant.updateSnapshots(transaction);
        super.updateSnapshots(transaction);
        Container container = this.storage.container;
        if (container instanceof ChestBlockEntity && (chest = (ChestBlockEntity)container).getBlockState().getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            BlockPos otherChestPos = chest.getBlockPos().relative(ChestBlock.getConnectedDirection(chest.getBlockState()));
            BlockEntity blockEntity = chest.getLevel().getBlockEntity(otherChestPos);
            if (blockEntity instanceof ChestBlockEntity) {
                ChestBlockEntity otherChest = (ChestBlockEntity)blockEntity;
                ((ContainerStorageImpl)ContainerStorageImpl.of((Container)otherChest, null)).setChangedParticipant.updateSnapshots(transaction);
            }
        }
    }

    @Override
    protected void releaseSnapshot(ItemStack snapshot) {
        this.lastReleasedSnapshot = snapshot;
    }

    @Override
    protected void onFinalCommit() {
        ItemStack original = this.lastReleasedSnapshot;
        ItemStack currentStack = this.getStack();
        Container container = this.storage.container;
        if (container instanceof SpecialLogicContainer) {
            SpecialLogicContainer specialLogicInv = (SpecialLogicContainer)((Object)container);
            specialLogicInv.fabric_onFinalCommit(this.slot, original, currentStack);
        }
        if (!original.isEmpty() && original.getItem() == currentStack.getItem()) {
            if (!Objects.equals(original.getComponentsPatch(), currentStack.getComponentsPatch())) {
                for (DataComponentType<?> type : original.getComponents().keySet()) {
                    original.set(type, null);
                }
                original.applyComponents(currentStack.getComponents());
            }
            original.setCount(currentStack.getCount());
            this.setStack(original);
        } else {
            original.setCount(0);
        }
    }

    @Override
    public String toString() {
        return "ContainerSlotWrapper[%s#%d]".formatted(DebugMessages.forInventory(this.storage.container), this.slot);
    }
}

