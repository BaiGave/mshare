/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.player;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public interface BlockEvents {
    public static final Event<UseItemOnCallback> USE_ITEM_ON = EventFactory.createArrayBacked(UseItemOnCallback.class, listeners -> (itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult) -> {
        for (UseItemOnCallback event : listeners) {
            InteractionResult result = event.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
            if (result == null) continue;
            return result;
        }
        return null;
    });
    public static final Event<UseWithoutItemCallback> USE_WITHOUT_ITEM = EventFactory.createArrayBacked(UseWithoutItemCallback.class, listeners -> (blockState, level, blockPos, player, blockHitResult) -> {
        for (UseWithoutItemCallback event : listeners) {
            InteractionResult result = event.useWithoutItem(blockState, level, blockPos, player, blockHitResult);
            if (result == null) continue;
            return result;
        }
        return null;
    });

    @FunctionalInterface
    public static interface UseWithoutItemCallback {
        public @Nullable InteractionResult useWithoutItem(BlockState var1, Level var2, BlockPos var3, Player var4, BlockHitResult var5);
    }

    @FunctionalInterface
    public static interface UseItemOnCallback {
        public @Nullable InteractionResult useItemOn(ItemStack var1, BlockState var2, Level var3, BlockPos var4, Player var5, InteractionHand var6, BlockHitResult var7);
    }
}

