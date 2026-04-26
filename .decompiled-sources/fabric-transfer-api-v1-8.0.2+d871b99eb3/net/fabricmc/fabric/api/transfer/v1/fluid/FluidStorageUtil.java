/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.fluid;

import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.impl.transfer.DebugMessages;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

public final class FluidStorageUtil {
    public static boolean interactWithFluidStorage(Storage<FluidVariant> storage, Player player, InteractionHand hand) {
        Storage<FluidVariant> handStorage = ContainerItemContext.forPlayerInteraction(player, hand).find(FluidStorage.ITEM);
        if (handStorage == null) {
            return false;
        }
        Item handItem = player.getItemInHand(hand).getItem();
        try {
            return FluidStorageUtil.moveWithSound(storage, handStorage, player, true, handItem) || FluidStorageUtil.moveWithSound(handStorage, storage, player, false, handItem);
        }
        catch (Exception e) {
            CrashReport report = CrashReport.forThrowable(e, "Interacting with fluid storage");
            report.addCategory("Interaction details").setDetail("Player", () -> DebugMessages.forPlayer(player)).setDetail("Hand", (Object)hand).setDetail("Hand item", handItem::toString).setDetail("Fluid storage", () -> Objects.toString(storage, null));
            throw new ReportedException(report);
        }
    }

    private static boolean moveWithSound(Storage<FluidVariant> from, Storage<FluidVariant> to, Player player, boolean fill, Item handItem) {
        for (StorageView<FluidVariant> storageView : from) {
            long maxExtracted;
            if (storageView.isResourceBlank()) continue;
            FluidVariant resource = storageView.getResource();
            try (Transaction extractionTestTransaction = Transaction.openOuter();){
                maxExtracted = storageView.extract(resource, Long.MAX_VALUE, extractionTestTransaction);
                extractionTestTransaction.abort();
            }
            Transaction transferTransaction = Transaction.openOuter();
            try {
                SoundEvent sound;
                long accepted = to.insert(resource, maxExtracted, transferTransaction);
                if (accepted <= 0L || storageView.extract(resource, accepted, transferTransaction) != accepted) continue;
                transferTransaction.commit();
                SoundEvent soundEvent = sound = fill ? FluidVariantAttributes.getFillSound(resource) : FluidVariantAttributes.getEmptySound(resource);
                if (resource.isOf(Fluids.WATER)) {
                    if (fill && handItem == Items.GLASS_BOTTLE) {
                        sound = SoundEvents.BOTTLE_FILL;
                    }
                    if (!fill && handItem == Items.POTION) {
                        sound = SoundEvents.BOTTLE_EMPTY;
                    }
                }
                player.level().playSound((Entity)player, player.getX(), player.getEyeY(), player.getZ(), sound, SoundSource.PLAYERS, 1.0f, 1.0f);
                boolean bl = true;
                return bl;
            }
            finally {
                if (transferTransaction == null) continue;
                transferTransaction.close();
            }
        }
        return false;
    }

    private FluidStorageUtil() {
    }
}

