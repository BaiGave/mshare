/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.item;

import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.transfer.item.CursorSlotWrapper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface PlayerInventoryStorage
extends ContainerStorage {
    public static PlayerInventoryStorage of(Player player) {
        return PlayerInventoryStorage.of(player.getInventory());
    }

    public static PlayerInventoryStorage of(Inventory playerInventory) {
        return (PlayerInventoryStorage)ContainerStorage.of(playerInventory, null);
    }

    public static SingleSlotStorage<ItemVariant> getCursorStorage(AbstractContainerMenu menu) {
        return CursorSlotWrapper.get(menu);
    }

    @Override
    public long insert(ItemVariant var1, long var2, TransactionContext var4);

    default public void offerOrDrop(ItemVariant variant, long amount, TransactionContext transaction) {
        long offered = this.offer(variant, amount, transaction);
        this.drop(variant, amount - offered, transaction);
    }

    public long offer(ItemVariant var1, long var2, TransactionContext var4);

    public void drop(ItemVariant var1, long var2, boolean var4, boolean var5, TransactionContext var6);

    default public void drop(ItemVariant variant, long amount, boolean retainOwnership, TransactionContext transaction) {
        this.drop(variant, amount, false, retainOwnership, transaction);
    }

    default public void drop(ItemVariant variant, long amount, TransactionContext transaction) {
        this.drop(variant, amount, false, transaction);
    }

    public SingleSlotStorage<ItemVariant> getHandSlot(InteractionHand var1);
}

