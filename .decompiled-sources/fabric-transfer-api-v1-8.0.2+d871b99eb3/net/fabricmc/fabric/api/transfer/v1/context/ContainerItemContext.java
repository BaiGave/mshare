/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.context;

import java.util.List;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.transfer.context.ConstantContainerItemContext;
import net.fabricmc.fabric.impl.transfer.context.CreativeInteractionContainerItemContext;
import net.fabricmc.fabric.impl.transfer.context.PlayerContainerItemContext;
import net.fabricmc.fabric.impl.transfer.context.SingleSlotContainerItemContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

public interface ContainerItemContext {
    public static ContainerItemContext forPlayerInteraction(Player player, InteractionHand hand) {
        if (player.hasInfiniteMaterials()) {
            return ContainerItemContext.forCreativeInteraction(player, player.getItemInHand(hand));
        }
        return ContainerItemContext.ofPlayerHand(player, hand);
    }

    public static ContainerItemContext forCreativeInteraction(Player player, ItemStack interactingStack) {
        return new CreativeInteractionContainerItemContext(ItemVariant.of(interactingStack), interactingStack.getCount(), player);
    }

    public static ContainerItemContext ofPlayerHand(Player player, InteractionHand hand) {
        return new PlayerContainerItemContext(player, hand);
    }

    public static ContainerItemContext ofPlayerCursor(Player player, AbstractContainerMenu menu) {
        return ContainerItemContext.ofPlayerSlot(player, PlayerInventoryStorage.getCursorStorage(menu));
    }

    public static ContainerItemContext ofPlayerSlot(Player player, SingleSlotStorage<ItemVariant> slot) {
        return new PlayerContainerItemContext(player, slot);
    }

    public static ContainerItemContext ofSingleSlot(SingleSlotStorage<ItemVariant> slot) {
        return new SingleSlotContainerItemContext(slot);
    }

    public static ContainerItemContext withConstant(ItemStack constantContent) {
        return ContainerItemContext.withConstant(ItemVariant.of(constantContent), constantContent.getCount());
    }

    public static ContainerItemContext withConstant(ItemVariant constantVariant, long constantAmount) {
        StoragePreconditions.notNegative(constantAmount);
        return new ConstantContainerItemContext(constantVariant, constantAmount);
    }

    default public <A> @Nullable A find(ItemApiLookup<A, ContainerItemContext> lookup) {
        return this.getItemVariant().isBlank() ? null : (A)lookup.find(this.getItemVariant().toStack(), this);
    }

    default public ItemVariant getItemVariant() {
        return (ItemVariant)this.getMainSlot().getResource();
    }

    default public long getAmount() {
        if (this.getItemVariant().isBlank()) {
            throw new IllegalStateException("Amount may not be queried when the current item variant is blank.");
        }
        return this.getMainSlot().getAmount();
    }

    default public long insert(ItemVariant itemVariant, long maxAmount, TransactionContext transaction) {
        long mainInserted = this.getMainSlot().insert(itemVariant, maxAmount, transaction);
        long overflowInserted = this.insertOverflow(itemVariant, maxAmount - mainInserted, transaction);
        return mainInserted + overflowInserted;
    }

    default public long extract(ItemVariant itemVariant, long maxAmount, TransactionContext transaction) {
        return this.getMainSlot().extract(itemVariant, maxAmount, transaction);
    }

    default public long exchange(ItemVariant newVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(newVariant, maxAmount);
        try (Transaction nested = transaction.openNested();){
            long extracted = this.extract(this.getItemVariant(), maxAmount, nested);
            if (this.insert(newVariant, extracted, nested) == extracted) {
                nested.commit();
                long l = extracted;
                return l;
            }
        }
        return 0L;
    }

    public SingleSlotStorage<ItemVariant> getMainSlot();

    public long insertOverflow(ItemVariant var1, long var2, TransactionContext var4);

    public @UnmodifiableView List<SingleSlotStorage<ItemVariant>> getAdditionalSlots();
}

